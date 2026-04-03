import cv2 as cv
import numpy as np
import matplotlib.pyplot as plt

# (a) Load the RGB image
image_path = "pics/landscape_1.png"
bgr_image = cv.imread(image_path)
rgb_image = cv.cvtColor(bgr_image, cv.COLOR_BGR2RGB)

"""
RGB is the preferred color space for visualization 
(same as how the human eye perceives images).
"""

# Parameters
gamma_all = 0.6   # (b) gamma for all channels (<1 brightens, >1 darkens)
gamma_one = 0.6   # (d) gamma for one channel
single_channel = 0  # 0=R, 1=G, 2=B (in RGB order below)

# (b) Same γ-correction to each RGB channel
inv_all = 1.0 / gamma_all
lut_all = np.array([((i / 255.0) ** gamma_all) * 255.0 for i in range(256)], dtype=np.uint8)
rgb_gamma_all = cv.LUT(rgb_image, lut_all)

# (c) Compare before and after γ-correction
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

"""
- Gamma (γ) controls the overall brightness.
      If γ < 1, image becomes brighter.
      If γ > 1, image becomes darker.
- Applied identically to R, G, and B channels.
--> The overall color balance is preserved since all channels were changed equally.
(without distorting color balance)
"""

# (d) Apply γ-correction to only ONE channel
inv_one = 1.0 / gamma_one
lut_one = np.array([((i / 255.0) ** inv_one) * 255.0 for i in range(256)], dtype=np.uint8)

rgb_gamma_single = rgb_image.copy()
channels = list(cv.split(rgb_gamma_single))   # [R, G, B] in this RGB array
channels[single_channel] = cv.LUT(channels[single_channel], lut_one)
rgb_gamma_single = cv.merge(channels)

fig2, axes2 = plt.subplots(1, 2, figsize=(16, 5))
chan_name = ["R", "G", "B"][single_channel]
fig2.suptitle(f"(d) γ on {chan_name} channel only (γ={gamma_one})", fontsize=14)

axes2[0].imshow(rgb_image)
axes2[0].set_title("Original")
axes2[0].axis('off')

axes2[1].imshow(rgb_gamma_single)
axes2[1].set_title(f"Gamma on {chan_name} only")
axes2[1].axis('off')

plt.tight_layout()
plt.show()

"""
Adjusting gamma on only one channel distorts color balance.
"""

# (e) Histogram equalization per RGB channel
r, g, b = cv.split(rgb_image)
r_eq = cv.equalizeHist(r)
g_eq = cv.equalizeHist(g)
b_eq = cv.equalizeHist(b)
rgb_eq = cv.merge([r_eq, g_eq, b_eq])

"""
Equalization per channel improves contrast but can distort color fidelity.
"""

# (f) Compare before and after histogram equalization
fig3, axes3 = plt.subplots(1, 2, figsize=(12, 5))
fig3.suptitle("(e)-(f) Original vs Per-channel Histogram Equalization (RGB)", fontsize=14)

axes3[0].imshow(rgb_image)
axes3[0].set_title("Original")
axes3[0].axis('off')

axes3[1].imshow(rgb_eq)
axes3[1].set_title("Equalized per channel")
axes3[1].axis('off')

plt.tight_layout()
plt.show()

"""
Histogram equalization effectively increases contrast.
"""
