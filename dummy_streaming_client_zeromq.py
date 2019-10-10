import os
import zmq
import pickle

context = zmq.Context()

print("Connecting to hello world server")
socket = context.socket(zmq.REQ)
socket.connect("tcp://192.168.0.168:5555")

try:
    while True:
        message = { "type": "data", 
                "session": "session2" 
                }
        message['data'] = os.urandom(71000)
        socket.send(pickle.dumps(message))
        message = socket.recv()
except Exception as e:
    print(e)
    print("Shutting down camera")
