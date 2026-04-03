import cv2 as cv
import numpy as np
import matplotlib.pyplot as plt
from matplotlib import colormaps

image_path1 = "pics/arterie.tif"
image1 = cv.imread(image_path1, cv.IMREAD_GRAYSCALE)

image_path2 = "pics/ctSkull.tif"
image2 = cv.imread(image_path2, cv.IMREAD_GRAYSCALE)

# gray level histogram
hist1 = cv.calcHist([image1], [0], None, [256], [0, 256])
hist2 = cv.calcHist([image2], [0], None, [256], [0, 256])

plt.figure(figsize=(8, 5))
plt.title('Gray Level Histogram')

plt.subplot(1, 2, 1)
plt.hist(image1.ravel(), bins=256, range=[0,256])
plt.xlabel('Gray Level (0–255)')
plt.ylabel('Number of Pixels')
plt.xlim([0, 256])
plt.grid(True, linestyle='--', alpha=0.5)

plt.subplot(1, 2, 2)
plt.hist(image2.ravel(), bins=256, range=[0,256])
plt.xlabel('Gray Level (0–255)')
plt.ylabel('Number of Pixels')
plt.xlim([0, 256])
plt.grid(True, linestyle='--', alpha=0.5)

plt.show()

cdf1 = hist1.cumsum()   # cumulative distribution function
cdf2 = hist2.cumsum()
total_pixels1 = cdf1[-1]
total_pixels2 = cdf2[-1]

# Color Map
k = 8 # number of colors
colormap_name = "inferno"
cmap = plt.colormaps[colormap_name]
colors = (cmap(np.linspace(0, 1, k))[:, :3] * 255).astype(np.uint8)

# equal-frequancy bins
thresholds1 = []
pixels_per_interval = total_pixels1/k

for i in range (1, k):
    target_pixel_count = i * pixels_per_interval
    thresholds_gray_value = np.searchsorted(cdf1, target_pixel_count)
    thresholds1.append(thresholds_gray_value)

thresholds2 = []
pixels_per_interval = total_pixels2/k

for i in range (1, k):
    target_pixel_count = i * pixels_per_interval
    thresholds_gray_value = np.searchsorted(cdf2, target_pixel_count)
    thresholds2.append(thresholds_gray_value)

# color-coded image
color_img1 = np.zeros((*image1.shape, 3), dtype=np.uint8)

prev_t1 = 0
for i, t in enumerate(thresholds1 + [256]):
    mask = (image1 >= prev_t1) & (image1 <= t - 1)   # boolean Maske
    color_img1[mask] = colors[i]
    prev_t1 = t

color_img2 = np.zeros((*image2.shape, 3), dtype=np.uint8)

prev_t2 = 0
for i, t in enumerate(thresholds2 + [256]):
    mask = (image2 >= prev_t2) & (image2 <= t - 1)   # boolean Maske
    color_img2[mask] = colors[i]
    prev_t2 = t


plt.subplot(2, 2, 1)
plt.title('Original Grayscale Image')
plt.imshow(image1, cmap='gray')
plt.axis('off')

plt.subplot(2, 2, 2)
plt.title(f'Color Coded Image ({colormap_name}, {k} colors)')
plt.imshow(cv.cvtColor(color_img1, cv.COLOR_BGR2RGB))
plt.axis('off')

plt.subplot(2, 2, 3)
plt.title('Original Grayscale Image')
plt.imshow(image2, cmap='gray')
plt.axis('off')

plt.subplot(2, 2, 4)
plt.title(f'Color Coded Image ({colormap_name}, {k} colors)')
plt.imshow(cv.cvtColor(color_img2, cv.COLOR_BGR2RGB))
plt.axis('off')

plt.show()