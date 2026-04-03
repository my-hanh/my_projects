from skimage import io
import matplotlib.pyplot as plt

image_path_1 = "pics/bloodCells.tif"
image_path_2 = "pics/ctSkull.tif"
image_path_3 = "pics/xRayChest.tif"

image1 = io.imread(image_path_1).squeeze()
image2 = io.imread(image_path_2).squeeze()
image3 = io.imread(image_path_3).squeeze()

# compute histograms manually
def compute_histogram(image, bins=256):
    hist = [0] * bins
    for pixel in image.ravel():
        hist[pixel] += 1
    return hist

hist_manual_1 = compute_histogram(image1)
hist_manual_2 = compute_histogram(image2)
hist_manual_3 = compute_histogram(image3)

plt.figure(figsize=(15, 5))

plt.subplot(1, 3, 1)
plt.bar(range(256), hist_manual_1)
plt.title("Manual Histogram: bloodCells")

plt.subplot(1, 3, 2)
plt.bar(range(256), hist_manual_2)
plt.title("Manual Histogram: ctSkull")

plt.subplot(1, 3, 3)
plt.bar(range(256), hist_manual_3)
plt.title("Manual Histogram: xRayChest")

plt.tight_layout()
plt.show()


# build-in function
plt.figure(figsize=(15, 5))

plt.subplot(1, 3, 1)
hist_1 = plt.hist(image1.ravel(), bins=256, range=[0,256])
plt.title("Build-in Function Histogram: bloodCells")

plt.subplot(1, 3, 2)
hist_2 = plt.hist(image2.ravel(), bins = 256, range=[0,256])
plt.title("Build-in Function Manual Histogram: ctSkull")

plt.subplot(1, 3, 3)
hist_3 = plt.hist(image3.ravel(), bins = 256, range=[0,256])
plt.title("Build-in Function Manual Histogram: xRayChest")

plt.show()
