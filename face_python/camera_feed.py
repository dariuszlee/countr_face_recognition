from flask import Flask, render_template, Response
import cv2

app = Flask(__name__)

@app.route('/')
def index():
    return "Working..."

def gen():
    video_capture = cv2.VideoCapture("/dev/video0")
    while True:
        ret, frame = video_capture.read()
        _, frame = cv2.imencode('.jpg', frame)
        frame = frame.tostring()
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n')

@app.route('/video_feed')
def video_feed():
    return Response(gen(),
                    mimetype='multipart/x-mixed-replace; boundary=frame')

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
