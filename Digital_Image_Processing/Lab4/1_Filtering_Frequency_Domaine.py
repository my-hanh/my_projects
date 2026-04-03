from pathlib import Path
import numpy as np
import matplotlib.pyplot as plt
import scipy
import scipy.fft
import scipy.signal
from scipy import ndimage, fft
import cv2 as cv
import time

image_path = "pics/MenInDesert.jpg"
image = cv.imread(image_path, cv.IMREAD_GRAYSCALE)

#2.) 2D fft
height, width = image.shape[:2]
print(f"Image Dimensions HxW: {height} x {width}, dtype={image.dtype}")

F_fft2_image = scipy.fft.fft2(image)
print(f"fft_2D_image data type: {F_fft2_image.dtype}")   #complex128

"""
numpy.complex128 is represented by two 64-bit floats (real and imaginary parts)
"""

#3.) original image + magnitude of fft (liner & log)
F_center = scipy.fft.fftshift(F_fft2_image) #shift DC to center

magnitude = np.abs(F_center)

magnitude_linear = magnitude/magnitude.max()

magnitude_log = np.log1p(magnitude)
magnitude_log /= magnitude_log.max()

plt.figure(figsize=(13,4))

plt.subplot(1,3,1)
plt.imshow(image, cmap='gray')
plt.title('Original (grayscale)')
plt.axis('off')

plt.subplot(1,3,2)
plt.imshow(magnitude_linear, cmap='gray')
plt.title('FFT Magnitude (linear)')
plt.axis('off')

plt.subplot(1,3,3)
plt.imshow(magnitude_log, cmap='gray')
plt.title('FFT Magnitude (log scale)')
plt.axis('off')

plt.tight_layout()
plt.show()

#4.) inverse 2D fft
ifft2_image = scipy.fft.ifft2(F_fft2_image)
print(f"ifft2 image data type: {ifft2_image.dtype}")

#back to original dtype of image
inversed_image_back_to_original = np.real(ifft2_image)
print(f"Inverse fft data type: {inversed_image_back_to_original.dtype}")
img_back_uint8 = np.clip(np.rint(inversed_image_back_to_original), 0, 255).astype(image.dtype)


plt.figure(figsize=(10,4))
plt.subplot(1,2,1); plt.imshow(image, cmap='gray'); plt.title("Original")
plt.axis('off')
plt.subplot(1,2,2); plt.imshow(img_back_uint8, cmap='gray'); plt.title("Back-transformed")
plt.axis('off')
plt.tight_layout(); plt.show()

"""
the FFT + inverse FFT pipeline is lossless
"""


#5.) 9x9 averaging filter
h_averaging_filter = np.ones((9, 9), dtype=np.float32) / (9 * 9)
filtered_image = scipy.signal.convolve2d(image, h_averaging_filter, mode="same", boundary="symm")

plt.figure(figsize=(10,4))
plt.subplot(1,2,1)
plt.imshow(image, cmap='gray')
plt.title("Original Image")
plt.axis('off')

plt.subplot(1,2,2)
plt.imshow(filtered_image, cmap='gray')
plt.title("9×9 Averaging Filtered Image")
plt.axis('off')

plt.tight_layout()
plt.show()

"""
removes high-frequency components (details vanish, they correspond to high frequencies)
"""

#6.) spectrum filter H 
M, N = image.shape      #Image
A, B = h_averaging_filter.shape #Kernel/Filter

#padded Image
P = M+A-1
Q = N+B-1

F_pad = scipy.fft.fft2(image, s=(P, Q))
H_pad = scipy.fft.fft2(h_averaging_filter, s=(P, Q))

G_pad = F_pad * H_pad
g_full_convolution = np.real(scipy.fft.ifft2(G_pad))

#centre of the core as reference
r0 = A//2
c0 = B//2

#has a row shift of r0 and a column shift of c0,
#and is exactly the same size as the original image
g_same = g_full_convolution[r0:r0 + M, c0:c0 + N]

plt.figure(figsize=(12,4))
plt.subplot(1,3,1); plt.imshow(image, cmap='gray'); plt.title('Original'); plt.axis('off')
plt.subplot(1,3,2); plt.imshow(filtered_image.astype(image.dtype), cmap='gray'); plt.title('9×9 avg filter'); plt.axis('off')
plt.subplot(1,3,3); plt.imshow(g_same, cmap='gray'); plt.title('Freq-domain result'); plt.axis('off')
plt.tight_layout(); plt.show()

"""
Multiplication in frequency domain = convolution in spatial domain
"""

#7.) equality check

#Spatial reference (linear, zero padding)
g_spatial = scipy.signal.convolve2d(image, h_averaging_filter, mode='same', boundary='fill', fillvalue=0)

# Frequency-domain (linear): pad -> multiply -> ifft -> crop
g_freq = g_same

#equality check
max_abs_diff = np.max(np.abs(g_spatial - g_freq))
rmse         = np.sqrt(np.mean((g_spatial - g_freq)**2))
print(f"\nmax |diff| = {max_abs_diff:.3e}, RMSE = {rmse:.3e}")

# strict pass/fail with tolerance (floating-point noise)
print("allclose:", np.allclose(g_spatial, g_freq, atol=1e-9))

#8.) Runtime comparison: spatial vs frequency
img_f = image.astype(float)                      
h      = h_averaging_filter.astype(float)      

def spatial_same_zero(img, ker):
    # linear convolution, zero padding -> matches your frequency path
    return scipy.signal.convolve2d(img, ker, mode='same',
                                   boundary='fill', fillvalue=0)

def freq_linear_same(img, ker):
    # pad to full linear size, multiply in F-domain, iFFT, crop to 'same'
    M, N = img.shape
    A, B = ker.shape
    P, Q = M + A - 1, N + B - 1
    F = scipy.fft.fft2(img, s=(P, Q))
    H = scipy.fft.fft2(ker, s=(P, Q))
    g_full = np.real(scipy.fft.ifft2(F * H))
    r0, c0 = A // 2, B // 2
    return g_full[r0:r0 + M, c0:c0 + N]

def timeit_ms(fn, repeats=5):
    t0 = time.perf_counter()
    for _ in range(repeats):
        fn()
    return 1e3 * (time.perf_counter() - t0) / repeats

# Baseline (your image, 9x9) 
t_spatial = timeit_ms(lambda: spatial_same_zero(img_f, h))
t_fft     = timeit_ms(lambda: freq_linear_same(img_f, h))
print(f"\nBaseline  — Spatial: {t_spatial:7.2f} ms | FFT: {t_fft:7.2f} ms")

# Scale image & kernel by ~2× and repeat 
img2 = cv.resize(img_f, None, fx=2, fy=2, interpolation=cv.INTER_NEAREST)

# kernel 2× -> 18x18 averaging
A2, B2 = h.shape[0] * 2, h.shape[1] * 2
h2 = np.ones((A2, B2), dtype=float) / (A2 * B2)

t_spatial2 = timeit_ms(lambda: spatial_same_zero(img2, h2))
t_fft2     = timeit_ms(lambda: freq_linear_same(img2, h2))
print(f"×2 sizes — Spatial: {t_spatial2:7.2f} ms | FFT: {t_fft2:7.2f} ms")

# growth factors
print(f"Spatial growth: {t_spatial2 / t_spatial:5.2f}×,  FFT growth: {t_fft2 / t_fft:5.2f}×")

"""
With a 9×9 filter, the spatial method is typically faster.

After doubling both the image and the kernel:
- Spatial time rises by roughly ~12× 
- FFT time rises by ~8× 
"""