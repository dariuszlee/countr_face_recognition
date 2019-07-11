from flask import Flask, request
import matplotlib.pyplot as plt
import cv2
import numpy as np

app = Flask(__name__)

@app.route('/', methods=['POST'])
def index():
    data = request.files.get('image', '').stream.read()
    img_np = np.fromstring(data, dtype=np.uint8)
    img = cv2.imdecode(img_np, cv2.IMREAD_COLOR)[:,:, ::-1]
    plt.imshow(img)
    plt.show()
    return "success"

if __name__ == "__main__":
    app.run(debug=True)
