package countr.faceserver;

import org.apache.commons.lang3.SerializationUtils;

import countr.common.RecognitionMessage;
import countr.faceserver.IFaceServer;

import org.zeromq.ZMQ;
import org.zeromq.ZContext;


public class FaceServer implements IFaceServer{
    public void Start(){

    };

    public void Recognize(){

    }

    public static void main(String[] args){
        try (ZContext context = new ZContext()) {
            // Socket to talk to clients
            ZMQ.Socket socket = context.createSocket(ZMQ.REP);
            socket.bind("tcp://*:5555");

            while (!Thread.currentThread().isInterrupted()) {
                // Block until a message is received
                byte[] reply = socket.recv(0);
                RecognitionMessage yourObject = SerializationUtils.deserialize(reply);

                // Print the message
                System.out.println(
                    "Received: [" + new String(reply, ZMQ.CHARSET) + "]"
                );

                // Send a response
                String response = "Hello, world!";
                socket.send(response.getBytes(ZMQ.CHARSET), 0);
            }
        }
    }
}
