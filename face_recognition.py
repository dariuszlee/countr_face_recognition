import cv2
import dlib
from dlib_hog_face_detection import get_frontal_dlib
import os
from arcface.mtcnn_detector import MtcnnDetector
from face_detection import get_input
from dlib_hog_face_detection import calculate_image_blur
import mxnet as mx
import numpy as np
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
    if os.path.exists("./passport_processed.jpg"):
        return cv2.imread("./passport_processed.jpg")
    det_threshold = [0.6,0.7,0.8]
    detector = MtcnnDetector(model_folder="./mtcnn_model", ctx=ctx, num_worker=1, accurate_landmark = True, threshold=det_threshold)
    reference_image = cv2.imread("./passport.jpg")

    processed = get_input(detector, reference_image)
    cv2.imwrite("./passport_processed.jpg", processed)
    return processed

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


def load_yale_embeddings_fast(model):
    embeddings = dict()
    detector = dlib.get_frontal_face_detector()
    for path in os.listdir("./yalefaces"):
        path = "./yalefaces/" + path
        reference_image = cv2.imread(path)
        detected_face = get_frontal_dlib(reference_image, detector,
                                       (112, 112))
        # cv2.imshow('path', detected_face)
        # print(detected_face.shape)
        # if cv2.waitKey(25) & 0xFF == ord('q'):
        #     break
        reference_me = transform_frame(detected_face)
        reference_embedding = get_feature(model, reference_me)

        path = path + ".fast"
        embeddings[path] = reference_embedding

    return embeddings


def check_against_embedding_db(db, to_check):
    greatest = (None, -29999)
    for name, db_embedding in db.items():
        similarity_score = np.dot(db_embedding, to_check)
        if similarity_score > greatest[1]:
            greatest = (name, similarity_score)
    return greatest


def main():
    # mx.test_utils.download('https://s3.amazonaws.com/onnx-model-zoo/arcface/resnet100.onnx')
    if len(mx.test_utils.list_gpus())==0:
        ctx = mx.cpu()
    else:
        ctx = mx.gpu(0)

    model_name = "./resnet100.onnx"
    model = get_model(ctx, model_name)

    yale_faces = load_yale_embeddings(ctx, model)

    cap = cv2.VideoCapture("./face_capture_blur_frontal.avi")
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

    total_vals = sum([v for k, v in most_likely.items()])
    total = most_likely['./yalefaces/trainer_reference.png']
    print("Raw Face Recognition", total/ total_vals * 100)
    cap.release()

    # cap = cv2.VideoCapture("./processed.avi")
    # most_likely = dict()
    # if not cap.isOpened():
    #     print("Error opening video stream or file")
    # while(cap.isOpened()):
    #     ret, frame = cap.read()
    #     if ret:
    #         # cv2.imshow('Image', frame)
    #         if not dlib_hog_face_detection.calculate_image_blur(frame):
    #             continue

    #         frame = transform_frame(frame)
    #         embedding = get_feature(model, frame)

    #         most_similar_embedding = check_against_embedding_db(yale_faces, embedding)
    #         if most_similar_embedding[0] in most_likely:
    #             most_likely[most_similar_embedding[0]] += 1
    #         else:
    #             most_likely[most_similar_embedding[0]] = 1
    #     else:
    #         break

    # total_vals = sum([v for k, v in most_likely.items()])
    # total = most_likely['./yalefaces/trainer_reference.png']
    # print("With blur detection: ", total/ total_vals * 100)
    # cap.release()

    # cap = cv2.VideoCapture("./processed_and_cleaned.avi")
    # most_likely = dict()
    # if not cap.isOpened():
    #     print("Error opening video stream or file")
    # while cap.isOpened():
    #     ret, frame = cap.read()
    #     if ret:
    #         # cv2.imshow('Image', frame)
    #         frame = transform_frame(frame)
    #         embedding = get_feature(model, frame)

    #         most_similar_embedding = check_against_embedding_db(yale_faces, embedding)
    #         if most_similar_embedding[0] in most_likely:
    #             most_likely[most_similar_embedding[0]] += 1
    #         else:
    #             most_likely[most_similar_embedding[0]] = 1
    #     else:
    #         break
    # total_vals = sum([v for k, v in most_likely.items()])
    # total = most_likely['./yalefaces/trainer_reference.png']
    # print("With blur and frontal detection: ", total/ total_vals * 100)


if __name__ == "__main__":
    main()
