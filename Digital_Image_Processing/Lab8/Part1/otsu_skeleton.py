import numpy as np
import matplotlib.pyplot as plt
from PIL import Image


def basic_thresholding(image):
    t = np.mean(image.flatten()).astype(int)

    eps = 0.5
    max_iter = 200
    count = 0
    new_t = 0

    while abs(new_t < t) >= eps and count < max_iter:
        if count > 0:
            t = new_t

        G1 = image[image <= t]
        G2 = image[image > t]

        m1 = np.mean(G1) if len(G1) > 0 else 0.0
        m2 = np.mean(G2) if len(G2) > 0 else 0.0

        new_t = (m1 + m2)/2
        count += 1

    t = new_t
    bin_img = image > t
    return bin_img, int(t)


def my_otsu(image):
    #8-bit grayscale
    img = image
    if image.ndim == 3:
        img = img[..., 0]
    img = img.astype(np.uint8)

    hist = np.bincount(img.ravel(), minlength=256).astype(np.float64)
    total = hist.sum()
    p = hist / total if total > 0 else hist

    # Cumulative Sum P1(k) and m(k), global mean value mG
    levels = np.arange(256, dtype=np.float64)
    P1 = np.cumsum(p)
    m = np.cumsum(p*levels)
    mG = m[-1]

    # σ_B^2(k)
    denom = P1 * (1.0 - P1)
    num = (mG * P1 - m) ** 2
    with np.errstate(divide='ignore', invalid='ignore'):
        sigma_b2 = np.where(denom > 0, num / denom, 0.0)

    # Optimum
    threshold = int(np.argmax(sigma_b2))  # k̂
    between_class_variance = sigma_b2

    # separability η = σ_B^2(k̂)/σ_G^2 (optional)
    sigma_g2 = np.sum(p * (levels - mG) ** 2)
    separability = float(sigma_b2[threshold] / sigma_g2) if sigma_g2 > 0 else 0.0

    binary_image = image >= threshold
    return binary_image, between_class_variance, threshold, separability


def main():
    #image = np.asarray(Image.open("binary_test_image.png"))
    image = np.asarray(Image.open("thGonz.tif"))

    if len(image.shape) == 3:
        image = image[:, :, 0]

    binary_basic, t = basic_thresholding(image)
    print("Basic Thresholding. Output Threshold: " + str(t))

    binary_basic = binary_basic.astype(int)

    binary_otsu, between_class_variance, threshold, separability = my_otsu(image)
    print("Otsu's Method. Output Threshold: " + str(threshold))
    print("Separability: " + str(separability))

    plt.figure(figsize=(12, 4))

    plt.subplot(1, 3, 1)
    plt.imshow(binary_basic, cmap="gray")
    plt.title("Basic Thresholding\nT=" + str(t))
    plt.axis('off')

    plt.subplot(1, 3, 2)
    plt.plot(between_class_variance * 100)
    plt.axvline(threshold, color='r', linestyle='--')
    plt.title("Between-class Variance\n(k und T=" + str(threshold) + ")")
    plt.xlabel("Intensity level")
    plt.ylabel("Variance * 100")

    plt.subplot(1, 3, 3)
    plt.imshow(binary_otsu.astype(int), cmap="gray")
    plt.title("Otsu Binary\nT=" + str(threshold))
    plt.axis('off')

    plt.tight_layout()
    plt.show()


if __name__ == "__main__":
    main()

"""
    Separability Measure
 
    η≈1 → sehr gut trennbar (quasi-binär)
    η mittel/klein → Klassen überlappen stärker
    
    (a) Nach Intuition:
        Die binary_test_image.png ist (nahezu) binär 
        → deutlich einfacher zu schwellen als thGonz.tif.

    (b) Nach η:
        Auch hier schneidet binary_test_image.png besser ab 
        (deutlich höheres η, typischerweise nahe 1), 
        während thGonz.tif ein merklich kleineres η
        aufweist, da seine Grauwerte-Verteilung breiter 
        ist/überlappt.
"""
