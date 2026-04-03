import os
import numpy as np
import torch
import torch.nn.functional as F
import matplotlib.pyplot as plt

from transformers import AutoImageProcessor, AutoModel
from transformers.image_utils import load_image
from huggingface_hub import login


# -----------------------
# Auth
# -----------------------
token = os.environ.get("TOKEN")
if token is None:
    raise RuntimeError("HF TOKEN environment variable not set")
login(token=token)

# -----------------------
# Helpers
# -----------------------
def tensor_to_vis_image(pixel_values, processor):
    """Convert normalized [3,H,W] tensor to displayable RGB image [H,W,3] in [0,1]."""
    x = pixel_values.detach().cpu().float().numpy()
    mean = np.array(processor.image_mean).reshape(3, 1, 1)
    std = np.array(processor.image_std).reshape(3, 1, 1)
    x = x * std + mean
    x = np.clip(x, 0, 1)
    x = np.transpose(x, (1, 2, 0))
    return x

def pick_diverse_anchors(patch_grid_n, margin=2, k=8):
    """
    Greedy farthest-point sampling in feature space (cosine distance) to get diverse anchors.
    patch_grid_n: [nh, nw, D] L2-normalized
    """
    nh, nw, _ = patch_grid_n.shape

    ys = torch.arange(margin, nh - margin)
    xs = torch.arange(margin, nw - margin)
    yy, xx = torch.meshgrid(ys, xs, indexing="ij")
    coords = torch.stack([yy.flatten(), xx.flatten()], dim=1)  # [M,2]
    feats = patch_grid_n[coords[:, 0], coords[:, 1]]          # [M,D]

    # Start from the patch with maximum "energy" proxy: here pick the one with max L2 norm
    # (since feats are normalized, all norms are 1; instead pick the one most "central" by sim variance)
    # Simple robust start: highest mean similarity magnitude to others (approx with one random subset)
    start = 0
    chosen = [start]

    sim = feats @ feats[chosen[0]].unsqueeze(1)   # [M,1]
    min_dist = 1 - sim.squeeze(1)                 # cosine distance

    for _ in range(1, k):
        idx = torch.argmax(min_dist).item()
        chosen.append(idx)
        sim_new = feats @ feats[idx].unsqueeze(1)
        dist_new = 1 - sim_new.squeeze(1)
        min_dist = torch.minimum(min_dist, dist_new)

    return [(int(coords[i, 0]), int(coords[i, 1])) for i in chosen]


# -----------------------
# Model + Image
# -----------------------
name = "facebook/dinov3-vits16-pretrain-lvd1689m"
img = load_image("Tomates.jpg")  # or URL/path

processor = AutoImageProcessor.from_pretrained(name, token=token)
model = AutoModel.from_pretrained(name, token=token).eval()

# Higher resolution helps "dense" maps look less mushy.
# do_center_crop=False avoids losing parts of the image.
inputs = processor(
    images=img,
    return_tensors="pt",
    do_center_crop=False,
    size={"shortest_edge": 518},
)

with torch.inference_mode():
    out = model(**inputs)

# -----------------------
# Patch features -> grid
# -----------------------
tokens = out.last_hidden_state                # [B, 1 + R + N, D]
R = getattr(model.config, "num_register_tokens", 0)
patch_tokens = tokens[:, 1 + R :, :]          # [B, N, D]

patch_size = getattr(model.config, "patch_size", 16)
B, _, H, W = inputs.pixel_values.shape
nh, nw = H // patch_size, W // patch_size

patch_grid = patch_tokens.reshape(B, nh, nw, -1)[0]     # [nh,nw,D]
patch_grid_n = F.normalize(patch_grid, dim=-1)          # [nh,nw,D] (L2 normalized)
P = patch_grid_n.reshape(-1, patch_grid_n.shape[-1])    # [N,D]

# For correct overlay, display the *processed* image that produced the features
vis = tensor_to_vis_image(inputs.pixel_values[0], processor)

# -----------------------
# Sharpened similarity heatmap
# -----------------------
TEMP = 8.0     # 5–15: higher => sharper
GAMMA = 2.0    # 1.5–3: higher => stronger emphasis on high similarity
SMOOTH = True  # avg-pool smoothing to reduce patch noise

def sim_heatmap(anchor_yx):
    ay, ax = anchor_yx
    q = patch_grid_n[ay, ax]                  # [D], normalized
    sim = (P @ q).reshape(nh, nw)             # cosine similarity

    # --- make "dense features" pop ---
    sim = sim * TEMP
    sim = (sim - sim.min()) / (sim.max() - sim.min() + 1e-6)
    sim = sim ** GAMMA

    if SMOOTH:
        sim = F.avg_pool2d(sim[None, None], kernel_size=3, stride=1, padding=1)[0, 0]

    # Upsample to image resolution for overlay
    sim_up = F.interpolate(
        sim[None, None],
        size=(H, W),
        mode="bilinear",
        align_corners=False
    )[0, 0].cpu().numpy()

    return sim_up

# Choose 8 diverse anchors automatically
anchors = pick_diverse_anchors(patch_grid_n, margin=2, k=8)

# -----------------------
# Plot 3x3 grid
# -----------------------
fig, axes = plt.subplots(3, 3, figsize=(10, 10))
k = 0
for r in range(3):
    for c in range(3):
        ax = axes[r, c]
        if r == 1 and c == 1:
            ax.imshow(vis)
            ax.set_title("Input (processed)")
        else:
            ay, axx = anchors[k]
            hm = sim_heatmap((ay, axx))
            k += 1

            ax.imshow(vis)
            ax.imshow(hm, cmap="viridis", alpha=0.60)   # slightly higher alpha => stronger emphasis
            ax.set_title(f"Similarity ({ay},{axx})")

        ax.axis("off")

plt.tight_layout()
plt.show()
