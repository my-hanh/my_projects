import cv2 as cv
import numpy as np
import matplotlib.pyplot as plt


image_path = "reallyBlack.png"
bgr_image = cv.imread(image_path)
rgb_image = cv.cvtColor(bgr_image, cv.COLOR_BGR2RGB)

# Parameters
gamma_all = 0.4   # gamma for all channels (<1 brightens, >1 darkens)
single_channel = 0  # 0=R, 1=G, 2=B (in RGB order below)

# Same γ-correction to each RGB channel
inv_all = 1.0 / gamma_all
lut_all = np.array([((i / 255.0) ** gamma_all) * 255.0 for i in range(256)], dtype=np.uint8)
rgb_gamma_all = cv.LUT(rgb_image, lut_all)

# Compare before and after γ-correction
fig1, axes1 = plt.subplots(1, 2, figsize=(12, 5))
fig1.suptitle(f"(a)-(c) Original vs Gamma-corrected (γ={gamma_all})", fontsize=14)

axes1[0].imshow(rgb_image)
axes1[0].set_title("Original")
axes1[0].axis('off')

axes1[1].imshow(rgb_gamma_all)
axes1[1].set_title("Gamma (all channels)")
axes1[1].axis('off')

plt.tight_layout()
plt.show()