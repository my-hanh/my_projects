import os
import numpy as np
import cv2
import torch
import torch.nn.functional as F
import matplotlib.pyplot as plt

from transformers import AutoImageProcessor, AutoModel
from huggingface_hub import login

# -----------------------
# Config
# -----------------------
MODEL_ID = "facebook/dinov3-vits16-pretrain-lvd1689m"
DEVICE = "cuda" if torch.cuda.is_available() else "cpu"

# Feature resolution (higher = sharper but slower)
SHORTEST_EDGE = 384   # try 256 / 384 / 518
DO_CENTER_CROP = False

# Heatmap contrast
TEMP = 8.0     # 5–15
GAMMA = 2.0    # 1.5–3.0
SMOOTH = True  # avg pooling on similarity map

# Webcam
CAM_ID = 0
WINDOW_NAME = "DINOv3 Webcam (click to set anchor) - press q to quit"
FPS_LIMIT = 15  # reduce if CPU bound

# -----------------------
# Auth (TOKEN env var)
# -----------------------
token = os.environ.get("TOKEN")
if token is None:
    raise RuntimeError("HF TOKEN environment variable not set")
login(token=token)

# -----------------------
# Load model
# -----------------------
processor = AutoImageProcessor.from_pretrained(MODEL_ID, token=token)
model = AutoModel.from_pretrained(MODEL_ID, token=token).to(DEVICE).eval()

patch_size = getattr(model.config, "patch_size", 16)
num_register = getattr(model.config, "num_register_tokens", 0)

# -----------------------
# State for clicking
# -----------------------
anchor_xy = None  # anchor in display pixel coords (x,y)

def on_mouse(event, x, y, flags, param):
    global anchor_xy
    if event == cv2.EVENT_LBUTTONDOWN:
        anchor_xy = (x, y)

# -----------------------
# Helpers
# -----------------------
def unnormalize_to_uint8(pixel_values, processor):
    """
    pixel_values: torch [3,H,W] normalized
    returns uint8 BGR image [H,W,3] for OpenCV display
    """
    x = pixel_values.detach().cpu().float().numpy()
    mean = np.array(processor.image_mean).reshape(3, 1, 1)
    std = np.array(processor.image_std).reshape(3, 1, 1)
    x = x * std + mean
    x = np.clip(x, 0, 1)
    x = (x * 255.0).astype(np.uint8)
    x = np.transpose(x, (1, 2, 0))  # RGB
    x = cv2.cvtColor(x, cv2.COLOR_RGB2BGR)
    return x

def make_similarity_overlay(vis_bgr, sim_map_01, alpha=0.6):
    """
    vis_bgr: uint8 [H,W,3]
    sim_map_01: float [H,W] in [0,1]
    """
    hm = (sim_map_01 * 255).astype(np.uint8)
    hm_color = cv2.applyColorMap(hm, cv2.COLORMAP_VIRIDIS)
    out = cv2.addWeighted(vis_bgr, 1 - alpha, hm_color, alpha, 0)
    return out

@torch.inference_mode()
def compute_dense_features(frame_bgr):
    """
    frame_bgr: uint8 [H,W,3]
    returns:
      vis_bgr (processed image used by model) [H,W,3]
      patch_grid_n: [nh,nw,D] normalized patch features
    """
    frame_rgb = cv2.cvtColor(frame_bgr, cv2.COLOR_BGR2RGB)

    inputs = processor(
        images=frame_rgb,
        return_tensors="pt",
        do_center_crop=DO_CENTER_CROP,
        size={"shortest_edge": SHORTEST_EDGE},
    )
    inputs = {k: v.to(DEVICE) for k, v in inputs.items()}

    out = model(**inputs)
    tokens = out.last_hidden_state  # [B, 1+R+N, D]
    patches = tokens[:, 1 + num_register :, :]  # [B,N,D]

    B, _, H, W = inputs["pixel_values"].shape
    nh, nw = H // patch_size, W // patch_size

    patch_grid = patches.reshape(B, nh, nw, -1)[0]            # [nh,nw,D]
    patch_grid_n = F.normalize(patch_grid, dim=-1)            # [nh,nw,D]

    vis_bgr = unnormalize_to_uint8(inputs["pixel_values"][0], processor)

    return vis_bgr, patch_grid_n

