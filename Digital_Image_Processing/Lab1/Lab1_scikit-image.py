from skimage import io
import matplotlib.pyplot as plt

image_path = "pics/lena_gray.gif"
#image_path = "pics/lena_color.gif"
image = io.imread(image_path).squeeze()

width = image.shape[1]
height = image.shape[0]

print("Return Type: " + str(type(image)))
print("Shape of image array: " + str(image.shape))
print("Image size: " + str(width) + "x" + str(height))
print("Number of channels: " + str(len(image.shape)))

plt.imshow(image, cmap="gray")
plt.axis("off")
plt.show()

io.imsave("saved_pics/saved_img_skimage.png", image)