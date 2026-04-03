import numpy as np
import matplotlib.pyplot as plt
import cv2 as cv
import skimage.segmentation
import scipy.ndimage

image_path = "pics/bloodCells.png"
image = cv.imread(image_path, cv.IMREAD_GRAYSCALE)

_, binary_image = cv.threshold(image, 0, 255, cv.THRESH_BINARY + cv.THRESH_OTSU)
binary_image = (binary_image > 0).astype(np.uint8)

cleared_border_image = skimage.segmentation.clear_border(binary_image)

#fill holes to get full cells
Ic = cv.bitwise_not(cleared_border_image)
B = np.array([[0,1,0],
                [1,1,1],
                [0,1,0]], dtype=np.uint8)
X = np.zeros_like(Ic, np.uint8)
X[0, :] = Ic[0, :]
X[-1, :] = Ic[-1, :]
X[:, 0] = Ic[:, 0]
X[:, -1] = Ic[:, -1]

# Iterate: X_k = (X_{k-1} ⊕ B) ∩ Ic
while True:
    X_prev = X
    X = cv.dilate(X, B)
    X = cv.bitwise_and(X, Ic)
    if np.array_equal(X, X_prev):
        break

holes  = cv.subtract(Ic, X)          # holes are background not connected to border
filled_cells_image = cv.bitwise_or(cleared_border_image, holes)     # add holes back to the objects

labeled_array = scipy.ndimage.label(filled_cells_image)
count = labeled_array[1]

areas = np.bincount(labeled_array[0].ravel())[1:]  # skip background count

print (f"Number of blood cells counted: {count}")
print(f"Areas of the blood cells: {areas}")

plt.figure(figsize=(12, 4))
plt.subplot(1, 3, 1)
plt.imshow(image, cmap='gray')
plt.title('Original Image')
plt.axis('off')

plt.subplot(1, 3, 2)
plt.imshow(binary_image, cmap='gray')
plt.title(f'Binary Image')
plt.axis('off')

plt.subplot(1, 3, 3)
plt.imshow(filled_cells_image, cmap='gray')
plt.title(f'Final Image with Filled Cells\nCount: {count}')
plt.axis('off')

plt.tight_layout()
plt.show()

# --- histogram of sizes ---
plt.figure(figsize=(6,4))
plt.hist(areas, bins='auto')
plt.xlabel('Cell size (pixels)')
plt.ylabel('Count')
plt.title('Distribution of Cell Sizes')
plt.tight_layout()
plt.show()