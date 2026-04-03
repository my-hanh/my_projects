import pydicom
import matplotlib.pyplot as plt
import numpy as np

path = "pics/brain/brain_001.dcm"

ds = pydicom.dcmread(path)

print(ds)

plt.imshow(ds.pixel_array, cmap=plt.cm.bone)
plt.title("DICOM image original")
plt.show()

flipped_image_horizontally = np.fliplr(ds.pixel_array)
plt.imshow(flipped_image_horizontally, cmap=plt.cm.bone)
plt.title("DICOM image flipped horizontally")
plt.show()

flipped_image_vertically = np.flipud(ds.pixel_array)
plt.imshow(flipped_image_vertically, cmap=plt.cm.bone)
plt.title("DICOM image flipped vertically")
plt.show()

img = ds.pixel_array
#g = img.astype(np.uint8)
g = ((img - img.min()) / (img.max() - img.min()) * 255).astype(np.uint8)    # Normalize to 0-255 --> uint8

plt.imshow(g, cmap="gray")
plt.title("DICOM image converted to uint8")
plt.show()


result_img_float = g.astype(np.float32)

plt.imshow(result_img_float, cmap="gray")
plt.title("DICOM image converted to float32")
plt.show()