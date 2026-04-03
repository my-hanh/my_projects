import numpy as np
import cv2
import matplotlib.pyplot as plt


image_path1 = "lena_noise.png"
image1 = cv2.cvtColor(cv2.imread(image_path1), cv2.COLOR_BGR2RGB)

h_M = np.array([[1, 1, 1], [1, 1, 1], [1, 1, 1]]) * (1/9)

median_filtered = cv2.medianBlur(image1, 3)

fig, ax = plt.subplots(1, 2, figsize=(12, 4))
ax[0].imshow(image1)
ax[0].set_title("Original")
ax[0].axis("off")

ax[1].imshow(median_filtered)
ax[1].set_title("Median Filter")
ax[1].axis("off")

plt.tight_layout()
plt.show()