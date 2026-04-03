import cv2
import numpy as np
import math


def process_frame_harris(frame, blockSize=2, ksize=3, k=0.04, thresh=0.01):
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    gray_f = np.float32(gray)
    dst = cv2.cornerHarris(gray_f, blockSize, ksize, k)
    dst = cv2.dilate(dst, None)
    mask = dst > thresh * dst.max()
    res = frame.copy()
    res[mask] = [0, 0, 255]
    return res, dst


def process_frame_hough(frame):
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    edges = cv2.Canny(gray, 50, 150, apertureSize=3)
    lines = cv2.HoughLines(edges, 1, np.pi / 180, 200)

    out = frame.copy()
    if lines is None:
        return out

    for r_theta in lines:
        r, theta = np.array(r_theta[0], dtype=np.float64)
        a = np.cos(theta)
        b = np.sin(theta)
        x0 = a * r
        y0 = b * r
        x1 = int(x0 + 1000 * (-b))
        y1 = int(y0 + 1000 * (a))
        x2 = int(x0 - 1000 * (-b))
        y2 = int(y0 - 1000 * (a))
        cv2.line(out, (x1, y1), (x2, y2), (0, 0, 255), 1)

    return out


def thresholding_Otsu_frame(frame):
    """
    Otsu + Auto-Invert: sorgt dafür, dass "Münzen" eher weiß werden.
    (Bei schwarzem Blatt kippt Otsu sonst gerne.)
    """
    if frame.ndim == 3 and frame.shape[2] == 3:
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    elif frame.ndim == 2:
        gray = frame.copy()
    else:
        gray = cv2.cvtColor(frame.astype(np.uint8), cv2.COLOR_BGR2GRAY)

    if gray.dtype != np.uint8:
        gray = cv2.normalize(gray, None, 0, 255, cv2.NORM_MINMAX).astype(np.uint8)

    _, th = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

    # Auto-Invert, wenn "zu viel" weiß ist (dann ist Hintergrund vermutlich weiß geworden)
    white_ratio = (th > 0).mean()
    if white_ratio > 0.70:
        th = cv2.bitwise_not(th)

    return th


def distanceTransform_frame(frame):
    return cv2.distanceTransform(frame, cv2.DIST_L2, 5)


