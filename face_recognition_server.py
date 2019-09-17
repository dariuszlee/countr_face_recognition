from flask import Flask, request
from arcface.mtcnn_detector import MtcnnDetector
import mxnet as mx
import cv2
from face_recognition import (check_against_embedding_db, get_feature,
                              transform_frame, get_model, load_yale_embeddings,
                              load_yale_embeddings_fast)
import os
import dlib_hog_face_detection
from face_detection import get_input


class FaceRecognitionServer(Flask):
    def __init__(self, *args, **kwargs):
        # self.big_resource = np.arange(100)
        # setattr(self, 'some_resource', np.arange(100))
        super().__init__(*args, **kwargs)

app = FaceRecognitionServer(__name__)
if len(mx.test_utils.list_gpus())==0:
    ctx = mx.cpu()
else:
    ctx = mx.gpu(0)

model_name = "./resnet100.onnx"
print("Loading Model")
model = get_model(ctx, model_name)
print("Finished Loading Model")

print("Loading db")
if os.path.exists("db/dump.pkl"):
    yale_faces = __import__('pickle').load(open("db/dump.pkl",
                                                "rb"))
else:
    yale_faces = load_yale_embeddings(ctx, model)
# yale_faces.update(load_yale_embeddings_fast(model))
print("Finished Loading db")

print("Loading fast detector")
detector = dlib_hog_face_detection.get_detector()
print("Finished loading fast detector")

print("Loading neural_net detector")
det_threshold = [0.6,0.7,0.8]
detector_mtcnn = MtcnnDetector(model_folder="./mtcnn_model", ctx=ctx, num_worker=1,
                         accurate_landmark = True, threshold=det_threshold)
print("Finished Loading neural_net detector")

@app.route('/video', methods=['GET'])
def index():
    video_name = request.args.get('name')
    cap = cv2.VideoCapture(video_name)
    most_likely = dict()
    if not cap.isOpened():
        print("Error opening video stream or file")
    while(cap.isOpened()):
        ret, frame = cap.read()
        if ret:
            frame = transform_frame(frame)
            embedding = get_feature(model, frame)

            most_similar_embedding = check_against_embedding_db(yale_faces, embedding)
            if most_similar_embedding[0] in most_likely:
                most_likely[most_similar_embedding[0]] += 1
            else:
                most_likely[most_similar_embedding[0]] = 1
        else:
            break
    cap.release()
    __import__('pprint').pprint(most_likely)
    return "Sucess"

@app.route('/image_raw', methods=['GET'])
def image_raw():
    image_name = request.args.get('name')
    if not os.path.exists(image_name):
        return "Path doesn't exists"
    img = cv2.imread(image_name)
    img = dlib_hog_face_detection.get_frontal_dlib(img, detector, (112,112))
    frame = transform_frame(img)
    embedding = get_feature(model, frame)

    most_similar_embedding = check_against_embedding_db(yale_faces, embedding)
    print(most_similar_embedding[0])
    return "Success"


@app.route('/add_to_db', methods=['GET'])
def add_to_db():
    image_name = request.args.get('name')
    img = cv2.imread(image_name)
    processed = get_input(detector_mtcnn, img)
    frame = transform_frame(processed)
    embedding = get_feature(model, frame)
    yale_faces[image_name] = embedding
    return "Success"


@app.route('/dump_db', methods=['GET'])
def dump_db():
    __import__('pickle').dump(yale_faces, open("./db/dump.pkl", "wb"))
    print("Keys in db:", list(yale_faces.keys()))
    return "Success"

@app.route('/dump_db', methods=['GET'])
def clear_db():
    yale_faces = {}
    return "Success"


if __name__ == "__main__":
    app.run(host="0.0.0.0", debug=False)
