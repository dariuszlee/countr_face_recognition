import cv2
import dlib

def calculate_image_blur(image):
    is_valid_threshold = 80

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    blur = cv2.Laplacian(gray, cv2.CV_64F).var()
    return blur > is_valid_threshold


if __name__ == "__main__":
    cap = cv2.VideoCapture("./processed.avi")
    while(cap.isOpened()):
        ret, frame = cap.read()
        if ret == True:
            cv2.imshow('Image', frame)
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            blur = cv2.Laplacian(gray, cv2.CV_64F).var()
            print(blur)
            __import__('ipdb').set_trace()

            if cv2.waitKey(25) & 0xFF == ord('q'):
                break
        else:
            break
