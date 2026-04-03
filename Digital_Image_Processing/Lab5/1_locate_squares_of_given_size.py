import numpy as np
import matplotlib.pyplot as plt
import cv2 as cv

image_path = "pics/squares.png"
image = cv.imread(image_path, cv.IMREAD_GRAYSCALE)

# 1. morphological operations to locate squares of size 5x5

# Define structuring elements
se4 = np.ones((4,4), np.uint8)
se6 = np.ones((6,6), np.uint8)

# Erode and dilate with 4×4 kernel → keeps all objects ≥4×4
opened_4 = cv.dilate(cv.erode(image, se4), se4)

# Erode and dilate with 6×6 kernel → keeps all objects ≥6×6
opened_6 = cv.dilate(cv.erode(image, se6), se6)

# Subtract → isolates exactly 5×5 squares
only_5x5 = cv.subtract(opened_4, opened_6)

plt.figure(figsize=(12, 4))
plt.subplot(1, 2, 1)
plt.imshow(image, cmap='gray')
plt.title('Original Image')
plt.axis('off')

plt.subplot(1, 2, 2)
plt.imshow(only_5x5, cmap='gray')
plt.title(f'Located 5x5 Squares')
plt.axis('off')

plt.tight_layout()
plt.show()

# 2. Find and count squares 5x5
# --- iterative component growth (no connectedComponents) ---
B = np.array([[0,1,0],
              [1,1,1],
              [0,1,0]], dtype=np.uint8)

_, I = cv.threshold(only_5x5, 127, 255, cv.THRESH_BINARY)
I = (I > 0).astype(np.uint8)  # binary image
count = 0

while True:
    #pick a seed pixel
    ys, xs = np.where(I == 1)
    if len(xs) == 0:
        break  # no more squares to find

    y0, x0 = ys[0], xs[0]
    Xk = np.zeros_like(I)
    Xk[y0, x0] = 1  # seed pixel

    # Iterative growth (dilation & intersection)
    while True:
        Xk_next = cv.dilate(Xk, B)
        Xk_next = cv.bitwise_and(Xk_next, I)

        if np.array_equal(Xk_next, Xk):
            break  # convergence
        Xk = Xk_next

    # Remove found square from I
    I[Xk == 1] = 0

    #check size
    if np.count_nonzero(Xk) == 25:
        count += 1

print(f"Number of 5x5 squares found: {count}")

