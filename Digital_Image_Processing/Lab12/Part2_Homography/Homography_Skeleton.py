
import numpy as np
import matplotlib.pyplot as plt


FIGURE_SIZE = (12, 9)
# Bilder einlesen
imgFolder = ''

#imgName = 'HausPerspektivisch.png'
imgName = 'chessboard_perspective.jpg'

img_persp = plt.imread(imgFolder+imgName)

plt.figure(figsize=FIGURE_SIZE)
plt.imshow(img_persp)
plt.show()

# Coordiantes are defined as [vertical, horizontal]
if imgName == "chessboard_perspective.jpg":
    Ps = np.array([[48, 385]])   # TODO: Additional Points go here
    points_u = ()# TODO: Coordinates of undistorted Points go here

def plot_points_on_image(img: np.ndarray, points: np.ndarray):
    plt.figure()
    plt.imshow(img)
    for point in points:
        plt.plot(point[1], point[0], 'o')    
    plt.title("image with points at specified coordinates")    
    plt.xlabel("coordinate 1")
    plt.ylabel("coordinate 0")
    plt.show()
    
plot_points_on_image(img_persp, Ps)

# TODO: Please add your code here