def anchor_to_patch(anchor_xy, H, W, nh, nw):
    """
    Convert display pixel coords (x,y) into patch coords (ay,ax) for patch grid size (nh,nw).
    """
    x, y = anchor_xy
    x = np.clip(x, 0, W - 1)
    y = np.clip(y, 0, H - 1)

    ax = int((x / W) * nw)
    ay = int((y / H) * nh)

    ax = int(np.clip(ax, 0, nw - 1))
    ay = int(np.clip(ay, 0, nh - 1))
    return ay, ax

@torch.inference_mode()
def compute_similarity_map(patch_grid_n, anchor_patch):
    """
    patch_grid_n: [nh,nw,D] normalized
    anchor_patch: (ay,ax)
    returns sim_up: [H,W] float in [0,1] (upsampling done later by caller if needed)
    """
    nh, nw, D = patch_grid_n.shape
    P = patch_grid_n.reshape(-1, D)  # [N,D]
    ay, ax = anchor_patch
    q = patch_grid_n[ay, ax]         # [D]

    sim = (P @ q).reshape(nh, nw)    # cosine sim because normalized

    # sharpen
    sim = sim * TEMP
    sim = (sim - sim.min()) / (sim.max() - sim.min() + 1e-6)
    sim = sim ** GAMMA

    if SMOOTH:
        sim = F.avg_pool2d(sim[None, None], 3, 1, 1)[0, 0]

    return sim  # [nh,nw] torch

# -----------------------
# Main loop
# -----------------------
cap = cv2.VideoCapture(CAM_ID)
if not cap.isOpened():
    raise RuntimeError("Could not open webcam")

cv2.namedWindow(WINDOW_NAME, cv2.WINDOW_NORMAL)
cv2.setMouseCallback(WINDOW_NAME, on_mouse)

last_time = 0.0

while True:
    # FPS limit
    now = cv2.getTickCount() / cv2.getTickFrequency()
    if now - last_time < 1.0 / FPS_LIMIT:
        key = cv2.waitKey(1) & 0xFF
        if key == ord("q"):
            break
        continue
    last_time = now

    ret, frame = cap.read()
    if not ret:
        break

    # Compute dense features for this frame
    vis_bgr, patch_grid_n = compute_dense_features(frame)

    H, W = vis_bgr.shape[:2]
    nh, nw, _ = patch_grid_n.shape

    # If no anchor yet, set anchor to center
    if anchor_xy is None:
        ax0, ay0 = W // 2, H // 2
        anchor_disp = (ax0, ay0)
    else:
        anchor_disp = anchor_xy

    anchor_patch = anchor_to_patch(anchor_disp, H, W, nh, nw)

    sim = compute_similarity_map(patch_grid_n, anchor_patch)  # [nh,nw]
    sim_up = F.interpolate(
        sim[None, None],
        size=(H, W),
        mode="bilinear",
        align_corners=False
    )[0, 0].detach().cpu().numpy()

    overlay = make_similarity_overlay(vis_bgr, sim_up, alpha=0.6)

    # Draw anchor marker + info
    x, y = anchor_disp
    cv2.circle(overlay, (int(x), int(y)), 6, (255, 255, 255), -1)
    cv2.circle(overlay, (int(x), int(y)), 10, (0, 0, 0), 2)

    ay, ax = anchor_patch
    cv2.putText(
        overlay,
        f"anchor patch: ({ay},{ax})  res: {H}x{W}  grid: {nh}x{nw}  device: {DEVICE}",
        (10, 25),
        cv2.FONT_HERSHEY_SIMPLEX,
        0.7,
        (255, 255, 255),
        2,
        cv2.LINE_AA,
    )

    cv2.imshow(WINDOW_NAME, overlay)

    key = cv2.waitKey(1) & 0xFF
    if key == ord("q"):
        break
    # optional: reset anchor to center
    if key == ord("c"):
        anchor_xy = None

cap.release()
cv2.destroyAllWindows()
