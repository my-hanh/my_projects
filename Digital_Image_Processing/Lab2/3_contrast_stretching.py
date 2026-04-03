from skimage import io
import matplotlib.pyplot as plt
import numpy as np
from skimage import exposure

image_path = "pics/xRayChest.tif"
image = io.imread(image_path).squeeze()

L = 256
# Normalize the image to [0, 1]
image_norm = (image - image.min()) / (image.max() - image.min())

# Compute the CDF of the normalized image
hist, bins = np.histogram(image_norm.flatten(), bins=L, range=[0,1], density=True)
cdf = hist.cumsum()
cdf = cdf / cdf[-1]  # Normalize

# Use the CDF to map the old values to new values
image_equalized = np.interp(image_norm.flatten(), bins[:-1], cdf)
image_equalized = (image_equalized * (L-1)).astype(np.uint8).reshape(image.shape)



# Automatic histogram equalization using skimage
image_eq_auto = exposure.equalize_hist(image)
image_eq_auto_uint8 = (image_eq_auto * (L-1)).astype(np.uint8)




fig, axes = plt.subplots(2, 3, figsize=(15, 8))

axes[0, 0].imshow(image, cmap='gray')
axes[0, 0].set_title('Original Image')
axes[0, 0].axis('off')

axes[0, 1].imshow(image_equalized, cmap='gray')
axes[0, 1].set_title('Manual Histogram Equalization')
axes[0, 1].axis('off')

axes[0, 2].imshow(image_eq_auto_uint8, cmap='gray')
axes[0, 2].set_title('Auto Histogram Equalized')
axes[0, 2].axis('off')

axes[1, 0].hist(image.flatten(), bins=L, range=[0,L-1])
axes[1, 0].set_title('Original Histogram')
axes[1, 0].set_xlim([0, L-1])

axes[1, 1].hist(image_equalized.flatten(), bins=L, range=[0,L-1])
axes[1, 1].set_title('Manual Equalized Histogram')
axes[1, 1].set_xlim([0, L-1])

axes[1, 2].hist(image_eq_auto_uint8.flatten(), bins=L, range=[0,L-1])
axes[1, 2].set_title('Auto Equalized Histogram')
axes[1, 2].set_xlim([0, L-1])

plt.tight_layout()
plt.show()
