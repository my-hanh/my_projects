import cv2 as cv
import numpy as np
import matplotlib.pyplot as plt


# ---------- Non-maximum Suppression------------
image_path = "arterie.jpg"
image_bgr = cv.imread(image_path)

image_gray = cv.cvtColor(image_bgr, cv.COLOR_BGR2GRAY).astype(np.float64) / 255.0

# smooth image with Gaussian low pass filter 7x7 and sigma=4.0
blur = cv.GaussianBlur(image_gray, ksize=(7,7), sigmaX=4.0, sigmaY=4.0)

# Sobel gradients
Sobel_kernel_x = np.array(([1, 0, -1],
                           [2, 0, -2],
                           [1, 0, 1]))
Sobel_kernel_y = np.array(([1, 2, 1],
                           [0, 0, 0],
                           [-1, -2, -1]))

G_x = cv.filter2D(blur, cv.CV_64F, Sobel_kernel_x)
G_y = cv.filter2D(blur, cv.CV_64F, Sobel_kernel_y)
G = np.hypot(G_x, G_y)
theta = (np.rad2deg(np.arctan2(G_y, G_x)) + 180) % 180  # angle in [0,180)

# non-maximum suppression
# Quantize gradient directions to 0,45,90,135
ang_q = np.zeros_like(theta, dtype=np.uint8)
ang_q[((0 <= theta) & (theta < 22.5)) | ((157.5 <= theta) & (theta < 180))] = 0
ang_q[(22.5 <= theta) & (theta < 67.5)]   = 45
ang_q[(67.5 <= theta) & (theta < 112.5)]  = 90
ang_q[(112.5 <= theta) & (theta < 157.5)] = 135

H, W = G.shape
G_nms = np.zeros_like(G, dtype=np.float64) #non-maximum suppression

for y in range(1, H - 1):
    for x in range(1, W - 1):
        m = G[y, x]
        direction = ang_q[y, x]

        if direction == 0:
            m1, m2 = G[y, x - 1], G[y, x + 1]
        elif direction == 45:
            m1, m2 = G[y - 1, x + 1], G[y + 1, x - 1]
        elif direction == 90:
            m1, m2 = G[y - 1, x], G[y + 1, x]
        else:  # 135
            m1, m2 = G[y - 1, x - 1], G[y + 1, x + 1]

        if m >= m1 and m >= m2:
            G_nms[y, x] = m
        # else remains 0 (suppressed)

# normalize for display
G_disp     = (G / (G.max() + 1e-12)).astype(np.float32)
G_nms_disp = (G_nms / (G_nms.max() + 1e-12)).astype(np.float32)

print("Done. Arrays ready:")
print(" Gx shape:", G_x.shape, "| Gy shape:", G_y.shape)
print(" G magnitude range:", (G.min(), G.max()))
print(" G_nms magnitude range:", (G_nms.min(), G_nms.max()))

fig, axes = plt.subplots(1, 2, figsize=(12, 6))
axes[0].imshow(G_disp, cmap='gray')
axes[0].set_title("Gradient magnitude")
axes[0].axis('off')
axes[1].imshow(G_nms_disp, cmap='gray')
axes[1].set_title("After NMS")
axes[1].axis('off')
plt.tight_layout()
plt.show()


# ---------- Post-processing: hysteresis thresholding ------------
Gn = (G_nms - G_nms.min()) / (G_nms.max() - G_nms.min() + 1e-12)
th1 = 0.30
th2 = 0.01  # choose th2 < th1 (handout suggests a small value)

strong = Gn >= th1           # strong ridge points (seed set)

# Forward pass (upper-left -> lower-right)
# Neighborhood: P(x+1, y), P(x+1, y+1), P(x, y+1), P(x+1, y-1)
H, W = Gn.shape
hyst = strong.copy()

for y in range(H):
    for x in range(W):
        if hyst[y, x]:
            # (x+1, y)
            if x + 1 < W and Gn[y, x + 1] >= th2:
                hyst[y, x + 1] = True
            # (x+1, y+1)
            if x + 1 < W and y + 1 < H and Gn[y + 1, x + 1] >= th2:
                hyst[y + 1, x + 1] = True
            # (x, y+1)
            if y + 1 < H and Gn[y + 1, x] >= th2:
                hyst[y + 1, x] = True
            # (x+1, y-1)
            if x + 1 < W and y - 1 >= 0 and Gn[y - 1, x + 1] >= th2:
                hyst[y - 1, x + 1] = True


# Backward pass (lower-right -> upper-left)
# Neighborhood: P(x, y-1), P(x-1, y-1), P(x-1, y), P(x-1, y+1)

# Binary hysteresis result (0/255) for visualization or saving
hyst_u8 = (hyst.astype(np.uint8) * 255)

# Compare to Canny
#    OpenCV Canny expects 8-bit input and thresholds in [0,255].
#    We map th2 (low) and th1 (high) from [0,1] -> [0,255].
canny = cv.Canny((image_gray * 255).astype(np.uint8),
                 threshold1=int(th2 * 255),
                 threshold2=int(th1 * 255))

fig2, ax = plt.subplots(1, 2, figsize=(12, 6))
ax[0].imshow(hyst_u8, cmap='gray')
ax[0].set_title("Hysteresis")
ax[0].axis('off')
ax[1].imshow(canny, cmap='gray')
ax[1].set_title("Canny")
ax[1].axis('off')
plt.tight_layout()
plt.show()