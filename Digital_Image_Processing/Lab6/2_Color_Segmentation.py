import cv2 as cv
import numpy as np
import matplotlib.pyplot as plt

image_path = "pics/brainCells.tif"
bgr_image = cv.imread(image_path)

#RGB
rgb_image = cv.cvtColor(bgr_image, cv.COLOR_BGR2RGB)
R,G,B = cv.split(rgb_image)

#CMY
cmy_image = 255 - rgb_image
C, M, Y = cv.split(cmy_image)

#HSI
    # Convert to float in [0,1]
rf = R.astype(np.float32) / 255.0
gf = G.astype(np.float32) / 255.0
bf = B.astype(np.float32) / 255.0

    # Intensity
I = (rf + gf + bf) / 3.0

    # Saturation: S = 1 - 3*min(R,G,B)/(R+G+B); handle zero denominator
min_rgb = np.minimum(np.minimum(rf, gf), bf)
sum_rgb = (rf + gf + bf)
S = np.zeros_like(I, dtype=np.float32)
mask = sum_rgb > 1e-8
S[mask] = 1.0 - (3.0 * min_rgb[mask] / sum_rgb[mask])
S = np.clip(S, 0.0, 1.0)

    # Hue:
    # theta = arccos( 0.5*((R-G)+(R-B)) / sqrt((R-G)^2 + (R-B)*(G-B)) )
num = 0.5 * ((rf - gf) + (rf - bf))
den = np.sqrt((rf - gf)**2 + (rf - bf)*(gf - bf)) + 1e-8
theta = np.arccos(np.clip(num / den, -1.0, 1.0))            # [0, pi]
H = theta.copy()
H[bf > gf] = (2.0 * np.pi) - H[bf > gf]                     # [0, 2pi)

    # For visualization, scale to 8-bit:
H_vis = np.uint8(np.clip((H / (2.0 * np.pi)) * 255.0, 0, 255))   # Hue 0..255
S_vis = np.uint8(np.clip(S * 255.0, 0, 255))                     # Sat 0..255
I_vis = np.uint8(np.clip(I * 255.0, 0, 255))                     # Intensity 0..255



fig, axes = plt.subplots(4, 3, figsize=(12, 12))

axes[0, 1].imshow(rgb_image)
axes[0, 1].set_title("Original RGB Image")
axes[0, 1].axis('off')

axes[1, 0].imshow(R, cmap='gray'); axes[1, 0].set_title("RGB - R")
axes[1, 1].imshow(G, cmap='gray'); axes[1, 1].set_title("RGB - G")
axes[1, 2].imshow(B, cmap='gray'); axes[1, 2].set_title("RGB - B")

axes[2, 0].imshow(C, cmap='gray'); axes[2, 0].set_title("CMY - C")
axes[2, 1].imshow(M, cmap='gray'); axes[2, 1].set_title("CMY - M")
axes[2, 2].imshow(Y, cmap='gray'); axes[2, 2].set_title("CMY - Y")

axes[3, 0].imshow(H_vis, cmap='gray'); axes[3, 0].set_title("HSI - H")
axes[3, 1].imshow(S_vis, cmap='gray'); axes[3, 1].set_title("HSI - S")
axes[3, 2].imshow(I_vis, cmap='gray'); axes[3, 2].set_title("HSI - I")

axes[0, 0].axis('off')
axes[0, 2].axis('off')

plt.tight_layout()
plt.show()


#(b) most suitable color space
"""
HSI separates Hue (color), Saturation (color purity), Intensity (brightness) 
→ thresholds are more robust to brightness changes
"""

# (c) Color Segmentation using HSI
# Select all pixels within a blue-ish hue range
H_low, H_high = 150, 190   # Hue range for blue (0–255)
S_min = 60                 # Minimum saturation
I_min = 25                 # Minimum intensity

# binary mask (white = selected pixels)
mask = (
    (H_vis >= H_low) &
    (H_vis <= H_high) &
    (S_vis >= S_min) &
    (I_vis >= I_min)
).astype(np.uint8) * 255

# (d) Display the segmentation result
plt.figure(figsize=(10, 4))
plt.subplot(1, 2, 1)
plt.imshow(rgb_image)
plt.title("Original RGB Image")
plt.axis("off")

plt.subplot(1, 2, 2)
plt.imshow(mask, cmap='gray')
plt.title("Segmentation Result (HSI)")
plt.axis("off")

plt.tight_layout()
plt.show()