def watershedAlgorithm_frame(gray_frame, markers, src_color=None):
    if src_color is None:
        src_color = gray_frame

    src = src_color.copy()
    if src.ndim == 2:
        src = cv2.cvtColor(src, cv2.COLOR_GRAY2BGR)
    if src.dtype != np.uint8:
        src = cv2.normalize(src, None, 0, 255, cv2.NORM_MINMAX).astype(np.uint8)

    markers = markers.astype(np.int32)
    markers = cv2.watershed(src, markers)

    labels = np.unique(markers)
    coins = []
    for label in labels:
        if label <= 1:
            continue
        target = np.where(markers == label, 255, 0).astype(np.uint8)
        contours_info = cv2.findContours(target, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        contours = contours_info[0] if len(contours_info) == 2 else contours_info[1]
        if not contours:
            continue
        coins.append(contours[0])

    out = src.copy()
    if coins:
        out = cv2.drawContours(out, coins, -1, color=(0, 23, 223), thickness=2)
    return out


def classify_coins_bayes_frame(frame, paper_size_mm=(210, 297), priors=None, sigma_mm=0.8):
    thresh = thresholding_Otsu_frame(frame)

    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (3, 3))
    sure_bg = cv2.dilate(thresh, kernel, iterations=3)

    dist = distanceTransform_frame(thresh)
    _, sure_fg = cv2.threshold(dist, 0.5 * dist.max(), 255, cv2.THRESH_BINARY)
    sure_fg = sure_fg.astype(np.uint8)

    unknown = cv2.subtract(sure_bg, sure_fg)

    _, markers = cv2.connectedComponents(sure_fg)
    markers = markers + 1
    markers[unknown == 255] = 0
    markers_ws = markers.astype(np.int32)

    src = frame.copy()
    if src.ndim == 2:
        src = cv2.cvtColor(src, cv2.COLOR_GRAY2BGR)
    if src.dtype != np.uint8:
        src = cv2.normalize(src, None, 0, 255, cv2.NORM_MINMAX).astype(np.uint8)

    cv2.watershed(src, markers_ws)

    h_px, w_px = src.shape[:2]
    px_per_mm_x = w_px / float(paper_size_mm[0])
    px_per_mm_y = h_px / float(paper_size_mm[1])
    px_per_mm = (px_per_mm_x + px_per_mm_y) / 2.0

    coin_diameters_mm = {
        '5 Rp': 17.15,
        '10 Rp': 19.15,
        '20 Rp': 21.05,
        '50 Rp': 23.00,
        '1 Fr': 23.2,
        '2 Fr': 27.4,
        '5 Fr': 31.45
    }
    coin_values_chf = {
        '5 Rp': 0.05,
        '10 Rp': 0.10,
        '20 Rp': 0.20,
        '50 Rp': 0.50,
        '1 Fr': 1.00,
        '2 Fr': 2.00,
        '5 Fr': 5.00
    }

    classes = list(coin_diameters_mm.keys())
    if priors is None:
        priors = {c: 1.0 / len(classes) for c in classes}
    else:
        s = sum(priors.values())
        priors = {c: (priors.get(c, 0.0) / s) if s > 0 else (1.0 / len(classes)) for c in classes}

    annotated = src.copy()
    total_chf = 0.0

    labels = np.unique(markers_ws)
    for label in labels:
        if label <= 1:
            continue

        mask = (markers_ws == label).astype(np.uint8) * 255
        contours_info = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        contours = contours_info[0] if len(contours_info) == 2 else contours_info[1]
        if not contours:
            continue

        cnt = max(contours, key=cv2.contourArea)
        (x, y), radius_px = cv2.minEnclosingCircle(cnt)
        if radius_px <= 2:
            continue

        diameter_px = 2.0 * radius_px
        diameter_mm = diameter_px / px_per_mm

        var = sigma_mm * sigma_mm
        norm = 1.0 / math.sqrt(2.0 * math.pi * var)

        posteriors = {}
        for c in classes:
            mu = coin_diameters_mm[c]
            likelihood = norm * math.exp(-0.5 * ((diameter_mm - mu) ** 2) / var)
            posteriors[c] = priors.get(c, 1e-9) * likelihood

        best_class = max(posteriors.items(), key=lambda kv: kv[1])[0]
        value = coin_values_chf.get(best_class, 0.0)
        total_chf += value

        center = (int(x), int(y))
        cv2.circle(annotated, center, int(radius_px), (0, 200, 0), 2)
        text = f"{best_class} {value:.2f}CHF {diameter_mm:.1f}mm"
        cv2.putText(annotated, text, (int(x - radius_px), int(y - radius_px - 10)),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1, cv2.LINE_AA)

    cv2.putText(annotated, f"Total: {total_chf:.2f} CHF", (10, 25),
                cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 255), 2, cv2.LINE_AA)

    return annotated, total_chf


