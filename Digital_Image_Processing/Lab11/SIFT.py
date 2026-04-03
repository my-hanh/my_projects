import numpy as np
import cv2 as cv
import matplotlib.pyplot as plt


# b.) create array of keypoints and descriptors
img1 = cv.imread("clutteredDesk.jpg")
img2 = cv.imread("stapleRemover.jpg")

gray1= cv.cvtColor(img1,cv.COLOR_BGR2GRAY)
gray2= cv.cvtColor(img2,cv.COLOR_BGR2GRAY)

# SIFT-Initialisierung robust gegen fehlende Stubs / verschiedene OpenCV-Versionen
sift_creator = getattr(cv, 'SIFT_create', None) or getattr(getattr(cv, 'xfeatures2d', None), 'SIFT_create', None)
if sift_creator is None:
    raise ImportError("SIFT nicht gefunden — installiere `opencv-contrib-python`")
sift = sift_creator()

keypoints1, descriptors1 = sift.detectAndCompute(gray1, None)
keypoints2, descriptors2 = sift.detectAndCompute(gray2, None)

img1 = cv.drawKeypoints(gray1,keypoints1,img1,flags=cv.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS)
img2 = cv.drawKeypoints(gray2,keypoints2,img2,flags=cv.DRAW_MATCHES_FLAGS_DRAW_RICH_KEYPOINTS)

cv.imwrite('sift_keypoints1.jpg',img1)
cv.imwrite('sift_keypoints2.jpg',img2)

# c.) how many components the query and the train descriptor vectors have
print('des1 shape:', descriptors1.shape)
print('des2 shape:', descriptors2.shape)
print('des1 dtype:', descriptors1.dtype)

# d.) SiftVisualizer
from sift_visualizer import SiftVisualizer

vis1 = SiftVisualizer(img1, keypoints1, descriptors1)
vis2 = SiftVisualizer(img2, keypoints2, descriptors2)

vis1.investigator('clutteredDesk keypoints')
vis2.investigator('stapleRemover keypoints')

img1_rgb = cv.cvtColor(img1, cv.COLOR_BGR2RGB)
img2_rgb = cv.cvtColor(img2, cv.COLOR_BGR2RGB)

fig, axes = plt.subplots(2, 2)
axes[0, 0].imshow(img1_rgb)
axes[0, 0].axis('off')
axes[0, 0].set_title('Keypoints 1')

axes[0, 1].imshow(img2_rgb)
axes[0, 1].axis('off')
axes[0, 1].set_title('Keypoints 2')

img_vis1 = cv.imread("sift_keypoints1.jpg")
axes[1, 0].imshow(img_vis1)
axes[1, 0].axis('off')
axes[1, 0].set_title('SiftVisualizer 1')

img_vis2 = cv.imread("sift_keypoints2.jpg")
axes[1, 1].imshow(img_vis2)
axes[1, 1].axis('off')
axes[1, 1].set_title('SiftVisualizer 2')

plt.tight_layout()
plt.show()

# e.) Brute Force Matching + Ratio Test

img1 = cv.imread('book.jpg',cv.IMREAD_GRAYSCALE)          # queryImage
img2 = cv.imread('book&coffee.webp', cv.IMREAD_GRAYSCALE) # trainImage

# Initiate ORB detector
orb_creator = getattr(cv, 'ORB_create', None)
if orb_creator is None:
    raise ImportError("ORB nicht gefunden — überprüfe deine OpenCV-Installation")
orb = orb_creator()

# find the keypoints and descriptors with ORB
kp1, des1 = orb.detectAndCompute(img1, None)
kp2, des2 = orb.detectAndCompute(img2, None)

# BFMatcher with default params
bf = cv.BFMatcher()
matches = bf.knnMatch(des1, des2, k=2)

# Apply ratio test
good = []
for m, n in matches:
    if m.distance < 0.75 * n.distance:
        good.append([m])

# cv.drawMatchesKnn expects list of lists as matches.
img3 = cv.drawMatchesKnn(img1, kp1, img2, kp2, good, None, flags=cv.DrawMatchesFlags_NOT_DRAW_SINGLE_POINTS)

plt.imshow(img3)
plt.show()

"""
f.) What metric does the Class BFMatcher_create() use to compare the feature vectors?
What other metrics are supported?

BFMatcher_create() normally uses L2 (Euclidean) distance for SIFT. 
Other metrics: NORM_L1, NORM_L2, NORM_HAMMING, NORM_HAMMING2.
"""

# g.) Try crossCheck=True and compare
bf_cc = cv.BFMatcher(cv.NORM_L2, crossCheck=True)
matches_cc = bf_cc.match(des1, des2)
matches_cc = sorted(matches_cc, key=lambda m: m.distance)
print("Matches with crossCheck:", len(matches_cc))

img_matches_cc = cv.drawMatches(
    img1, kp1, img2, kp2, matches_cc, None,
    flags=cv.DrawMatchesFlags_NOT_DRAW_SINGLE_POINTS)
plt.figure()
plt.imshow(img_matches_cc, cmap='gray')
plt.title('SIFT matches (BF + crossCheck)')
plt.axis('off')
plt.show()

"""
g.) What does the flag crossCheck means?

crossCheck=True keeps only mutual best matches: 
A↔B must be each other’s best match. 
Fewer, cleaner matches — but you can’t use knnMatch in that mode.

i.) Why is the method findHomography() powerful but still in some cases cannot reject all
the outliers?

findHomography() with RANSAC is powerful because it can handle many outliers, but:
- some wrong matches can still accidentally fit the homography,
- too many outliers / bad distribution → wrong model,
- repeated patterns and big threshold also let outliers through.

j.) What kind of objects are the return value H, and mask?

- H: a 3×3 numpy.ndarray (float), the homography matrix.
- mask: a (N,1) uint8 array of 0/1 marking inliers vs outliers.

k.) How can H be used to transform image points from the reference to the query image?

To transform points from the reference image to the query image, use:
- either the homogeneous formula p′∼Hp
- or OpenCV’s cv2.perspectiveTransform() (like in the code).

l.) Explain in your own words what makes the strength of this object recognition method.

The strength of this method is the combination of:

- local invariant features (SIFT) → robust to scale, rotation, partial occlusion;
- distinctive descriptors → relatively reliable matches;
- geometric verification with homography + RANSAC → 
  many false matches are thrown away, and you get a clear geometric mapping of the object into the scene.
"""