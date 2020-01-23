import cv2
import time 
from matplotlib import pyplot as plt
import requests


def start_capture(video_device=2, ip_address=None, port="5000"):
    to_send_to = "http://" + ip_address + ":" + port + "/feed_video"
    print("Locals: ", locals())
    video_capture = cv2.VideoCapture(video_device)

    start_time = time.time()
    count = 0
    # Check success
    if not video_capture.isOpened():
        raise Exception("Could not open video device")
    try:
        while True:
            # Read picture. ret === True on success
            ret, frame = video_capture.read()
            if ret:
                count += 1
            if count % 100 == 0:
                elapsed_time = time.time() - start_time
                print("Elapsed Time: ", elapsed_time)
            send_to_server(frame, to_send_to)
    except Exception as e:
        print(e)
        print("Shutting down camera")
    finally:
        # Close device
        video_capture.release()


import pdb
def send_to_server(frame, url):
    _, img_encoding = cv2.imencode('.jpg', frame)
    # pdb.set_trace()
    img_encoding_to_string = img_encoding.tostring()
    files = {'image': ('data_dar.jpg', img_encoding_to_string, 'multipart/form-data')}
    try:
        status = requests.post(url,files=files)
    except:
        pass


if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description='Process some integers.')
    parser.add_argument('--ip', type=str, required=True,
                        help='The ip address of the face recognition server.')
    args = parser.parse_args()
    print(args.ip)
    start_capture("/dev/video0", args.ip)
    # Debug modes: mp, file, server
    # start_capture(2, "mp")
    # start_capture(0, "server")
    # send_to_server(None)
