package countr.faceclient;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;
import java.awt.image.DataBufferByte;

import javax.imageio.ImageIO;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.SerializationUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import countr.common.DebugUtils;
import countr.common.RecognitionMessage;
import countr.common.RecognitionMessage.MessageType;
import countr.common.RecognitionResponse;


public class FaceClient implements IFaceClient
{
    enum State {
        Closed,
        Running
    }
    private final String connectionString;

    private final State state;
    private VideoCapture frameGrabber;
    private final ZContext zeroMqContext;
    private final UUID sessionId;

    public FaceClient() throws ConfigurationException{
        final Configurations configs = new Configurations();
        final Configuration config = configs.properties(new File("client.properties"));

        this.connectionString = "tcp://localhost:5555";

        state = State.Closed;

        this.zeroMqContext = new ZContext();
        this.sessionId = this.attemptConnect();
    }

    public Mat ReadCamera(){
        final VideoCapture vc = new VideoCapture();
        vc.open(0);
        final Mat matrix = new Mat();
        final boolean res = vc.read(matrix);

        vc.release();
        return matrix;
    }

    private byte[] matrixToBytes(Mat mat){
        final int channels = mat.channels();
        final int height =  mat.height();
        final int width =  mat.width();
        final byte[] b = new byte[height * width * channels];
        mat.get(0,0, b);
        return b;
    }

    private RecognitionMessage matrixToRecognitionMessage(Mat mat, MessageType type){
        final int channels = mat.channels();
        final int imageType =  mat.type();
        final int height =  mat.height();
        final int width =  mat.width();
        final byte[] b = new byte[height * width * channels];
        mat.get(0,0, b);
        return new RecognitionMessage(b, type, height, width, this.sessionId, imageType);
    }

    private RecognitionMessage matrixToRecognitionMessage(BufferedImage image, MessageType type){
        Mat mat = this.convertImage(image);
        return this.matrixToRecognitionMessage(mat, type);
    }

    @Override
    public void Recognize(Mat mat){
        if(mat.channels() == 1){
            DebugUtils.printMatrixInfo(mat);
            mat = this.convertImage(mat);
        }
        DebugUtils.printMatrixInfo(mat);

        try (ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            final RecognitionMessage message = this.matrixToRecognitionMessage(mat, MessageType.Recognize);
            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] reply = socket.recv(0);
        }

    }

    public void Recognize(BufferedImage bf){
        Mat mat = this.convertImage(bf);
        this.Recognize(mat);
    }

    @Override
    public void Close(){
        final ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ);
        socket.connect(this.connectionString);

        final RecognitionMessage message = RecognitionMessage.createDeactivate(this.sessionId);
        final byte[] messageData = SerializationUtils.serialize(message);
        socket.send(messageData, 0);

        final byte[] reply = socket.recv(0);
        System.out.println("Deactivation reply: " + reply);
    }

    public void AddPhoto(final BufferedImage image, String userId, int groupId){
        try(final ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            Mat mat = this.convertImage(image);
            byte[] dataBytes = this.matrixToBytes(mat);
            final RecognitionMessage message = RecognitionMessage.createAddPhoto(dataBytes, image.getWidth(), image.getHeight(), mat.type(), this.sessionId, userId, groupId);

            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] replyBytes = socket.recv(0);
            RecognitionResult reply = SerializationUtils.deserialize(replyBytes);
            System.out.println("AddPhoto reply: " + reply);

        }
    }

    private UUID attemptConnect(){
        System.out.println("Attempting registration with FaceServer...");

        final UUID uuId = UUID.randomUUID();
        try(final ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            final RecognitionMessage message = RecognitionMessage.createActivate(uuId);

            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] reply = socket.recv(0);
            System.out.println("Activation reply: " + reply);
        }
        return uuId;
    }


    private Mat convertImage(final BufferedImage image){
        final BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        convertedImg.getGraphics().drawImage(image, 0, 0, null);
        byte[] pixels = ((DataBufferByte) convertedImg.getRaster().getDataBuffer()).getData();

        Mat imageFinal = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8U, new Scalar(3));
        imageFinal.put(0, 0, pixels);
        return imageFinal;
    }

    private Mat convertImage(final Mat src){
        final Mat rgbFrame = new Mat(src.rows(), src.cols(), CvType.CV_8U, new Scalar(3));
        Imgproc.cvtColor(src, rgbFrame, Imgproc.COLOR_GRAY2RGB, 3);
        return rgbFrame;
    }

    public static void main(final String[] args) {
        System.out.println(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        FaceClient fc = null;
        try {
            fc = new FaceClient();
        }
        catch (final ConfigurationException ex){
            System.out.println(ex);
            System.exit(1);
        }
        try (FileInputStream f = new FileInputStream("/home/dzly/projects/countr_face_recognition/face_python/yalefaces/subject01.normal.jpg.png")) {
            final BufferedImage image = ImageIO.read(f);
            System.out.println(image);
            System.out.println();
            System.out.println(image);
            fc.Recognize(image);
            System.out.println("Finished recognizing image 1");
            System.out.println();
        }
        catch (final Exception e) {
            System.out.println(e);
        }

        System.out.println();
        System.out.println();
        BufferedImage image = null;
        try (FileInputStream f = new FileInputStream("/home/dzly/projects/countr_face_recognition/face_python/yalefaces/trainer_reference.png")) {
            image = ImageIO.read(f);
        }
        catch (final Exception e) {
            System.out.println(e);
        }
        System.out.println();
        System.out.println(image);
        fc.Recognize(image);
        System.out.println("Finished recognizing image 2");
        System.out.println();

        System.out.println();
        System.out.println("Reading camera 3");
        Mat m = fc.ReadCamera();
        // fc.AddPhoto(image);
        fc.Recognize(m);

        System.exit(0);
    }
}
