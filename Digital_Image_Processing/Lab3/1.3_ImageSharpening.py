import numpy as np
import cv2
import matplotlib.pyplot as plt


image_path1 = "pics/bloodCells.tif"
image1 = cv2.cvtColor(cv2.imread(image_path1), cv2.COLOR_BGR2RGB)

h1 = np.array([[1, 1, 1], [1, -4, 1], [0, 1, 0]]) 
h2 = np.array([[1, 1, 1], [1, -8, 1], [1, 1, 1]]) 

lap1 = cv2.filter2D(image1, -1, h1, borderType=cv2.BORDER_REFLECT)
lap2 = cv2.filter2D(image1, -1, h2, borderType=cv2.BORDER_REFLECT)

fig, ax = plt.subplots(2, 3)
ax[0, 0].imshow(image1)
ax[0, 0].set_title("Original")
ax[0, 0].axis("off")

ax[0, 1].imshow(lap1)
ax[0, 1].set_title("Laplace filter 1")
ax[0, 1].axis("off")

ax[0, 2].imshow(lap2)
ax[0, 2].set_title("Laplace filter 2")
ax[0, 2].axis("off")

alpha = 0.5
sharp1 = cv2.addWeighted(image1.astype(np.float32), 1.0, lap1.astype(np.float32), -alpha, 0)
sharp2 = cv2.addWeighted(image1.astype(np.float32), 1.0, lap2.astype(np.float32), -alpha, 0)

ax[1, 0].imshow(image1)
ax[1, 0].set_title("Original")
ax[1, 0].axis("off")

ax[1, 1].imshow(sharp1)
ax[1, 1].set_title("Sharpened Image 1")
ax[1, 1].axis("off")

ax[1, 2].imshow(sharp2)
ax[1, 2].set_title("Sharpened Image 2")
ax[1, 2].axis("off")

plt.tight_layout()
plt.show()