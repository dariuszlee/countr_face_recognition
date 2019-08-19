import cv2
import mxnet as mx
import numpy as np
import sklearn
from mxnet.contrib.onnx.onnx2mx.import_model import import_model


def get_feature(model,aligned):
    input_blob = np.expand_dims(aligned, axis=0)
    data = mx.nd.array(input_blob)
    db = mx.io.DataBatch(data=(data,))
    model.forward(db, is_train=False)
    embedding = model.get_outputs()[0].asnumpy()
    embedding = sklearn.preprocessing.normalize(embedding).flatten()
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


if __name__ == "__main__":
    if len(mx.test_utils.list_gpus())==0:
        ctx = mx.cpu()
    else:
        ctx = mx.gpu(0)

    model_name = "./resnet100.onnx"
    model = get_model(ctx, model_name)

    cap = cv2.VideoCapture("./processed.avi")
    if (cap.isOpened()== False):
        print("Error opening video stream or file")
    while(cap.isOpened()):
        ret, frame = cap.read()
        if ret == True:
            frame = transform_frame(frame)
            get_feature(model, frame)
            # __import__('ipdb').set_trace()

            # Press Q on keyboard to  exit
            if cv2.waitKey(25) & 0xFF == ord('q'):
                break
