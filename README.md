# rcount_face_recognition
Sample Face Recognition Project

# Project Information
- Raspberry Pi 3 B+
- IDS UV 1551LE-C Camera
    - Colour compression: yuv2, mjpg
    - Rolling Shutter


### To Run
`
python server.py
`

then

`
python camera.py
`

# TODO:
1. Motion detection on Raspberry Py
2. Video stream sampling options
3. Throwing out "bad" face detection. Linked with point 4.
4. Camera View is too wide. Detected face is quite small (Need to investigate how bad this can be)

5. Test with more sample data
6. Experiment with trained neural networks on raspberry pi. MxNet?
7. Evaluate face recognition model (how?)

8. False rejection rate estimation
    a. atleast 100 samples


# Done
1. Implemented Face Detection
4. Implemented Face Detection
2. Implement basic communication model client/server
3. Implement raspberry pi video capture w/open cv 
4. Implemented Face Recognition: Untested

1. take the crop, and remap
2. Intrinsic property of lens
    a. infer camera parameters
3. Camera global vs rolling shutter
    . global shutter time should not have blur. reduce exposure time
4. face detection quality classifier

# Use Conda
Makes life easier....
