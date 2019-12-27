package countr.faceserver;

import org.apache.commons.lang3.SerializationUtils;

import countr.common.RecognitionMessage;
import countr.faceserver.IFaceServer;

import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;

import java.nio.ByteBuffer;

import org.zeromq.ZMQ;
import org.zeromq.ZContext;


public class FaceServer implements IFaceServer{
    public void Start(){

    };

    public void Recognize(){

    }

    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        try (ZContext context = new ZContext()) {
            // Socket to talk to clients
            ZMQ.Socket socket = context.createSocket(ZMQ.REP);
            socket.bind("tcp://*:5555");

            while (!Thread.currentThread().isInterrupted()) {
                // Block until a message is received
                byte[] reply = socket.recv(0);
                RecognitionMessage yourObject = SerializationUtils.deserialize(reply);

                // Print the message
                System.out.println("Id: " + yourObject.getSender());
                byte[] data = yourObject.getImage();
                System.out.println("Id: " + yourObject.getImage().length);

                ByteBuffer buf = ByteBuffer.wrap(yourObject.getImage());
                MatOfByte matOfByte = new MatOfByte(yourObject.getImage());
                int width = yourObject.getWidth();
                int height = yourObject.getHeight();
                int type = yourObject.getType();
                Mat mat = new Mat(height, width, type);
                mat.put(0,0, data);
                // Mat mat2 = Imgcodecs.imdecode(mat, type);

                String outfile = String.format("/home/dzly/projects/countr_face_recognition/faceclient/output/%d.png", yourObject.getSender());
                boolean res = Imgcodecs.imwrite(outfile, mat);

                // Send a response
                String response = "Hello, world!";
                socket.send(response.getBytes(ZMQ.CHARSET), 0);
            }
        }
    }
}
