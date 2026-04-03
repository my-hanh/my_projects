import cv2
import os

output_size = (500, 500)
face_cascade = cv2.CascadeClassifier("haarcascade_frontalface_default.xml")

folder = "input_face_images"
images = [f for f in os.listdir(folder) if f.lower().endswith(('.png', '.jpg', '.jpeg'))]

while True:
    print("\nAvailable images:")
    for idx, img_name in enumerate(images):
        print(f"{idx}: {img_name}")
    print("q: Quit")

    choice = input("Enter the number of the image you want to use (or 'q' to quit): ")
    if choice.lower() == 'q':
        break
    if not choice.isdigit() or int(choice) < 0 or int(choice) >= len(images):
        print("Invalid choice. Try again.")
        continue

    image_path = os.path.join(folder, images[int(choice)])
    image = cv2.imread(image_path)

    height, width = image.shape[:2]
    if width > output_size[0] or height > output_size[1]:
        scale = min(output_size[0] / width, output_size[1] / height)
        new_size = (int(width * scale), int(height * scale))
        image = cv2.resize(image, new_size)

    gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    faces = face_cascade.detectMultiScale(gray_image, scaleFactor=1.1, minNeighbors=10)

    for (x, y, width, height) in faces:
        cv2.rectangle(image, (x, y), (x+width, y+height), (0, 255, 255), 2)

    cv2.imshow("Detected Faces", image)
    cv2.waitKey()
