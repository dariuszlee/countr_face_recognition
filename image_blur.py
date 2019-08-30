import cv2
import dlib

def calculate_image_blur(image):
    is_valid_threshold = 80

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    blur = cv2.Laplacian(gray, cv2.CV_64F).var()
    return blur > is_valid_threshold

def get_frontal_dlib(image):
    pass

if __name__ == "__main__":
    cap = cv2.VideoCapture("./processed.avi")
    detector = dlib.get_frontal_face_detector()

    valids = []
    while(cap.isOpened()):
        ret, frame = cap.read()
        if ret == True:
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            if not calculate_image_blur(frame):
                continue
            detected = detector(gray, 2)
            if len(detected) == 0:
                continue

            valids.append(frame)
            # cv2.imshow('Image', frame)
            # if cv2.waitKey(25) & 0xFF == ord('q'):
            #     break
        else:
            break

    video = cv2.VideoWriter("./processed_and_cleaned.avi", 0, 1, (112, 112))
    for image in valids:
        if image is not None:
            video.write(image)
    video.release()

