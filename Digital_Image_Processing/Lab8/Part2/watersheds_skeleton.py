import numpy as np
import matplotlib.pyplot as plt
import cv2 as cv

from scipy import ndimage as ndi

import skimage as skimage
import skimage.segmentation as segmentation
import skimage.feature as feature
from skimage.filters import threshold_otsu


def compute_binary_image_otsu(image, show=False):
    """
    Compute a binarized image using Otsu's method.
    Returns a boolean mask with True for foreground (objects).
    """
    # If the input is RGB(A), convert to grayscale (safety check)
    if image.ndim == 3:
        image = image[..., 0]

    # Otsu threshold works for float [0,1] or uint images as well
    thr = threshold_otsu(image)
    image_binary = image > thr

    if show:
        plt.figure(figsize=(8, 3))
        plt.subplot(1, 2, 1)
        plt.imshow(image_binary, cmap='gray')
        plt.title('Otsu binary')
        plt.axis('off')

        plt.subplot(1, 2, 2)
        # Adaptive histogram range
        vmin, vmax = float(np.min(image)), float(np.max(image))
        plt.hist(image.ravel(), bins=256, range=(vmin, vmax))
        plt.axvline(thr, color='r', linestyle='dashed', linewidth=2)
        plt.title(f'Histogram (thr={thr:.4f})')
        plt.tight_layout()
        plt.show()
    return image_binary.astype(bool)


def distance_transform_implementation_example(binaryImage, show=False):
    """
    Provided two-pass integer DT example from the skeleton.
    Kept for reference; not used by the final pipeline (we use EDT).
    """
    g = 1000 * np.uint16(binaryImage > 0)
    for iy in range(1, g.shape[0]-1):
        for ix in range(1, g.shape[1]-1):
            neighborMinValue = np.min([
                g[iy-1, ix-1], g[iy-1, ix], g[iy, ix-1], g[iy-1, ix+1],
                g[iy+1, ix+1], g[iy+1, ix], g[iy, ix+1], g[iy+1, ix-1]
            ])
            if g[iy, ix]:
                g[iy, ix] = neighborMinValue + 1

    for iy in range(g.shape[0]-2, 1, -1):
        for ix in range(g.shape[1]-2, 1, -1):
            neighborMinValue = np.min([
                g[iy-1, ix-1], g[iy-1, ix], g[iy, ix-1], g[iy-1, ix+1],
                g[iy+1, ix+1], g[iy+1, ix], g[iy, ix+1], g[iy+1, ix-1]
            ])
            if g[iy, ix]:
                g[iy, ix] = neighborMinValue + 1

    if show:
        plt.figure()
        plt.imshow(g, 'gray')
        plt.axis('off')
        plt.title('Two-pass integer DT (example)')
        plt.show()

    return g


def main(image_path="coins.tif", use_distance=True, show_intermediate=True):
    # --- Load image ---
    image = plt.imread(image_path)
    if image.ndim == 3:
        image = image[:, :, 0]

    # --- 2.1 Thresholding (Otsu) ---
    imageBinary = compute_binary_image_otsu(image, show=False)

    # --- 2.2 Seed points ---
    # a) Exact Euclidean distance transform on the binary mask
    imageDistance = ndi.distance_transform_edt(imageBinary)

    # b) Local maxima of the distance (these are good object centers)
    # For robustness, set a minimal distance between peaks relative to image size
    min_dist = max(5, int(min(image.shape) * 0.01))
    peakCoords = feature.peak_local_max(
        imageDistance,
        min_distance=min_dist,
        footprint=np.ones((3, 3), dtype=bool),
        labels=imageBinary
    )

    # c) Represent seed points as a matrix, then label them
    mask = np.zeros_like(imageBinary, dtype=bool)
    if peakCoords.size > 0:
        mask[peakCoords[:, 0], peakCoords[:, 1]] = True
    # Give individual integer labels to the seed points
    seedMask, _ = ndi.label(mask)

    # --- 3. Watershed algorithm ---
    # a) Apply watershed to the NEGATIVE of the chosen "topography"
    if use_distance:
        topo = -imageDistance
    else:
        # If using the grayscale image, invert brightness so bright objects become basins.
        # Normalize to float for safety.
        imgf = image.astype(np.float32)
        imgf = (imgf - imgf.min()) / (imgf.max() - imgf.min() + 1e-8)
        topo = -imgf

    labeled_regions = segmentation.watershed(topo, markers=seedMask, mask=imageBinary)

    # b) Mask so that segmented regions cover only the objects (done by 'mask=imageBinary')
    labels = labeled_regions * imageBinary

    # --- 4. Visualization (intermediate steps) ---
    if show_intermediate:
        fig, axes = plt.subplots(ncols=5, figsize=(12, 3), sharex=True, sharey=True)
        ax = axes.ravel()

        ax[0].imshow(-image, cmap=plt.cm.gray, interpolation='none')
        ax[0].set_title('Original Image')
        ax[1].imshow(imageBinary, cmap=plt.cm.gray, interpolation='none')
        ax[1].set_title('Otsu: image_binary')
        # Normalize DT for display only
        dt_disp = imageDistance / np.max(imageDistance) if np.max(imageDistance) > 0 else imageDistance
        ax[2].imshow(dt_disp, cmap=plt.cm.jet, interpolation='none')
        ax[2].set_title('Distance (norm.)')
        if peakCoords.size > 0:
            ax[3].scatter(peakCoords[:, 1].reshape((-1,)), peakCoords[:, 0].reshape((-1,)), s=10, marker='o')
        ax[3].set_title('Seed points')
        ax[4].imshow(labels, cmap=plt.cm.nipy_spectral, interpolation='none')
        ax[4].set_title('Separated objects')

        for a in ax:
            a.set_axis_off()

        fig.tight_layout()
        plt.show()

    return labels, imageBinary, imageDistance, peakCoords, seedMask


if __name__ == "__main__":
    # Default run expects "coins.tif" next to this script.
    # You can also call main("thGonz.tif") to explore that image.
    main()
