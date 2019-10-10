import pickle
import zmq
import time
import cv2

context = zmq.Context()

#  Socket to talk to server
print("Connecting to hello world server")
socket = context.socket(zmq.REQ)
socket.connect("tcp://192.168.0.168:5555")

video_device = "/dev/video0"

count = 0

start_time = time.time()
#  Do 10 requests, waiting each time for a response
video_capture = cv2.VideoCapture(video_device)
try:
    while True:
        # Read picture. ret === True on success
        ret, frame = video_capture.read()
        if ret:
            count += 1
        else:
            raise Exception()
        if count % 100 == 0:
            elapsed_time = time.time() - start_time
            print("Elapsed Time: ", elapsed_time)
            start_time = time.time()
        # send_to_server(frame, to_send_to)
        _, img_encoding = cv2.imencode('.jpg', frame)
        img_encoding_to_string = img_encoding.tostring()
        message = { "type": "data", 
                "session": "session1" 
                }
        message['data'] = img_encoding_to_string
        socket.send(pickle.dumps(message))
        message = socket.recv()
except Exception as e:
    print(e)
    print("Shutting down camera")
finally:
    # Close device
    video_capture.release()
