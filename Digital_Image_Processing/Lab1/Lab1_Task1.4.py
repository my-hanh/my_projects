import os
import numpy as np
import matplotlib.pyplot as plt
from PIL import Image
import pydicom

path = "pics/brain"

files = sorted([f for f in os.listdir(path) if f.endswith('.dcm')])

arrays = []
pil_images = []
for f in files:
    ds = pydicom.dcmread(os.path.join(path, f))
    arr = ds.pixel_array
    arrays.append(arr)
    pil_images.append(Image.fromarray(((arr - arr.min()) / (arr.max() - arr.min()) * 255).astype(np.uint8))) # Convert to PIL Image for GIF saving

volume = np.stack(arrays, axis=-1)   # shape: (height, width, num_slices)
print("3D array shape:", volume.shape)

#for i, arr in enumerate(arrays, 1):
#    plt.imshow(arr, cmap="gray")
#    plt.title(f"Slice {i}")
#    plt.axis("off")
#    plt.show()

gif_path = "brain_slices.gif"
pil_images[0].save(
    gif_path,
    save_all=True,
    append_images=pil_images[1:],  # all remaining slices
    duration=200,  # ms per frame
    loop=0
)

print(f"GIF saved as {gif_path}")