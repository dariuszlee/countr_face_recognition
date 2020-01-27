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
import countr.utils.DebugUtils;
import countr.common.EmbeddingResponse;
import countr.common.RecognitionMessage;
import countr.common.RecognitionMessage.MessageType;
import countr.common.RecognitionResult;


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

    public void GetEmbeddings(int groupId) {
        try (ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            final RecognitionMessage message = RecognitionMessage.createGetEmbeddings(this.sessionId, groupId);
            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] reply = socket.recv(0);
            EmbeddingResponse replyMessage = SerializationUtils.deserialize(reply);
            System.out.println(replyMessage);
        }
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
            RecognitionResult replyMessage = SerializationUtils.deserialize(reply);
            System.out.println(replyMessage);
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

    public void AddPhoto(Mat mat, String userId, int groupId){
        DebugUtils.saveImage(mat, "before");
        DebugUtils.printMatrixInfo(mat);
        if (mat.channels() == 1){
            DebugUtils.printMatrixInfo(mat);
            mat = this.convertImage(mat);
            DebugUtils.printMatrixInfo(mat);
        }
        DebugUtils.saveImage(mat, "before_1");
        DebugUtils.printMatrixInfo(mat);

        byte[] dataBytes = this.matrixToBytes(mat);
        try(final ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            final RecognitionMessage message = RecognitionMessage.createAddPhoto(dataBytes, mat.height(), mat.width(), mat.type(), this.sessionId, userId, groupId);

            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] replyBytes = socket.recv(0);
            RecognitionResult reply = SerializationUtils.deserialize(replyBytes);
            System.out.println("AddPhoto reply: " + reply);

        }
    }

    public void AddPhoto(final BufferedImage image, String userId, int groupId){
        Mat mat = this.convertImage(image);
        DebugUtils.saveImage(mat, "before_mat");
        if (mat.channels() == 1){
            DebugUtils.printMatrixInfo(mat);
            mat = this.convertImage(mat);
            DebugUtils.printMatrixInfo(mat);
        }

        byte[] dataBytes = this.matrixToBytes(mat);
        try(final ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

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
        DebugUtils.saveImage(image, "before");  
        int numberOfComponents =  image.getColorModel().getNumComponents();

        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        Mat imageFinal = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8U, new Scalar(numberOfComponents));
        imageFinal.put(0, 0, pixels);
        DebugUtils.saveImage(imageFinal, "before_mat_1");  
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
        int groupId = 1;

        String filePath = "/home/dzly/projects/countr_face_recognition/face_python/yalefaces/subject01.normal.jpg.png";
        final Mat image = Imgcodecs.imread(filePath);
        // fc.Recognize(image);
        fc.AddPhoto(image, "2", groupId);
        System.out.println("Finished recognizing image 1");
        System.out.println();
        fc.GetEmbeddings(groupId);

        // System.out.println();
        // System.out.println();
        // BufferedImage image = null;
        // try (FileInputStream f = new FileInputStream("/home/dzly/projects/countr_face_recognition/face_python/yalefaces/trainer_reference.png")) {
        //     image = ImageIO.read(f);
        // }
        // catch (final Exception e) {
        //     System.out.println(e);
        // }
        // System.out.println();
        // System.out.println(image);
        // fc.AddPhoto(image, "3", 1);
        // fc.Recognize(image);
        // System.out.println("Finished recognizing image 2");
        // System.out.println();

        // System.out.println();
        // System.out.println("Reading camera 3");
        // Mat m = fc.ReadCamera();
        // // fc.AddPhoto(image);
        // fc.Recognize(m);

        System.exit(0);
    }
}
