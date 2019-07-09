import cv2
import time
from matplotlib import pyplot as plt

def start_capture(video_device=2, debug_mode=True):
    video_capture = cv2.VideoCapture(video_device)

    # Check success
    if not video_capture.isOpened():
        raise Exception("Could not open video device")

    try:
        while True:
            # Read picture. ret === True on success
            ret, frame = video_capture.read()

            if ret:
                frameRGB = frame[:,:,::-1] # BGR => RGB
                if debug_mode:
                    plot_mathplotlib(frameRGB)
                else:
                    pass
    except Exception as e:
        print("Shutting down camera")
    finally:
        # Close device
        video_capture.release()


def plot_mathplotlib(frameRGB):
    plt.imshow(frameRGB)
    plt.pause(0.001)

if __name__ == "__main__":
    start_capture(2, True)
