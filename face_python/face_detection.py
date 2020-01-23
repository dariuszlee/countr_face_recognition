import os
import cv2
import numpy as np
from skimage import transform as trans

import mxnet as mx

from arcface.mtcnn_detector import MtcnnDetector

def preprocess(img, bbox=None, landmark=None, **kwargs):
    """
    Preprocess input image - returns aligned face images
    """
    M = None
    image_size = []
    str_image_size = kwargs.get('image_size', '')
    # Assert input image shape
    if len(str_image_size)>0:
        image_size = [int(x) for x in str_image_size.split(',')]
        if len(image_size)==1:
            image_size = [image_size[0], image_size[0]]
            assert len(image_size)==2
            assert image_size[0]==112
            assert image_size[0]==112 or image_size[1]==96
    # Do alignment using landmnark points
    if landmark is not None:
        assert len(image_size)==2
        src = np.array([
            [30.2946, 51.6963],
            [65.5318, 51.5014],
            [48.0252, 71.7366],
            [33.5493, 92.3655],
            [62.7299, 92.2041] ], dtype=np.float32 )
        if image_size[1]==112:
            src[:,0] += 8.0
        dst = landmark.astype(np.float32)

    tform = trans.SimilarityTransform()
    tform.estimate(dst, src)
    M = tform.params[0:2,:]

    # If no landmark points available, do alignment using bounding box. If no bounding box available use center crop
    if M is None:
        if bbox is None: #use center crop
            det = np.zeros(4, dtype=np.int32)
            det[0] = int(img.shape[1]*0.0625)
            det[1] = int(img.shape[0]*0.0625)
            det[2] = img.shape[1] - det[0]
            det[3] = img.shape[0] - det[1]
        else:
            det = bbox
            margin = kwargs.get('margin', 44)
            bb = np.zeros(4, dtype=np.int32)
            bb[0] = np.maximum(det[0]-margin/2, 0)
            bb[1] = np.maximum(det[1]-margin/2, 0)
            bb[2] = np.minimum(det[2]+margin/2, img.shape[1])
            bb[3] = np.minimum(det[3]+margin/2, img.shape[0])
            ret = img[bb[1]:bb[3],bb[0]:bb[2],:]
            if len(image_size)>0:
                ret = cv2.resize(ret, (image_size[1], image_size[0]))
                return ret
            else: #do align using landmark
                assert len(image_size)==2

    # warped = cv2.warpAffine(img,M,(image_size[1],image_size[0]), borderValue = 0.0)
    warped = cv2.warpAffine(img,M,(image_size[1],image_size[0]), borderValue = 0.0)

    return warped

def get_input(detector,face_img):
    # Pass input images through face detector
    ret = detector.detect_face(face_img, det_type = 0)
    if ret is None:
        return None
    bbox, points = ret
    print("bbox", len(bbox))
    if bbox.shape[0]==0:
        return None
    nimg = []
    for bb, po in zip(bbox, points):
        bb = bb[0:4]
        po = po[:].reshape((2,5)).T
        # Call preprocess() to generate aligned images
        nimg_temp = preprocess(face_img, bb, po, image_size='112,112')
        nimg.append(nimg_temp)
        
    return nimg
    # nimg = cv2.cvtColor(nimg, cv2.COLOR_BGR2RGB)
    # aligned = np.transpose(nimg, (2,0,1))
    # return aligned


def main_1():
    cap = cv2.VideoCapture("./outpy.avi")

    frames = []

    if not cap.isOpened():
        print("Error opening video stream or file")
    # Read until video is completed
    while cap.isOpened():
        # Capture frame-by-frame
        ret, frame = cap.read()
        if ret == True:

        # Display the resulting frame
            frames.append(frame)
            # cv2.imshow('Frame',frame)

            # Press Q on keyboard to  exit
            if cv2.waitKey(25) & 0xFF == ord('q'):
                break
        # Break the loop
        else:
            break


    if len(mx.test_utils.list_gpus())==0:
        ctx = mx.cpu()
    else:
        ctx = mx.gpu(0)
    # Configure face detector
    det_threshold = [0.6,0.7,0.8]

    detector = MtcnnDetector(model_folder="./mtcnn_model", ctx=ctx, num_worker=1, accurate_landmark = True, threshold=det_threshold)

    face_detected = []

    for frame in frames:
        processed = get_input(detector, frame)
        if processed is not None:
            face_detected.append(processed)
            # cv2.imshow('Image', processed)
        # if cv2.waitKey(25) & 0xFF == ord('q'):
        #     break

    video = cv2.VideoWriter("./processed.avi", 0, 1, (112,112))
    for image in face_detected:
        if image is not None:
            video.write(image)
    video.release()



    # Closes all the frames
    cv2.destroyAllWindows()

    # When everything done, release the video capture object
    cap.release()

def main_2():
    """

    """
    if len(mx.test_utils.list_gpus())==0:
        ctx = mx.cpu()
    else:
        ctx = mx.gpu(0)
    # Configure face detector
    det_threshold = [0.6,0.7,0.8]
    detector = MtcnnDetector(model_folder="./mtcnn_model", ctx=ctx, num_worker=1, accurate_landmark = True, threshold=det_threshold)

    for directory, sub, file_names in os.walk("./example_face_from_countr/ExampleDataFaceRecognition/ID_Images"):
        for file_name in file_names:
            full_path = directory + "/" + file_name
            img = cv2.imread(full_path)
            img_2 = get_input(detector, img)
            cv2.imshow("da", img)
            cv2.imshow("da_2", img_2)
            __import__('ipdb').set_trace()
            if cv2.waitKey(25) & 0xFF == ord('q'):
                break


def main_3():
    """
    """
    def worker():
        if len(mx.test_utils.list_gpus())==0:
            ctx = mx.cpu()
        else:
            ctx = mx.gpu(0)
        # Configure face detector
        det_threshold = [0.6,0.7,0.8]
        detector = MtcnnDetector(model_folder="./mtcnn_model", ctx=ctx, num_worker=1, accurate_landmark = True, threshold=det_threshold)
        while True:
            d = msg_queue.get()
            if not os.path.exists(d):
                print("Image ", d, " doesn't exist.")
                continue
            if d is None:
                break
            img = cv2.imread(d)
            img_2 = get_input(detector, img)
            db[d] = img_2
    import queue
    import threading
    msg_queue = queue.Queue()
    db = {}
    thrd = threading.Thread(target=worker)
    thrd.start()

    for directory, sub, file_names in os.walk("./example_face_from_countr/ExampleDataFaceRecognition/ID_Images"):
        for file_name in file_names:
            full_path = directory + "/" + file_name
            msg_queue.put(full_path)
    msg_queue.put(None)


if __name__ == "__main__":
    main_3()
