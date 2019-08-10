import cv2
import imutils
def begin_capture():
    # vs = cv2.VideoCapture("./capture.mp4")
    vs = cv2.VideoCapture(0)
    frame_width = int(vs.get(cv2.CAP_PROP_FRAME_WIDTH))
    frame_height = int(vs.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = int(vs.get(cv2.CAP_PROP_FPS))
    four_cc = cv2.VideoWriter_fourcc('M','J','P','G')
    print("FPS", fps, four_cc)
    out = cv2.VideoWriter('outpy.avi', four_cc, fps, (frame_width, frame_height))
    out_transformed = cv2.VideoWriter('outpy_trans.avi', four_cc, fps, (frame_width, frame_height))

    firstFrame = None

    # loop over the frames of the video
    try:
        while True:
            # grab the current frame and initialize the occupied/unoccupied
            # text
            frame = vs.read()[1]
            # frame = frame if args.get("video", None) is None else frame[1]
            text = "Unoccupied"
            # if the frame could not be grabbed, then we have reached the end
            # of the video
            if frame is None:
                break
            # resize the frame, convert it to grayscale, and blur it
            # frame = imutils.resize(frame, width=500)
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            gray = cv2.GaussianBlur(gray, (21, 21), 0)
            # if the first frame is None, initialize it
            if firstFrame is None:
                firstFrame = gray
                continue
            out.write(frame)
            out_transformed.write(gray)
    finally:
        vs.release()
        out.release()
        out_transformed.release()
        cv2.destroyAllWindows()



# ap = argparse.ArgumentParser()
# ap.add_argument("-v", "--video", help="path to the video file")
# ap.add_argument("-a", "--min-area", type=int, default=500, help="minimum area size")
# args = vars(ap.parse_args())

# if the video argument is None, then we are reading from webcam
# if args.get("video", None) is None:
# 	vs = VideoStream(src=0).start()
# 	time.sleep(2.0)
begin_capture()
