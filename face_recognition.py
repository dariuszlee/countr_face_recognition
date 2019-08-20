import cv2
import mxnet as mx
import numpy as np
import sklearn
from mxnet.contrib.onnx.onnx2mx.import_model import import_model
from sklearn.preprocessing import normalize


def get_feature(model,aligned):
    input_blob = np.expand_dims(aligned, axis=0)
    data = mx.nd.array(input_blob)
    db = mx.io.DataBatch(data=(data,))
    model.forward(db, is_train=False)
    embedding = model.get_outputs()[0]
    embedding = embedding.asnumpy()
    embedding = normalize(embedding).flatten()
    return embedding

def get_model(ctx, model):
    image_size = (112,112)
    # Import ONNX model
    sym, arg_params, aux_params = import_model(model)
    # Define and binds parameters to the network
    model = mx.mod.Module(symbol=sym, context=ctx, label_names = None)
    model.bind(data_shapes=[('data', (1, 3, image_size[0], image_size[1]))])
    model.set_params(arg_params, aux_params)
    return model

def transform_frame(frame):
    frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    aligned = np.transpose(frame, (2,0,1))
    return aligned

def load_reference_embeddings(ctx):
    import os
    if os.path.exists("./passport_processed.jpg"):
        return cv2.imread("./passport_processed.jpg")
    from arcface.mtcnn_detector import MtcnnDetector
    from face_detection import get_input
    det_threshold = [0.6,0.7,0.8]
    detector = MtcnnDetector(model_folder="./mtcnn_model", ctx=ctx, num_worker=1, accurate_landmark = True, threshold=det_threshold)
    reference_image = cv2.imread("./passport.jpg")

    processed = get_input(detector, reference_image)
    cv2.imwrite("./passport_processed.jpg", processed)
    return processed


if __name__ == "__main__":
    mx.test_utils.download('https://s3.amazonaws.com/onnx-model-zoo/arcface/resnet100.onnx')
    if len(mx.test_utils.list_gpus())==0:
        ctx = mx.cpu()
    else:
        ctx = mx.gpu(0)


    model_name = "./resnet100.onnx"
    model = get_model(ctx, model_name)

    reference_me = load_reference_embeddings(ctx)
    reference_me = transform_frame(reference_me)
    reference_embedding = get_feature(model, reference_me)

    cap = cv2.VideoCapture("./processed.avi")
    if (cap.isOpened()== False):
        print("Error opening video stream or file")
    while(cap.isOpened()):
        ret, frame = cap.read()
        if ret == True:
            cv2.imshow('Image', frame)
            frame = transform_frame(frame)
            embedding = get_feature(model, frame)

            print("Current dot:", np.dot(embedding, reference_embedding))

            if cv2.waitKey(25) & 0xFF == ord('q'):
                break
