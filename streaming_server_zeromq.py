import zmq
import time
import sys
import pickle

from arcface.mtcnn_detector import MtcnnDetector
import mxnet as mx
import cv2
from face_recognition import (check_against_embedding_db, get_feature,
                              transform_frame, get_model, load_yale_embeddings,
                              load_yale_embeddings_fast)
import os
from face_detection import get_input
from message_types import MESSAGE_CLEAR_SESSION, MESSAGE_CAMERA_FEED
import numpy as np

def add_to_scores(most_similar_embedding, session_id):
    global sessions
    session_data = sessions.get(session_id, {})
    for k, v in most_similar_embedding:
        current_score = session_data.get(k, 0)
        current_score += v
        session_data[k] = current_score
    sessions[session_id] = session_data

if True:
    if len(mx.test_utils.list_gpus())==0:
        print("MXNET: Using Cpu Mode")
        ctx = mx.cpu()
    else:
        print("MXNET: Using GPU Mode")
        ctx = mx.gpu(0)

    # print("Loading neural_net detector")
    det_threshold = [0.6,0.7,0.8]
    detector_mtcnn = None
    detector_mtcnn = MtcnnDetector(model_folder="./mtcnn_model", ctx=ctx, num_worker=1,
                            accurate_landmark = True, threshold=det_threshold)
    print("Finished Loading neural_net detector")

if True:
    model_name = "./resnet100.onnx"
    print("Loading Model")
    model = get_model(ctx, model_name)
    print("Finished Loading Model")

    print("Loading db")
    if os.path.exists("db/dump.pkl"):
        print("DB: Loading from file...")
        yale_faces = __import__('pickle').load(open("db/dump.pkl",
                                                    "rb"))
    else:
        print("DB: Recomputing")
        yale_faces = load_yale_embeddings(ctx, model)

    if os.path.exists("db/dump_img_data.pkl"):
        print("DB: Loading from file...")
        yale_faces_data = __import__('pickle').load(open("db/dump_img_data.pkl",
                                                        "rb"))
    else:
        yale_faces_data = {}
    # yale_faces.update(load_yale_embeddings_fast(model))
    print("Finished Loading db")

port = "5555"
if len(sys.argv) > 1:
    port =  sys.argv[1]
    int(port)

context = zmq.Context()
socket = context.socket(zmq.REP)
socket.bind("tcp://*:%s" % port)
print("Finished setting up sockets")

sessions = {}
try:
    socket.send(b"")
except:
    pass

while True:
    message = socket.recv()
    message_decoded = pickle.loads(message)
    print("message_decode: ", message_decoded['session'])
    message_session = message_decoded['session']
    message_type = message_decoded['type']
    message_data = message_decoded['data']
    if message_type == MESSAGE_CLEAR_SESSION:
        del message_session['session']
    elif message_type == MESSAGE_CAMERA_FEED:
        data = np.frombuffer(message_data, dtype=np.uint8)
        img = cv2.imdecode(data, cv2.IMREAD_COLOR)

        face_server = get_input(detector_mtcnn, img)
        if face_server == None:
            socket.send(b"")
            continue
        for face in face_server:
            frame = transform_frame(face)
            embedding = get_feature(model, frame)

            most_similar_embedding = check_against_embedding_db(yale_faces, embedding)
            add_to_scores(most_similar_embedding, message_session)
    else:
        print("Invalid message type: ", message_type)
    socket.send(b"")

