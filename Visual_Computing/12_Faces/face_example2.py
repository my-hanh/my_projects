import cv2
from deepface import DeepFace
import matplotlib.pyplot as plt

img1_path = "cristoforetti_samantha.jpg"
img2_path = "mit-an-bord-die-italienerin.webp"

# Load images
img1 = cv2.imread(img1_path)
img2 = cv2.imread(img2_path)

# Perform verification
verification = DeepFace.verify(
    img1_path=img1_path,
    img2_path=img2_path,
    model_name="ArcFace",
    detector_backend="retinaface"
)

# Get result
is_same = verification["verified"]
distance = verification["distance"]

label = f"Same Person: {is_same}\nDistance: {distance:.3f}"

# Create side-by-side display
img1_rgb = cv2.cvtColor(img1, cv2.COLOR_BGR2RGB)
img2_rgb = cv2.cvtColor(img2, cv2.COLOR_BGR2RGB)

plt.figure(figsize=(10,5))

plt.subplot(1,2,1)
plt.imshow(img1_rgb)
plt.axis("off")
plt.title("Image A")

plt.subplot(1,2,2)
plt.imshow(img2_rgb)
plt.axis("off")
plt.title("Image B")

plt.suptitle(label, fontsize=14)
plt.show()
