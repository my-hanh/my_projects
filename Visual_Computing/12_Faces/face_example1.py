# python
from deepface import DeepFace
import cv2
import matplotlib.pyplot as plt

# ----- Load image -----
img_path = "cristoforetti_samantha.jpg"
img = cv2.imread(img_path)

# DeepFace analyze
result = DeepFace.analyze(
    img_path=img_path,
    actions=['age', 'gender', 'emotion', 'race'],
    detector_backend='retinaface'
)

# ----- Use single face result -----
face = result[0] if isinstance(result, list) else result
x = int(face["region"]["x"])
y = int(face["region"]["y"])
w = int(face["region"]["w"])
h = int(face["region"]["h"])

# Draw rectangle on the image
cv2.rectangle(img, (x, y), (x + w, y + h), (0, 255, 0), 2)

# Prepare label text
label = f"Age: {face.get('age', 'N/A')}\n"
label += f"Gender: {face.get('gender', 'N/A')}\n"
label += f"Emotion: {face.get('dominant_emotion', 'N/A')}\n"
label += f"Race: {face.get('dominant_race', 'N/A')}"

# Convert BGR → RGB for matplotlib
img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)

# ----- Show image with result text below -----
fig, (ax_img, ax_text) = plt.subplots(2, 1, figsize=(8, 8), gridspec_kw={'height_ratios': [4, 1]})
ax_img.imshow(img_rgb)
ax_img.axis('off')
ax_img.set_title("DeepFace Analysis Result")

ax_text.axis('off')
# Place the label in the center-left of the bottom area
ax_text.text(0.01, 0.5, label, fontsize=12, va='center', family='monospace')

plt.tight_layout()
plt.show()