def process_homography_frame(
    frame,
    paper_size_mm=(210, 297),
    orientation="portrait",
    min_area_ratio=0.08,
    ratio_min=1.15,
    ratio_max=1.80,
    debug=True
):
    """
    Fix gegen Verzerrung:
    - Warped-Größe NICHT aus DPI, sondern aus dem gefundenen Rechteck (rw/rh)
    - Orientiert Portrait/Landscape passend
    Rückgabe: (warped_or_frame, paper_found_bool)
    """

    def order_points(pts):
        pts = np.array(pts, dtype="float32")
        rect = np.zeros((4, 2), dtype="float32")
        s = pts.sum(axis=1)
        rect[0] = pts[np.argmin(s)]  # tl
        rect[2] = pts[np.argmax(s)]  # br
        diff = np.diff(pts, axis=1)
        rect[1] = pts[np.argmin(diff)]  # tr
        rect[3] = pts[np.argmax(diff)]  # bl
        return rect

    h_img, w_img = frame.shape[:2]
    frame_area = float(h_img * w_img)
    min_area = min_area_ratio * frame_area

    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray, (7, 7), 0)

    # Schwarzes Blatt => invertiertes Binary: dunkel -> weiß
    _, th = cv2.threshold(blur, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)

    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (9, 9))
    th = cv2.morphologyEx(th, cv2.MORPH_CLOSE, kernel, iterations=2)
    th = cv2.morphologyEx(th, cv2.MORPH_OPEN, kernel, iterations=1)

    if debug:
        cv2.imshow("DEBUG threshold", th)

    contours_info = cv2.findContours(th, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    contours = contours_info[0] if len(contours_info) == 2 else contours_info[1]
    if not contours:
        return frame, False

    cnt = max(contours, key=cv2.contourArea)
    area = cv2.contourArea(cnt)
    if area < min_area:
        return frame, False

    rect = cv2.minAreaRect(cnt)  # ((cx,cy),(rw,rh),angle)
    (_, _), (rw, rh), _ = rect
    if rw <= 1 or rh <= 1:
        return frame, False

    ratio = max(rw, rh) / min(rw, rh)
    if ratio < ratio_min or ratio > ratio_max:
        return frame, False

    # 4 Eckpunkte aus rect
    box = cv2.boxPoints(rect)
    src_quad = order_points(box)

    # Zielgröße aus rw/rh, passend zu Orientation
    # (sonst kippt die Zuordnung width/height und du bekommst starke Verzerrung)
    if orientation == "portrait":
        out_w = int(round(min(rw, rh)))
        out_h = int(round(max(rw, rh)))
    else:
        out_w = int(round(max(rw, rh)))
        out_h = int(round(min(rw, rh)))

    # Mindestgröße, damit coins nicht zu klein werden
    out_w = max(out_w, 600)
    out_h = max(out_h, 800)

    dst_quad = np.array([[0, 0], [out_w - 1, 0], [out_w - 1, out_h - 1], [0, out_h - 1]], dtype="float32")

    M = cv2.getPerspectiveTransform(src_quad, dst_quad)
    warped = cv2.warpPerspective(frame, M, (out_w, out_h))

    if debug:
        dbg = frame.copy()
        cv2.polylines(dbg, [src_quad.astype(int)], True, (0, 255, 0), 2)
        cv2.putText(dbg, f"OK area={int(area)} rw={rw:.0f} rh={rh:.0f} ratio={ratio:.2f}", (10, 30),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 255, 0), 2, cv2.LINE_AA)
        cv2.imshow("DEBUG paper overlay", dbg)

    return warped, True

def get_px_per_mm_from_warp(warped_shape, paper_size_mm=(210, 297), orientation="portrait"):
    h_px, w_px = warped_shape[:2]
    w_mm, h_mm = paper_size_mm
    if orientation == "landscape":
        w_mm, h_mm = h_mm, w_mm

    px_per_mm_x = w_px / float(w_mm)
    px_per_mm_y = h_px / float(h_mm)
    return (px_per_mm_x + px_per_mm_y) / 2.0


