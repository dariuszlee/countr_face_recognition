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

if False:
    if len(mx.test_utils.list_gpus())==0:
        print("MXNET: Using Cpu Mode")
        ctx = mx.cpu()
    else:
        print("MXNET: Using GPU Mode")
        ctx = mx.gpu(0)

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

    # print("Loading neural_net detector")
    det_threshold = [0.6,0.7,0.8]
    detector_mtcnn = None
    detector_mtcnn = MtcnnDetector(model_folder="./mtcnn_model", ctx=ctx, num_worker=1,
                            accurate_landmark = True, threshold=det_threshold)
    print("Finished Loading neural_net detector")

port = "5555"
if len(sys.argv) > 1:
    port =  sys.argv[1]
    int(port)

context = zmq.Context()
socket = context.socket(zmq.REP)
socket.bind("tcp://*:%s" % port)

while True:
    message = socket.recv()
    message_decoded = pickle.loads(message)
    print("message_decode: ", message_decoded['session'])
    socket.send(b"")
