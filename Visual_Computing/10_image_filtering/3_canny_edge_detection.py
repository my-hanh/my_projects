import cv2
import numpy as np
import matplotlib.pyplot as plt

# ---------------------------------------------------------
# Parameters
# ---------------------------------------------------------
image_path = "coins.jpg"   # <--- put your coin image here
canny_low = 80             # lower hysteresis threshold
canny_high = 160           # upper hysteresis threshold
gaussian_sigma = 1.0       # smoothing for Canny pre-filter
# ---------------------------------------------------------


# 1. Load image (color + grayscale)
img_bgr = cv2.imread(image_path)
if img_bgr is None:
    raise FileNotFoundError(f"Could not read image: {image_path}")

img_rgb = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2RGB)
img_gray = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2GRAY)

# ---------------------------------------------------------
# 2. Sobel gradient filters
#    (3x3 kernels for x- and y-direction, for documentation)
# ---------------------------------------------------------

sobel_x_kernel = np.array(
    [[-1, 0, 1],
     [-2, 0, 2],
     [-1, 0, 1]], dtype=np.float32
)

sobel_y_kernel = np.array(
    [[-1, -2, -1],
     [ 0,  0,  0],
     [ 1,  2,  1]], dtype=np.float32
)

print("Sobel X kernel:\n", sobel_x_kernel)
print("Sobel Y kernel:\n", sobel_y_kernel)

# Using OpenCV's implementation (which uses 3x3 kernels by default)
sobel_x = cv2.Sobel(img_gray, cv2.CV_64F, 1, 0, ksize=3)
sobel_y = cv2.Sobel(img_gray, cv2.CV_64F, 0, 1, ksize=3)

sobel_mag = np.sqrt(sobel_x**2 + sobel_y**2)
sobel_mag = np.uint8(255 * sobel_mag / np.max(sobel_mag))

# ---------------------------------------------------------
# 3. Laplacian filter
#    (3x3 kernel for documentation)
# ---------------------------------------------------------

laplacian_kernel = np.array(
    [[0,  1, 0],
     [1, -4, 1],
     [0,  1, 0]], dtype=np.float32
)
print("Laplacian kernel (4-neighbor):\n", laplacian_kernel)

# Using OpenCV's Laplacian
laplacian = cv2.Laplacian(img_gray, cv2.CV_64F, ksize=3)
laplacian_abs = np.uint8(255 * np.abs(laplacian) / np.max(np.abs(laplacian)))

# ---------------------------------------------------------
# 4. Canny edge detector
#    Internally does:
#    - Gaussian smoothing
#    - gradient magnitude & direction
#    - non-maximum suppression
#    - double thresholding
#    - hysteresis tracking
# ---------------------------------------------------------

# Optional: explicit Gaussian smoothing before Canny
blurred = cv2.GaussianBlur(img_gray, (5, 5), gaussian_sigma)

edges_canny = cv2.Canny(blurred, threshold1=canny_low, threshold2=canny_high)

# ---------------------------------------------------------
# 5. Visualization
# ---------------------------------------------------------

fig, ax = plt.subplots(2, 3, figsize=(15, 10))

# Row 1
ax[0, 0].imshow(img_rgb)
ax[0, 0].set_title("Original RGB")
ax[0, 0].axis("off")

ax[0, 1].imshow(sobel_mag, cmap="gray")
ax[0, 1].set_title("Sobel Magnitude")
ax[0, 1].axis("off")

ax[0, 2].imshow(laplacian_abs, cmap="gray")
ax[0, 2].set_title("Laplacian")
ax[0, 2].axis("off")

# Row 2 – separate Sobel X/Y and Canny
ax[1, 0].imshow(np.uint8(np.absolute(sobel_x)), cmap="gray")
ax[1, 0].set_title("Sobel X")
ax[1, 0].axis("off")

ax[1, 1].imshow(np.uint8(np.absolute(sobel_y)), cmap="gray")
ax[1, 1].set_title("Sobel Y")
ax[1, 1].axis("off")

ax[1, 2].imshow(edges_canny, cmap="gray")
ax[1, 2].set_title("Canny Edges")
ax[1, 2].axis("off")

plt.tight_layout()
plt.show()
