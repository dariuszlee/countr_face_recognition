from flask import Flask, request
import mxnet as mx
import cv2
from face_recognition import (check_against_embedding_db, get_feature,
                              transform_frame, get_model, load_yale_embeddings)


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
            # cv2.imshow('Frame',frame)

            # # Press Q on keyboard to  exit
            # if cv2.waitKey(25) & 0xFF == ord('q'):
            #     break
        else:
            break
    cap.release()
    __import__('pprint').pprint(most_likely)
    return "Sucess"

if __name__ == "__main__":
    app.run(host="0.0.0.0", debug=True)
