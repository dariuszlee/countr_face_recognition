# CountR Face Recognition Project

A Client-Server Face Recognition Project. Client can be any device and has been tested on Linux x86/ARM systems.

## Quickstart

### Server
1. git clone https://github.com/dariuszlee/countr_face_recognition.git
2. git submodule update --init --recursive
3. cd mtcnn-java && mvn install && cd ..
3. cd facecommon && mvn install && cd ..
4. cd faceserver && mvn clean && mvn package && mvn exec:java -PServer
    a. Let this run

### Client
1. git clone https://github.com/dariuszlee/countr_face_recognition.git
2. cd facecommon && mvn install && cd ..
3. cd faceclient && mvn clean && mvn -Dtest=ClientUsageExample1 test
