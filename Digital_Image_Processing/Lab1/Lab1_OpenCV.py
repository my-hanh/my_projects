import cv2

image_path = "pics/lena_gray.gif"
#image_path = "pics/lena_color.gif"
image = cv2.imread(image_path)

width = image.shape[1]
height = image.shape[0]



print("Return Type: " + str(type(image)))
print("Shape of image array: " + str(image.shape))
print("Image size: " + str(width) + "x" + str(height))
print("Number of channels: " + str(len(image.shape)))

cv2.imwrite("saved_pics/saved_img_OpenCV.png", image)

gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

cv2.imshow("Image", image)
#cv2.imshow("Grayscale Image", gray_image)
cv2.waitKey(0)
cv2.destroyAllWindows()
