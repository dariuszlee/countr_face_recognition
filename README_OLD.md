# CountR Api Documentation

## Introduction / Quick Start

This is a proposal for the simplest use of a FaceRecognition system.

### Server
Simply start the server.
```bash
java -jar server.jar -port 6000
```

#### Scaling the server

If we are finding that we need to more server compute power (ie. server response time is too slow), we can configure another server on another port.

```bash
java -jar server.jar -port 6000
java -jar server.jar -port 6001
...
```

Clients would simply need to connect to a different port. 

The limit for number of servers is likely the size of GPU memory.

### Client
The client code needs some decisions. Here are two possible proposals.

The code itself can be delivered as a Jar dependency through Maven, for example.

#### Proposal 1
From application code, we can start a session and query a session.
```java
FaceRecognitionSession session = FaceRecognition.createSession(6000);
// Do some work
FaceRecognitionInfo info = session.GetSessionInfo();
// Do some other work
info = session.GetSessionInfo();
session.close()
```

FaceRecognitionInfo would look something like this.

```java
class FaceRecognitionInfo {
    int number_of_frames_analyzed;
    List<SimilarityScore> similarityScores;
}
```

##### Pros
1. This code runs in the background in a thread (doesn't block the main thread).
2. Can process more frames per second, increasing accuracy over time

#### Proposal 2
From application code, we can run our face recognition in a loop, querying the server in each iteration.

Instead code may look like this.
```java
FaceRecognitionClient client = FaceRecognition.createClient(6000);
while(true){
    List<SimilarityScore> scores = client.GetScore();
    // Do some stuff
}
```

##### Pros
1. Simpler and leaves implementation in the control of the user.


## Conclusion

This paper requires a decision if this implementation would be acceptable for CountR's current system.
