import numpy as np
import cv2
import matplotlib.pyplot as plt


image_path1 = "pics/cellsSandP.tif"
image1 = cv2.cvtColor(cv2.imread(image_path1), cv2.COLOR_BGR2RGB)

h_M = np.array([[1, 1, 1], [1, 1, 1], [1, 1, 1]]) * (1/9)

avg_3x3 = cv2.filter2D(image1, -1, h_M, borderType=cv2.BORDER_REFLECT)

median_filtered = cv2.medianBlur(image1, 3)

fig, ax = plt.subplots(1, 3, figsize=(12, 4))
ax[0].imshow(image1)
ax[0].set_title("Original")
ax[0].axis("off")

ax[1].imshow(avg_3x3)
ax[1].set_title("3x3 Mean Filter")
ax[1].axis("off")

ax[2].imshow(median_filtered)
ax[2].set_title("Median Filter")
ax[2].axis("off")

plt.tight_layout()
plt.show()