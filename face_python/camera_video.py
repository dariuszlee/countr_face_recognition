import cv2
import os
import argparse
from dlib_hog_face_detection import (get_detector, check_frontal_and_blur,
                                     get_frontal_dlib)


def process_capture(file_name, output_directory):
    if not os.path.exists(output_directory):
        print("Output directory doesn't exists")
        exit(1)
    vs = cv2.VideoCapture(file_name)
    count = 0
    detector = get_detector()
    desired_size = (112, 112)

    while True:
        ret, frame = vs.read()
        if ret:
            if check_frontal_and_blur(frame, detector, desired_size):
                print("Found", count)
                cv2.imwrite("{}/frame_{}.png".format(output_directory, count),
                            frame)
                count += 1
        else:
            break



def begin_capture(video_num, output_file, num_to_find=100):
    # vs = cv2.VideoCapture("./capture.mp4")
    vs = cv2.VideoCapture(video_num)
    frame_width = int(vs.get(cv2.CAP_PROP_FRAME_WIDTH))
    frame_height = int(vs.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = int(vs.get(cv2.CAP_PROP_FPS))
    four_cc = cv2.VideoWriter_fourcc('F','M','P','4')
    out = cv2.VideoWriter(output_file, four_cc, fps, (frame_width, frame_height))

    detector = get_detector()
    desired_size = (112, 112)

    # loop over the frames of the video
    count = 0
    try:
        while True:
            ret, frame = vs.read()
            if ret:
                out.write(frame)
                if check_frontal_and_blur(frame, detector, desired_size):
                    count += 1
                    print("Found")
                if count == num_to_find:
                    break
            else:
                break
    except Exception as e:
        __import__('ipdb').set_trace()
        print(e)
    finally:
        print("Exiting video capture...")
        vs.release()
        out.release()
        cv2.destroyAllWindows()


def store_dlib_images(input_dir, output_dir):
    detector = get_detector()
    for path in os.listdir(input_dir):
        print(path)
        full_input_path = input_dir + "/" + path
        full_output_path = output_dir + "/" + path

        frame = cv2.imread(full_input_path)
        frame = get_frontal_dlib(frame, detector, (112,112))
        cv2.imwrite(full_output_path, frame)


if __name__ == "__main__":
    # ap = argparse.ArgumentParser()
    # ap.add_argument("-v", "--video", help="path to the video file")
    # ap.add_argument("-c", "--camera", help="camera device to choose")
    # args = vars(ap.parse_args())
    # begin_capture(int(args.get("camera", 0)), args.get('video', "default_videol.avi"))
    # process_capture("./test_video.avi", 'counter')
    store_dlib_images("./counter", "./counter_processed")