def detect_and_classify_coins_hough(
    warped_bgr,
    paper_size_mm=(210, 297),
    orientation="portrait",
    sigma_mm=0.8,
    dp=1.2,
    minDist=35,
    param1=120,
    param2=28,
    minRadius=10,
    maxRadius=120,
):
    """
    Rückgabe: (annotated_warped_bgr, total_chf)

    - HoughCircles findet Kreise (Münzen) im entzerrten Bild
    - Durchmesser in mm via px_per_mm (über Papierformat)
    - Bayes/Nearest-ähnliche Klassifikation über bekannte Durchmesser
    """

    coin_diameters_mm = {
        '5 Rp': 17.15,
        '10 Rp': 19.15,
        '20 Rp': 21.05,
        '50 Rp': 23.00,
        '1 Fr': 23.2,
        '2 Fr': 27.4,
        '5 Fr': 31.45
    }
    coin_values_chf = {
        '5 Rp': 0.05,
        '10 Rp': 0.10,
        '20 Rp': 0.20,
        '50 Rp': 0.50,
        '1 Fr': 1.00,
        '2 Fr': 2.00,
        '5 Fr': 5.00
    }

    annotated = warped_bgr.copy()

    gray = cv2.cvtColor(warped_bgr, cv2.COLOR_BGR2GRAY)
    gray = cv2.medianBlur(gray, 7)

    # px/mm über A4
    px_per_mm = get_px_per_mm_from_warp(warped_bgr.shape, paper_size_mm, orientation)

    # HoughCircles
    circles = cv2.HoughCircles(
        gray,
        cv2.HOUGH_GRADIENT,
        dp=dp,
        minDist=minDist,
        param1=param1,
        param2=param2,
        minRadius=minRadius,
        maxRadius=maxRadius
    )

    total = 0.0
    if circles is None:
        cv2.putText(annotated, "No coins detected", (10, 25),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 2, cv2.LINE_AA)
        return annotated, total

    circles = np.round(circles[0, :]).astype("int")

    classes = list(coin_diameters_mm.keys())

    for (x, y, r) in circles:
        diameter_px = 2.0 * r
        diameter_mm = diameter_px / px_per_mm

        # "Bayes" (gauss) über Durchmesser
        var = sigma_mm * sigma_mm
        norm = 1.0 / math.sqrt(2.0 * math.pi * var)

        post = {}
        for c in classes:
            mu = coin_diameters_mm[c]
            likelihood = norm * math.exp(-0.5 * ((diameter_mm - mu) ** 2) / var)
            post[c] = likelihood  # Priors optional -> hier gleich

        best = max(post.items(), key=lambda kv: kv[1])[0]
        value = coin_values_chf.get(best, 0.0)
        total += value

        # Zeichnen
        cv2.circle(annotated, (x, y), r, (0, 255, 0), 2)
        cv2.circle(annotated, (x, y), 2, (0, 255, 0), 3)
        cv2.putText(annotated, f"{best} {value:.2f} CHF ({diameter_mm:.1f}mm)",
                    (x - r, y - r - 8),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1, cv2.LINE_AA)

    cv2.putText(annotated, f"Total: {total:.2f} CHF", (10, 25),
                cv2.FONT_HERSHEY_SIMPLEX, 0.9, (0, 255, 255), 2, cv2.LINE_AA)

    return annotated, total


def cameraCapture():
    cam = cv2.VideoCapture(0)
    if not cam.isOpened():
        print("Kamera konnte nicht geöffnet werden")
        return

    debug = True
    last_total = 0.0  # damit Total nicht sofort 0 wird, wenn Blatt kurz weg ist

    while True:
        ret, frame = cam.read()
        if not ret:
            print("Frame konnte nicht gelesen werden")
            break

        # Blatt erkennen + entzerren
        homography_result, paper_found = process_homography_frame(
            frame,
            paper_size_mm=(210, 297),
            orientation='portrait',
            min_area_ratio=0.08,
            ratio_min=1.15,
            ratio_max=1.80,
            debug=debug
        )

        display = frame.copy()

        if paper_found:
            # Münzen auf dem entzerrten Bild erkennen + Gesamtwert berechnen
            annotated_homography, total_chf = detect_and_classify_coins_hough(
                homography_result,
                paper_size_mm=(210, 297),
                orientation="portrait",
                dp=1.2,
                minDist=40,
                param1=120,
                param2=28,
                minRadius=12,
                maxRadius=140
            )
            last_total = total_chf

            cv2.putText(display, "Paper: DETECTED", (10, 60),
                        cv2.FONT_HERSHEY_SIMPLEX, 1.0, (0, 255, 0), 2, cv2.LINE_AA)
        else:
            annotated_homography = homography_result
            cv2.putText(display, "Paper: NOT DETECTED", (10, 60),
                        cv2.FONT_HERSHEY_SIMPLEX, 1.0, (0, 0, 255), 2, cv2.LINE_AA)

        # Total IM Live-Video anzeigen (immer)
        cv2.putText(display, f"Total: {last_total:.2f} CHF", (10, 30),
                    cv2.FONT_HERSHEY_SIMPLEX, 1.0, (0, 255, 255), 2, cv2.LINE_AA)

        cv2.imshow('Live Camera', display)
        cv2.imshow('Coins (homography)', annotated_homography)

        key = cv2.waitKey(1) & 0xFF
        if key == ord('q'):
            break
        if key == ord('d'):
            debug = not debug

    cam.release()
    cv2.destroyAllWindows()



def main():
    cameraCapture()


if __name__ == "__main__":
    main()
