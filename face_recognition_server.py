from flask import Flask
from face_detection import get_input
from sklearn.preprocessing import normalize
import mxnet as mx
import os
import cv2
import numpy as np
from arcface.mtcnn_detector import MtcnnDetector
from mxnet.contrib.onnx.onnx2mx.import_model import import_model


def get_feature(model,aligned):
    input_blob = np.expand_dims(aligned, axis=0)
    data = mx.nd.array(input_blob)
    db = mx.io.DataBatch(data=(data,))
    model.forward(db, is_train=False)
    embedding = model.get_outputs()[0]
    embedding = embedding.asnumpy()
    embedding = normalize(embedding).flatten()
    return embedding

def transform_frame(frame):
    frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    aligned = np.transpose(frame, (2,0,1))
    return aligned


def get_model(ctx, model):
    image_size = (112,112)
    # Import ONNX model
    sym, arg_params, aux_params = import_model(model)
    # Define and binds parameters to the network
    model = mx.mod.Module(symbol=sym, context=ctx, label_names = None)
    model.bind(data_shapes=[('data', (1, 3, image_size[0], image_size[1]))])
    model.set_params(arg_params, aux_params)
    return model


def load_yale_embeddings(ctx, model):
    det_threshold = [0.6,0.7,0.8]
    detector = MtcnnDetector(model_folder="./mtcnn_model", ctx=ctx, num_worker=1, accurate_landmark = True, threshold=det_threshold)

    embeddings = dict()
    for path in os.listdir("./yalefaces"):
        path = "./yalefaces/" + path
        reference_image = cv2.imread(path)
        processed = get_input(detector, reference_image)
        reference_me = transform_frame(processed)
        reference_embedding = get_feature(model, reference_me)

        embeddings[path] = reference_embedding

    return embeddings



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
yale_faces = load_yale_embeddings(ctx, model)
print("Finished Loading db")

@app.route('/', methods=['POST'])
def index():
    __import__('ipdb').set_trace()
    print("Success")
    return "success"

if __name__ == "__main__":
    app.run(host="0.0.0.0", debug=True)
