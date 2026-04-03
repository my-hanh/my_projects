from pathlib import Path
import numpy as np
import matplotlib.pyplot as plt
import scipy
from scipy import ndimage, fft
from PIL import Image

# ----------------------------------------------------- load image ---------------------------------------------
file_name = Path('.', 'pics', 'blurred_image.jpg')

# Open the image with pillow and convert to numpy array
img = Image.open(file_name)
img = np.asarray(img.convert('L'))

# Guessed motion blur filter
nFilter = 91
angle = 45
my_filter = np.zeros((nFilter, nFilter))
my_filter[nFilter//2, :] = 1.0 / nFilter
my_filter = scipy.ndimage.rotate(my_filter, angle, reshape=False)

nRows = img.shape[0]
nCols = img.shape[1]
nFFT = 1024
filter_spectrum = scipy.fft.fft2(my_filter, (nFFT, nFFT))
image_blurred_spectrum = scipy.fft.fft2(img, (nFFT, nFFT))

# --------------------------------------------------- reconstruct the image --------------------------------------------
K = 0.01
h_wiener_ft = np.conj(filter_spectrum)/(np.abs(filter_spectrum)**2 + K)
reconstructed_img_ft = scipy.fft.ifft2(image_blurred_spectrum * h_wiener_ft)
reconstructed_img = np.real(reconstructed_img_ft)

#reconstructed_img = reconstructed_img[nFilter // 2 - 1 : img.shape[0] + nFilter // 2 - 1,
#                                      nFilter // 2 - 1 : img.shape[1] + nFilter // 2 - 1]

reconstructed_img = reconstructed_img[:img.shape[0],: img.shape[1]]  # Use this due to cropped supposedly used in image generation



# --------------------------------------------------------- display images ---------------------------------------------
fig = plt.figure(1)
plt.subplot(3, 1, 1)
plt.title('Blurred Image')
plt.imshow(img, cmap='gray')

plt.subplot(3, 1, 2)
plt.title('Guessed Motion Blur Filter')
plt.imshow(my_filter, cmap='gray')

plt.subplot(3, 1, 3)
plt.title('Reconstructed Image')
plt.imshow(reconstructed_img, vmin=np.min(reconstructed_img), vmax=np.max(reconstructed_img), cmap='gray')

plt.show()