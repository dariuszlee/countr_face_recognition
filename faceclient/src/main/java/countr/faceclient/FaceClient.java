package countr.faceclient;

import countr.faceclient.IFaceClient;
import countr.common.RecognitionMessage;

import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import org.apache.commons.lang3.SerializationUtils;

public class FaceClient implements IFaceClient
{
    enum State {
        Closed,
        Running
    }
    private State state;
    private VideoCapture frameGrabber;

    public FaceClient() {
        state = State.Closed;
    }

    @Override
    public FaceRecognitionInfo GetSessionInfo(){
        return new FaceRecognitionInfo();
    }

    @Override
    public void Identify(){
        // frameGrabber = new VideoInputFrameGrabber(0);
        frameGrabber = new VideoCapture(0);
        try {
            // frameGrabber.start();
           for(int i = 0; i < 10; i++){
               Mat matrix = new Mat();
               System.out.println(matrix.type());
               boolean is_grabbed = frameGrabber.read(matrix);
               Imgcodecs.imwrite((i++) + "-aa.jpg", matrix);
           }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Close(){

    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        // FaceClient faceClient = new FaceClient();
        // faceClient.Identify();
        // WHAT ARE YOU DOING RIGHT NOW?
        // You are creating the zeromq connection. You need to get a matrix to bytes and send over the line....
        Mat mat = Imgcodecs.imread("/home/dzly/projects/countr_face_recognition/yalefaces/subject01.normal.jpg.png");
        int channels = mat.channels();
        int type =  mat.type();
        int depth =  mat.depth();
        int height =  mat.height();
        int width =  mat.width();
        byte[] b = new byte[height * width * channels];
        mat.get(0,0, b);
        // MatOfByte matOfByte = new MatOfByte();
        // Imgcodecs.imencode(".png", mat, matOfByte);

        // byte[] b = matOfByte.toArray();
        System.out.println("Length of bytes "+ b.length);
        System.out.println("width "+ width);
        System.out.println("height "+ height);
        System.out.println("type "+ type);
        System.out.println("depth "+ depth);
        System.out.println("channels "+ mat.channels());


        try (ZContext context = new ZContext()) {
            //  Socket to talk to server
            System.out.println("Connecting to hello world server");

            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://localhost:5555");

            for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
                String request = "Hello";
                System.out.println("Sending Hello " + requestNbr);

                // String outfile = String.format("/home/dzly/projects/countr_face_recognition/faceclient/out2/%d.png", mat);
                // boolean res = Imgcodecs.imwrite(outfile, mat);

                RecognitionMessage message = new RecognitionMessage(b, type, height, width, requestNbr);
                byte[] messageData = SerializationUtils.serialize(message);
                socket.send(messageData, 0);

                byte[] reply = socket.recv(0);
                System.out.println(
                        "Received " + new String(reply, ZMQ.CHARSET) + " " +
                        requestNbr);
            }
        }
    }
}