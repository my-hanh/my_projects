import torch
import torch.nn.functional as F
import matplotlib.pyplot as plt
import os
from transformers import AutoImageProcessor, AutoModel
from transformers.image_utils import load_image
from huggingface_hub import hf_hub_download, login

"""
token = os.environ["TOKEN"]
login(token=token)

path = hf_hub_download(
    repo_id="facebook/dinov3-vits16-pretrain-lvd1689m",
    filename="preprocessor_config.json",
    token=token,
)
print("Downloaded:", path)
"""

token = os.environ.get("TOKEN")
#print(os.environ.get("TOKEN"))
if token is None:
    raise RuntimeError("HF TOKEN environment variable not set")

login(token=token)

# 1) Modell + Bild
name = "facebook/dinov3-vits16-pretrain-lvd1689m"
img = load_image("http://images.cocodataset.org/val2017/000000039769.jpg")

processor = AutoImageProcessor.from_pretrained(name, token=token)
model = AutoModel.from_pretrained(name, token=token).eval()

inputs = processor(images=img, return_tensors="pt")

with torch.inference_mode():
    out = model(**inputs)

# 2) Patch-Tokens extrahieren (CLS + Register + Patches)
tokens = out.last_hidden_state  # [B, 1 + R + N, D]
R = getattr(model.config, "num_register_tokens", 0)
patch_tokens = tokens[:, 1 + R :, :]       # [1, N, D]

# Patch-Grid-Größe bestimmen
patch_size = getattr(model.config, "patch_size", 16)
B, _, H, W = inputs.pixel_values.shape
nh, nw = H // patch_size, W // patch_size  # Patch-Raster
patch_grid = patch_tokens.reshape(B, nh, nw, -1)  # [1, nh, nw, D]

# 3) Heatmap Variante A: PCA auf Patch-Embeddings (1. Hauptkomponente)
X = patch_tokens[0]  # [N, D]
X = X - X.mean(dim=0, keepdim=True)
# PCA via SVD: PC1 = V[:,0]
_, _, Vh = torch.linalg.svd(X, full_matrices=False)
pc1 = (X @ Vh[0].T)  # [N]
pc1_map = pc1.reshape(nh, nw)

# normalisieren auf [0,1]
pc1_map = (pc1_map - pc1_map.min()) / (pc1_map.max() - pc1_map.min() + 1e-6)

# auf Bildgröße hochskalieren
pc1_up = F.interpolate(
    pc1_map[None, None, :, :], size=(H, W), mode="bilinear", align_corners=False
)[0, 0].cpu().numpy()

# 4) Optional: Heatmap Variante B (oft noch "segmentiger"):
# Ähnlichkeitskarte zu einem gewählten Patch (hier: Bildmitte)
# Du kannst (cy,cx) ändern oder später interaktiv machen.
cy, cx = nh // 2, nw // 2
q = patch_grid[0, cy, cx]                      # [D]
P = patch_grid[0].reshape(-1, patch_grid.shape[-1])  # [N, D]
sim = F.cosine_similarity(F.normalize(P, dim=-1), F.normalize(q, dim=-1)[None, :], dim=-1)
sim_map = sim.reshape(nh, nw)
sim_map = (sim_map - sim_map.min()) / (sim_map.max() - sim_map.min() + 1e-6)

sim_up = F.interpolate(
    sim_map[None, None, :, :], size=(H, W), mode="bilinear", align_corners=False
)[0, 0].cpu().numpy()

# 5) Plot
fig = plt.figure(figsize=(12, 4))

ax1 = fig.add_subplot(1, 3, 1)
ax1.imshow(img)
ax1.set_title("Original")
ax1.axis("off")

ax2 = fig.add_subplot(1, 3, 2)
ax2.imshow(img)
ax2.imshow(pc1_up, alpha=0.55)
ax2.set_title("DINOv3 Patch-PCA Heatmap")
ax2.axis("off")

ax3 = fig.add_subplot(1, 3, 3)
ax3.imshow(img)
ax3.imshow(sim_up, alpha=0.55)
ax3.set_title(f"Patch-Similarity Heatmap (anchor: {cy},{cx})")
ax3.axis("off")

plt.tight_layout()
plt.show()




