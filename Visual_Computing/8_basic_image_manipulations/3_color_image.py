import matplotlib.pyplot as plt
from skimage import io

image = io.imread("happiness.avif")

colors = ["red", "green", "blue"]
channel_names = ["Red", "Green", "Blue"]

plt.figure(figsize=(15, 5))

for i, color in enumerate(colors):
    plt.hist(image[:, :, i].ravel(),
             bins=256,
             range=(0, 256),
             color=color,
             alpha=0.5,
             label=f"{channel_names[i]} channel")

plt.legend()
plt.title("RGB Histogram")
#plt.show()

print("Shape:", image.shape)
print("Dimensions:", image.ndim)
print("Data type:", image.dtype)
print("Min pixel:", image.min())
print("Max pixel:", image.max())

if image.ndim == 3:
    print("Channels:", image.shape[2])
else:
    print("Channels: 1 (grayscale)")