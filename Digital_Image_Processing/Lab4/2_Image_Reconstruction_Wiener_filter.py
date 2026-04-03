from pathlib import Path
import numpy as np
import matplotlib.pyplot as plt
import scipy
from scipy.signal import wiener
from PIL import Image

# ----------------------------------------------------- load image -----------------------------------------------------
img = np.asarray(Image.open(Path('.', 'pics', 'blurred_image.jpg')).convert('L'),
                 dtype=np.float32) / 255.0

# summarize some details about the image
print(img.shape)

# -------------------------------------------- Generate the motion blur filter -----------------------------------------
nFilter = 91
angle = 45
h = np.zeros((nFilter, nFilter))
h[nFilter//2, :] = 1.0 / nFilter
h = scipy.ndimage.rotate(h, angle, reshape=False)

# -------------------------------------------- Wiener deconvolution (with K) own implementation ----------------------------------------------------------
K = 0.01  # noise-to-signal power ratio

def wiener_deconv(image, psf, K):
    #psf0 = np.fft.ifftshift(psf)     # PSF center -> (0,0)
    psf0 = psf / psf.sum()
    H = np.fft.fft2(psf0, s=image.shape)
    G = np.fft.fft2(image)
    Hc = np.conj(H)
    Fhat = (Hc / (Hc*H + K + 1e-12)) * G
    f = np.real(np.fft.ifft2(Fhat))
    return np.clip(f, 0, 1)

deblurred_img = wiener_deconv(img, h, K)

# -------------------------------------------- Wiener deconvolution (with K) using scipy ----------------------------------------------------------

deblurred_img_scipy = wiener(img, mysize=h.shape, noise=K)

# -------------------------------------------- Display images ----------------------------------------------------------
fig = plt.figure(1)
plt.subplot(141)
plt.title('Original Image')
plt.imshow(img, cmap='gray')
plt.axis('off')

plt.subplot(142)
plt.title('Motion Blur Filter')
plt.imshow(h, cmap='gray')
plt.axis('off')

plt.subplot(143)
plt.title(f'Deblurred Image (K={K})')
plt.imshow(deblurred_img, cmap='gray', vmin=0, vmax=1)
plt.axis('off')

plt.subplot(144)
plt.title('Wiener Deconvolution with scipy')
plt.imshow(deblurred_img_scipy, cmap='gray', vmin=0, vmax=1)
plt.axis('off')

plt.show()


