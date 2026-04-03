from skimage import io, exposure, img_as_float
import matplotlib.pyplot as plt
import numpy as np

image_path_1 = "pics/ctSkull.tif"
image_path_2 = "pics/xRayChest.tif"

image1 = io.imread(image_path_1).squeeze()
image2 = io.imread(image_path_2).squeeze()

gamma = np.array([0.125, 0.25, 0.5, 1., 2., 4., 8.])
r = np.linspace(0, 1, 200) 


# gamma correction for image1
fig, axes = plt.subplots(1, len(gamma) + 1, figsize=(15, 5))
axes[0].imshow(image1, cmap='gray')
axes[0].set_title("Original")
axes[0].axis('off')

for i, g in enumerate(gamma):
    gamma_corrected = exposure.adjust_gamma(image1, g)
    axes[i + 1].imshow(gamma_corrected, cmap='gray')
    axes[i + 1].set_title(f"Gamma={g}")
    axes[i + 1].axis('off')

plt.tight_layout()
plt.show()

# gamma correction for image2
fig, axes = plt.subplots(1, len(gamma) + 1, figsize=(15, 5))
axes[0].imshow(image2, cmap='gray')
axes[0].set_title("Original")
axes[0].axis('off')

for i, g in enumerate(gamma):
    gamma_corrected = exposure.adjust_gamma(image2, g)
    axes[i + 1].imshow(gamma_corrected, cmap='gray')
    axes[i + 1].set_title(f"Gamma={g}")
    axes[i + 1].axis('off')

plt.tight_layout()
plt.show()




# Built-in Function gamma correction using scikit-image
gamma_value = 2.0  
gamma_corrected_builtin = exposure.adjust_gamma(image1, gamma_value)

# Manual implementation of gamma correction
image_float = img_as_float(image1)
gamma_corrected_manual = np.power(image_float, gamma_value)

fig, axes = plt.subplots(1, 3, figsize=(12, 4))
axes[0].imshow(image1, cmap='gray')
axes[0].set_title("Original")
axes[0].axis('off')

axes[1].imshow(gamma_corrected_builtin, cmap='gray')
axes[1].set_title("Built-in Gamma (gamma=2.0)")
axes[1].axis('off')

axes[2].imshow(gamma_corrected_manual, cmap='gray')
axes[2].set_title("Manual Gamma (gamma=2.0)")
axes[2].axis('off')

plt.tight_layout()
plt.show()
