import numpy as np
import cv2
import matplotlib.pyplot as plt


image_path1 = "pics/bloodCells.tif"
image1 = cv2.cvtColor(cv2.imread(image_path1), cv2.COLOR_BGR2RGB)

h_M = np.array([[1, 1, 1], [1, 1, 1], [1, 1, 1]]) * (1/9)

sigma = 0.85
h_G = np.array([[1, 2, 1], [2, 4, 2], [1, 2, 1]]) * (1/16)

low_avg = cv2.blur(image1, (3,3), borderType=cv2.BORDER_REFLECT)
low_gau = cv2.GaussianBlur(image1, (3,3), sigmaX=0.85, sigmaY=0.85, borderType=cv2.BORDER_REFLECT)

high_avg = image1 - low_avg         
high_gau = image1 - low_gau         

fig, ax = plt.subplots(1, 3, figsize=(12, 4))
ax[0].imshow(image1)
ax[0].set_title("Original")
ax[0].axis("off")

ax[1].imshow(high_avg)
ax[1].set_title("3x3 Mean Filter")
ax[1].axis("off")

ax[2].imshow(high_gau)
ax[2].set_title("3x3 Gaussian Filter")
ax[2].axis("off")

plt.tight_layout()
plt.show()