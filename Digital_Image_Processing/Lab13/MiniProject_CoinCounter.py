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

    if lines is None:
        return frame

    for r_theta in lines:
        arr = np.array(r_theta[0], dtype=np.float64)
        r, theta = arr
        a = np.cos(theta)
        b = np.sin(theta)
        x0 = a * r
        y0 = b * r
        x1 = int(x0 + 1000 * (-b))
        y1 = int(y0 + 1000 * (a))
        x2 = int(x0 - 1000 * (-b))
        y2 = int(y0 - 1000 * (a))
        cv2.line(frame, (x1, y1), (x2, y2), (0, 0, 255))

    return frame

def process_homography_frame(frame, paper_size_mm=(210, 297), dpi=150, orientation='portrait', area_thresh=1000):
    def order_points(pts):
        rect = np.zeros((4, 2), dtype="float32")
        s = pts.sum(axis=1)
        rect[0] = pts[np.argmin(s)]  # tl
        rect[2] = pts[np.argmax(s)]  # br
        diff = np.diff(pts, axis=1)
        rect[1] = pts[np.argmin(diff)]  # tr
        rect[3] = pts[np.argmax(diff)]  # bl
        return rect

    w_mm, h_mm = paper_size_mm
    if orientation == 'landscape':
        w_mm, h_mm = h_mm, w_mm
    px_per_mm = dpi / 25.4
    w_px = int(round(w_mm * px_per_mm))
    h_px = int(round(h_mm * px_per_mm))
    output_size = (w_px, h_px)

    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray, (5, 5), 0)
    edges = cv2.Canny(blur, 50, 150)

    contours_info = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    contours = contours_info[0] if len(contours_info) == 2 else contours_info[1]

    best_quad = None
    best_area = 0

    for cnt in contours:
        area = cv2.contourArea(cnt)
        if area < area_thresh:
            continue
        peri = cv2.arcLength(cnt, True)
        approx = cv2.approxPolyDP(cnt, 0.02 * peri, True)
        if len(approx) == 4 and area > best_area:
            best_area = area
            best_quad = approx.reshape(4, 2)

    if best_quad is None:
        return frame  # nichts gefunden

    src_rect = order_points(best_quad).astype("float32")
    w, h = output_size
    dst_rect = np.array([
        [0, 0],
        [w - 1, 0],
        [w - 1, h - 1],
        [0, h - 1]
    ], dtype="float32")

    M = cv2.getPerspectiveTransform(src_rect, dst_rect)
    warped = cv2.warpPerspective(frame, M, (w, h))

    cv2.polylines(frame, [best_quad.astype(int)], True, (0, 255, 0), 2)

    return warped

def thresholding_Otsu_frame(frame):
    if frame.ndim == 3 and frame.shape[2] == 3:
        if frame.dtype != np.uint8:
            tmp = cv2.normalize(frame, None, 0, 255, cv2.NORM_MINMAX).astype(np.uint8)
        else:
            tmp = frame
        gray_frame = cv2.cvtColor(tmp, cv2.COLOR_BGR2GRAY)
    elif frame.ndim == 2:
        if frame.dtype != np.uint8:
            gray_frame = cv2.normalize(frame, None, 0, 255, cv2.NORM_MINMAX).astype(np.uint8)
        else:
            gray_frame = frame.copy()
    else:
        gray_frame = cv2.cvtColor(frame.astype(np.uint8), cv2.COLOR_BGR2GRAY)

    ret, otsu_thresh_frame = cv2.threshold(
        gray_frame, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
    return otsu_thresh_frame

def distanceTransform_frame(frame):
    dist = cv2.distanceTransform(frame, cv2.DIST_L2, 5)
    return dist

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

#old: not used
def coinDetection_frame(frame, orig_color=None):
    if orig_color is None:
        orig_color = frame.copy()

    thresh_frame = thresholding_Otsu_frame(frame)

    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (3, 3))
    sure_bg = cv2.dilate(thresh_frame, kernel, iterations=3)
    distance = distanceTransform_frame(thresh_frame)
    ret, sure_fg = cv2.threshold(distance, 0.5 * distance.max(), 255, cv2.THRESH_BINARY)
    sure_fg = sure_fg.astype(np.uint8)
    unknown = cv2.subtract(sure_bg, sure_fg)

    ret, markers = cv2.connectedComponents(sure_fg)
    markers += 1
    markers[unknown == 255] = 0

    watershed_frame = watershedAlgorithm_frame(thresh_frame, markers, src_color=orig_color)
    return watershed_frame

def classify_coins_bayes_frame(frame, paper_size_mm=(210, 297), priors=None, sigma_mm=0.8):
    thresh = thresholding_Otsu_frame(frame)
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (3, 3))
    sure_bg = cv2.dilate(thresh, kernel, iterations=3)
    dist = distanceTransform_frame(thresh)
    ret, sure_fg = cv2.threshold(dist, 0.5 * dist.max(), 255, cv2.THRESH_BINARY)
    sure_fg = sure_fg.astype(np.uint8)
    unknown = cv2.subtract(sure_bg, sure_fg)

    ret, markers = cv2.connectedComponents(sure_fg)
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
        if s <= 0:
            priors = {c: 1.0 / len(classes) for c in classes}
        else:
            priors = {c: priors.get(c, 0.0) / s for c in classes}

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

        posteriors = {}
        for c in classes:
            mu = coin_diameters_mm[c]
            var = sigma_mm * sigma_mm
            likelihood = (1.0 / math.sqrt(2.0 * math.pi * var)) * math.exp(-0.5 * ((diameter_mm - mu) ** 2) / var)
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

def cameraCapture():
    cam = cv2.VideoCapture(0)
    if not cam.isOpened():
        print("Kamera konnte nicht geöffnet werden")
        return

    while True:
        ret, frame = cam.read()
        if not ret:
            print("Frame konnte nicht gelesen werden")
            break

        marked, dst = process_frame_harris(frame, blockSize=2, ksize=3, k=0.04, thresh=0.01)
        frame_hough = process_frame_hough(marked)
        homography_result = process_homography_frame(frame_hough, paper_size_mm=(210,297), dpi=150, orientation='portrait')

        annotated_homography, total_chf = classify_coins_bayes_frame(homography_result, paper_size_mm=(210, 297))

        display = frame.copy()
        cv2.putText(display, f"Total: {total_chf:.2f} CHF", (10, 30),
                    cv2.FONT_HERSHEY_SIMPLEX, 1.0, (0, 255, 255), 2, cv2.LINE_AA)

        cv2.imshow('Live Camera', display)
        cv2.imshow('Coins (homography)', annotated_homography)

        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
    cam.release()
    cv2.destroyAllWindows()

def main():
    cameraCapture()

if __name__ == "__main__":
    main()



