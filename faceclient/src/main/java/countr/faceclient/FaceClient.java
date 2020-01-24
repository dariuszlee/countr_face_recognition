package countr.faceclient;

import countr.faceclient.IFaceClient;
import countr.common.RecognitionMessage;

import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Scalar;
import org.opencv.core.CvType;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import org.apache.commons.lang3.SerializationUtils;
import java.util.UUID;

import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


public class FaceClient implements IFaceClient
{
    enum State {
        Closed,
        Running
    }
    private String connectionString;

    private State state;
    private VideoCapture frameGrabber;
    private ZContext zeroMqContext;
    private final UUID sessionId;

    public FaceClient() throws ConfigurationException{
        Configurations configs = new Configurations();
        Configuration config = configs.properties(new File("client.properties"));

        this.connectionString = "tcp://localhost:5555";

        state = State.Closed;

        this.zeroMqContext = new ZContext();
        this.sessionId = this.attemptConnect();
    }

    public Mat ReadCamera(){
        VideoCapture vc = new VideoCapture();
        vc.open(0);
        Mat matrix = new Mat();
        boolean res = vc.read(matrix);

        vc.release();
        return matrix;
    }

    @Override
    public FaceRecognitionInfo GetSessionInfo(){
        return new FaceRecognitionInfo();
    }

    @Override
    public void Identify(){
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
        ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ);
        socket.connect(this.connectionString);

        RecognitionMessage message = RecognitionMessage.createDeactivate(this.sessionId);
        byte[] messageData = SerializationUtils.serialize(message);
        socket.send(messageData, 0);

        byte[] reply = socket.recv(0);
        System.out.println("Deactivation reply: " + reply);
    }

    @Override
    public void AddPhoto(BufferedImage image){
        
    }

    private UUID attemptConnect(){
        System.out.println("Attempting registration with FaceServer...");

        ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ);
        socket.connect(this.connectionString);

        UUID uuId = UUID.randomUUID();
        RecognitionMessage message = RecognitionMessage.createActivate(uuId);

        byte[] messageData = SerializationUtils.serialize(message);
        socket.send(messageData, 0);

        byte[] reply = socket.recv(0);
        System.out.println("Activation reply: " + reply);
        return uuId;
    }

    private BufferedImage convertImage(BufferedImage image){
        BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        convertedImg.getGraphics().drawImage(image, 0, 0, null);
        return convertedImg;
    }

    private Mat convertImage(Mat src){
        Mat rgbFrame = new Mat(src.rows(), src.cols(), CvType.CV_8U, new Scalar(3));
        Imgproc.cvtColor(src, rgbFrame, Imgproc.COLOR_GRAY2RGB, 3);
        return rgbFrame;
    }

    public void main2(FaceClient faceClient){
        // Mat mat = Imgcodecs.imread("/home/dzlyy/projects/countr_face_recognition/yalefaces/subject01.normal.jpg.png");
        Mat mat = faceClient.ReadCamera();
        int channels = mat.channels();
        int imageType =  mat.type();
        int depth =  mat.depth();
        int height =  mat.height();
        int width =  mat.width();
        byte[] b = new byte[height * width * channels];
        mat.get(0,0, b);

        System.out.println("Length of bytes "+ b.length);
        System.out.println("width "+ width);
        System.out.println("height "+ height);
        System.out.println("type "+ imageType);
        System.out.println("depth "+ depth);
        System.out.println("channels "+ mat.channels());

        System.out.println("Connecting to hello world server");
        try (ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            //  Socket to talk to server
            socket.connect("tcp://localhost:5555");

            for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
                String request = "Hello";
                System.out.println("Sending Hello " + requestNbr);

                // String outfile = String.format("/home/dzly/projects/countr_face_recognition/faceclient/out2/%d.png", mat);
                // boolean res = Imgcodecs.imwrite(outfile, mat);

                UUID uuId = UUID.randomUUID();
                RecognitionMessage message = new RecognitionMessage(b, RecognitionMessage.MessageType.Recognize, height, width, uuId, imageType);
                byte[] messageData = SerializationUtils.serialize(message);
                socket.send(messageData, 0);

                byte[] reply = socket.recv(0);
                System.out.println(
                        "Received " + new String(reply, ZMQ.CHARSET) + " " +
                        requestNbr);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        FaceClient fc = null;
        try {
            fc = new FaceClient();
        }
        catch (ConfigurationException ex){
            System.out.println(ex);
            System.exit(1);
        }
        // fc.main2(fc);
        try (FileInputStream f = new FileInputStream("/home/dzly/projects/countr_face_recognition/face_python/yalefaces/subject01.normal.jpg.png")) {
            BufferedImage image = ImageIO.read(f);
            BufferedImage imageConf = fc.convertImage(image);
            System.out.println(image);
            System.out.println();
            System.out.println(imageConf);
        }
        catch (Exception e) {

        }

        System.out.println();
        System.out.println();
        try (FileInputStream f = new FileInputStream("/home/dzly/projects/countr_face_recognition/face_python/yalefaces/trainer_reference.png")) {
            BufferedImage image = ImageIO.read(f);
            BufferedImage imageConf = fc.convertImage(image);
            System.out.println(image);
            System.out.println();
            System.out.println(imageConf);
        }
        catch (Exception e) {

        }

        Mat m = fc.ReadCamera();
        System.out.println();
        System.out.println(m.channels());
        System.out.println(m.type());
        System.out.println(m.cols());
        System.out.println(m.rows());
        // fc.AddPhoto(image);
        System.exit(0);
    }
}
