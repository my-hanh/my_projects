import matplotlib.pyplot as plt
from skimage import io, color
import numpy as np
from skimage.filters import threshold_otsu

# Load the AVIF image
image = io.imread("happiness.avif")

# Convert RGB → Grayscale
gray_image = color.rgb2gray(image)

# Otsu thresholding
thresh = threshold_otsu(gray_image)
binary = (gray_image > thresh).astype(np.uint8) * 255  # Values: 0 or 255

# Plot histogram of the binary image
plt.figure(figsize=(8, 4))
plt.title("Binary Image Histogram")
plt.xlabel("Pixel Value")
plt.ylabel("Frequency")
plt.hist(binary.ravel(), bins=256, range=(0, 256), color="black")
plt.tight_layout()
plt.show()
