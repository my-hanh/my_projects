import cv2
import matplotlib.pyplot as plt

img = cv2.imread("rgb-apple.jpg")  # BGR in OpenCV
plt.imshow(img)                # expects RGB
plt.show()