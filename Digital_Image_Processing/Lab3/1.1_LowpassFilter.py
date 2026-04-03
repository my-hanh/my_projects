import numpy as np
import cv2
import matplotlib.pyplot as plt


image_path1 = "pics/bloodCells.tif"
image_path2 = "pics/cat.tif"

image1 = cv2.cvtColor(cv2.imread(image_path1), cv2.COLOR_BGR2RGB)
image2 = cv2.cvtColor(cv2.imread(image_path2), cv2.COLOR_BGR2RGB)

h_M = np.array([[1, 1, 1], [1, 1, 1], [1, 1, 1]]) * (1/9)
avg_3x3_1 = cv2.filter2D(image1, -1, h_M, borderType=cv2.BORDER_REFLECT)
avg_3x3_2 = cv2.filter2D(image2, -1, h_M, borderType=cv2.BORDER_REFLECT)


sigma = 0.85
h_G = np.array([[1, 2, 1], [2, 4, 2], [1, 2, 1]]) * (1/16)
gau_3x3_1 = cv2.filter2D(image1, -1, h_G, borderType=cv2.BORDER_REFLECT)
gau_3x3_2 = cv2.filter2D(image2, -1, h_G, borderType=cv2.BORDER_REFLECT)

fig, ax = plt.subplots(2, 3, figsize=(12, 4))
ax[0, 0].imshow(image1)
ax[0, 0].set_title("Original")
ax[0, 0].axis("off")

ax[0, 1].imshow(avg_3x3_1)
ax[0, 1].set_title("3x3 Mean Filter")
ax[0, 1].axis("off")

ax[0, 2].imshow(gau_3x3_1)
ax[0, 2].set_title("3x3 Gaussian Filter")
ax[0, 2].axis("off")

ax[1, 0].imshow(image2)
ax[1, 0].set_title("Original")
ax[1, 0].axis("off")

ax[1, 1].imshow(avg_3x3_2)
ax[1, 1].set_title("3x3 Mean Filter")
ax[1, 1].axis("off")

ax[1, 2].imshow(gau_3x3_2)
ax[1, 2].set_title("3x3 Gaussian Filter")
ax[1, 2].axis("off")

plt.tight_layout()
plt.show()