from multiprocessing import pool
import multiprocessing as mp
import os
import cv2
from face_detection import get_input
from arcface.mtcnn_detector import MtcnnDetector
import mxnet as mx

if len(mx.test_utils.list_gpus())==0:
    print("MXNET: Using Cpu Mode")
    ctx = mx.cpu()
else:
    print("MXNET: Using GPU Mode")
    ctx = mx.gpu(0)

det_threshold = [0.6,0.7,0.8]
detector_mtcnn = None

def init_pool():
    global detector_mtcnn
    detector_mtcnn = MtcnnDetector(model_folder="./mtcnn_model", ctx=ctx, num_worker=1,
                                   accurate_landmark = True, threshold=det_threshold)

def get_pool():
    return pool.Pool(1, init_pool)

def test_func(path):
    global detector_mtcnn
    if os.path.exists(path):
        img = cv2.imread(path)
        face_server = get_input(detector_mtcnn, img)
        return face_server
    else:
        print("path {} doesn't exist.".format(path))


def foo(q, q_return):
    global detector_mtcnn
    if detector_mtcnn is None:
        init_pool()
    while True:
        data = q.get()
        frame = test_func(data)
        q_return.put(frame)

if __name__ == '__main__':
    mp.set_start_method('spawn')
    q = mp.Queue()
    q_return = mp.Queue()
    p = mp.Process(target=foo, args=(q,q_return))
    p.start()
    for orig, sub, files in os.walk("yalefaces"):
        for file in files:
            path = "./yalefaces/" + file
            q.put(path)
            frame = q_return.get()
            print(frame)
    p.join()

# if __name__ == "__main__":
#     init_pool()
#     test_func(0)
#     # p = get_pool()
#     # p.map(test_func, [0])
