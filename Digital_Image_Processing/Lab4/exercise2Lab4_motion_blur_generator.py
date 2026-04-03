from pathlib import Path
import numpy as np
import matplotlib.pyplot as plt
import scipy
from scipy import ndimage
from PIL import Image

# ----------------------------------------------------- load image -----------------------------------------------------
img = Image.open(Path('.', 'pics', 'MenInDesert.jpg'))
img = np.asarray(img.convert('L'))

# summarize some details about the image
print(img.shape)

# -------------------------------------------- Generate the motion blur filter -----------------------------------------
nFilter = 91
angle = 30
h = np.zeros((nFilter, nFilter))
h[nFilter//2, :] = 1.0 / nFilter
h = scipy.ndimage.rotate(h, angle, reshape=False)

# Motion blur via 2D-convolution
modified_img = scipy.signal.convolve2d(img, h, mode='same')

# -------------------------------------------- Display images ----------------------------------------------------------
fig = plt.figure(1)
plt.subplot(131)
plt.title('Original Image')
plt.imshow(img, cmap='gray')

plt.subplot(132)
plt.title('Motion Blur Filter')
plt.imshow(h, cmap='gray')

plt.subplot(133)
plt.title('Modified Image')
plt.imshow(modified_img, cmap='gray')
plt.show()
