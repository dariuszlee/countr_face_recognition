import pickle
import time
import cv2

def load_img_db():
    data = pickle.load(open("./db/dump_img_data.pkl", 'rb'))
    for d, img in data.items():
        cv2.imshow("asdf", img)
        time.sleep(1)
        if cv2.waitKey(25) & 0xFF == ord('q'):
            break
if __name__ == "__main__":
    load_img_db()
