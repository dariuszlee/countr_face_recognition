import cv2
from time import sleep
from matplotlib import pyplot as plt
import requests


def start_capture(video_device=2, debug_mode=True):
    print("Locals: ", locals())
    video_capture = cv2.VideoCapture(video_device)

    # Check success
    if not video_capture.isOpened():
        raise Exception("Could not open video device")

    try:
        while True:
            # Read picture. ret === True on success
            ret, frame = video_capture.read()

            if ret:
                if debug_mode == 'mp':
                    frameRGB = frame[:,:,::-1] # BGR => RGB
                    plot_mathplotlib(frameRGB)
                elif debug_mode == 'file':
                    write_to_file(frame)
                    sleep(1)
                elif debug_mode == 'server':
                    send_to_server(frame)
                    sleep(1)
    except Exception as e:
        print(e)
        print("Shutting down camera")
    finally:
        # Close device
        video_capture.release()


def plot_mathplotlib(frameRGB):
    plt.imshow(frameRGB)
    plt.pause(0.001)

image = 1
def write_to_file(frame):
    global image
    if image % 5 == 0:
        image = 1
    cv2.imwrite('./sample_images/data_{}.jpg'.format(image), frame)
    image += 1

def send_to_server(frame):
    url = "http://192.168.178.123:5000"
    _, img_encoding = cv2.imencode('.jpg', frame)
    files = {'image': ('data_dar.jpg', img_encoding.tostring(), 'multipart/form-data')}
    status = requests.post(url,files=files)
    print(status)

if __name__ == "__main__":
    # Debug modes: mp, file, server
    start_capture(2, "mp")
    start_capture(0, "server")
    # send_to_server(None)
