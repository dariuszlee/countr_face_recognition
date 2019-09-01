import cv2
import dlib
import camera_details

def calculate_image_blur(image):
    image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    is_valid_threshold = 500
    blur = cv2.Laplacian(image, cv2.CV_64F).var()
    print(blur)
    return blur > is_valid_threshold

def get_frontal_dlib(frame, detector, desired_size):
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    detected = detector(gray, 0)
    if len(detected) != 0:
        detected_face = detected[0]
        left, right, top, bottom = scale_rectangle(detected_face,
                                                   desired_size)
        new_frame = frame[top:bottom,
                          left:right]
        return new_frame
    return None

def scale_rectangle(rect, desired_size):
    left, right = rect.left(), rect.right()
    # If Odd
    if right - left % 2 == 1:
        right += 1
    curr_size = right - left
    to_add_to_both_sides = int((desired_size[0] - curr_size) / 2)
    left -= to_add_to_both_sides
    right += to_add_to_both_sides

    top, bottom = rect.top(), rect.bottom()
    # If Odd
    if bottom - top % 2 == 1:
        bottom += 1
    curr_size = bottom - top
    to_add_to_both_sides = int((desired_size[1] - curr_size) / 2)
    top -= to_add_to_both_sides
    bottom += to_add_to_both_sides

    return left, left + desired_size[0], top, top + desired_size[1]



def main():
    cap = cv2.VideoCapture("./face_capture.avi")
    detector = dlib.get_frontal_face_detector()
    desired_size = (112, 112)

    valids = []
    while(cap.isOpened()):
        ret, frame = cap.read()
        if ret is True:
            new_frame = get_frontal_dlib(frame, detector, desired_size)
            if new_frame is None:
                continue
            if not calculate_image_blur(new_frame):
                continue

            valids.append(new_frame)
            cv2.imshow('Image', new_frame)
            if cv2.waitKey(25) & 0xFF == ord('q'):
                break
        else:
            break

    video = cv2.VideoWriter("./face_capture_blur_frontal.avi",
                            camera_details.four_cc,
                            camera_details.fps,
                            (112,
                             112))
    print(len(valids))
    for image in valids:
        if image is not None:
            video.write(image)
    video.release()

if __name__ == "__main__":
    main()
