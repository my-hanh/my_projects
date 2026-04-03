import cv2
import numpy as np
import matplotlib.pyplot as plt

# ---------------------------------------------------------
# Parameters (change these for your own experiment)
# ---------------------------------------------------------
img_low_path = "marie_curie.jpg"   # image that will provide LOW frequencies
img_high_path = "samantha_cristoforetti.jpg" # image that will provide HIGH frequencies

sigma_low = 7.0   # blur strength for low-pass image
sigma_high = 3.0  # blur strength for high-pass image

low_weight = 1.0  # contribution of low frequencies
high_weight = 1.0 # contribution of high frequencies
# ---------------------------------------------------------

def read_rgb(path):
    """Read an image as RGB float32 in [0,1]."""
    img_bgr = cv2.imread(path, cv2.IMREAD_COLOR)
    if img_bgr is None:
        raise FileNotFoundError(f"Could not read image: {path}")
    img_rgb = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2RGB)
    return img_rgb.astype(np.float32) / 255.0

def gaussian_blur(img, sigma):
    """Gaussian blur with kernel size chosen from sigma."""
    ksize = int(4 * sigma + 1)
    if ksize % 2 == 0:  # kernel size must be odd
        ksize += 1
    return cv2.GaussianBlur(img, (ksize, ksize), sigmaX=sigma, sigmaY=sigma)


# 1. Load images
img_low = read_rgb(img_low_path)
img_high = read_rgb(img_high_path)

# 2. Make sure both images have the same size
if img_low.shape != img_high.shape:
    img_high = cv2.resize(img_high, (img_low.shape[1], img_low.shape[0]))

# 3. Create low-frequency (blurred) version of first image
low_frequencies = gaussian_blur(img_low, sigma_low)

# 4. Create high-frequency version of second image
#    high = original - its low-pass version
high_blur = gaussian_blur(img_high, sigma_high)
high_frequencies = img_high - high_blur

# 5. Combine low and high frequencies to form the hybrid image
hybrid = low_weight * low_frequencies + high_weight * high_frequencies

# Clip to valid range [0,1]
hybrid = np.clip(hybrid, 0.0, 1.0)

# 6. Show results at original scale
fig, ax = plt.subplots(1, 4, figsize=(16, 4))

ax[0].imshow(img_low)
ax[0].set_title("Image A (low-freq source)")
ax[0].axis("off")

ax[1].imshow(img_high)
ax[1].set_title("Image B (high-freq source)")
ax[1].axis("off")

ax[2].imshow(low_frequencies)
ax[2].set_title("Low-pass (A)")
ax[2].axis("off")

ax[3].imshow(high_frequencies + 0.5)  # shifted for visualization
ax[3].set_title("High-pass (B)")
ax[3].axis("off")

plt.tight_layout()
plt.show()

# 7. Visualize hybrid image at multiple scales (simulating viewing distance)
scales = [1.0, 0.7, 0.5, 0.3]
fig2, ax2 = plt.subplots(1, len(scales), figsize=(16, 4))

for i, s in enumerate(scales):
    resized = cv2.resize(
        hybrid,
        (int(hybrid.shape[1] * s), int(hybrid.shape[0] * s)),
        interpolation=cv2.INTER_AREA,
    )
    ax2[i].imshow(resized)
    ax2[i].set_title(f"Hybrid (scale={s:.1f})")
    ax2[i].axis("off")

plt.tight_layout()
plt.show()
