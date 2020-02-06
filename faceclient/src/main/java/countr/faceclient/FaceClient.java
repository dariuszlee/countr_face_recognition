package countr.faceclient;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
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
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import countr.utils.DebugUtils;
import countr.common.EmbeddingResponse;
import countr.common.FaceEmbedding;
import countr.common.MatchResult;
import countr.common.RecognitionMessage;
import countr.common.RecognitionMessage.MessageType;
import countr.common.RecognitionResult;
import countr.common.ServerResult;
import countr.common.VerifyResult;

public class FaceClient implements IFaceClient
{
    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        }
        catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load.\n" + e);
            System.exit(1);
        }
    }

    private final String connectionString;
    private final int maxImageSize;
    private VideoCapture frameGrabber;
    private final ZContext zeroMqContext;
    private final UUID sessionId;

    private final float matchThreshold;
    private final float verifyThreshold;

    private final CascadeClassifier faceDetector;

    public FaceClient() throws ConfigurationException{
        final Configurations configs = new Configurations();
        final Configuration config = configs.properties(new File("client.properties"));

        this.connectionString = "tcp://" + config.getString("client.host") + ":" + config.getString("client.port");
        this.maxImageSize = config.getInt("client.maxImageSize");

        this.zeroMqContext = new ZContext();
        this.sessionId = this.attemptConnect();

        this.faceDetector = new CascadeClassifier("face_cascade");

        this.matchThreshold = config.getFloat("client.matchThreshold");
        this.verifyThreshold = config.getFloat("client.verifyThreshold");
        if (this.matchThreshold > 1 || this.matchThreshold < 0 || this.verifyThreshold > 1 || this.verifyThreshold < 0){
            throw new ConfigurationException("Match and Verify thresholds must be less than 1 and greater than 0.");
        }
    }

    @Override
    public Mat ReadCamera(int videoDevice){
        final VideoCapture vc = new VideoCapture();
        vc.open(videoDevice);
        final Mat matrix = new Mat();
        final boolean res = vc.read(matrix);

        vc.release();
        return matrix;
    }

    @Override
    public EmbeddingResponse GetEmbeddings(int groupId) {
        try (ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            final RecognitionMessage message = RecognitionMessage.createGetEmbeddings(this.sessionId, groupId);
            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] reply = socket.recv(0);
            EmbeddingResponse replyMessage = SerializationUtils.deserialize(reply);

            return replyMessage;
        }
    }

    @Override
    public RecognitionResult Recognize(Mat mat, int groupId){
        if(mat.channels() == 1){
            mat = this.convertImage(mat);
        }

        try (ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            final RecognitionMessage message = this.matrixToRecognitionMessage(mat, MessageType.Recognize);
            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] reply = socket.recv(0);
            RecognitionResult replyMessage = SerializationUtils.deserialize(reply);
            System.out.println(replyMessage);
            return replyMessage;
        }
    }

    @Override
    public RecognitionResult Recognize(String path, int groupId){
        Mat image = null;
        try {
            image = Imgcodecs.imread(path);
        }
        catch (Exception ex){
            System.out.println("Loading image file failed... Check path.");
            System.out.println(ex);
        }
        return this.Recognize(image, groupId);
    }

    @Override
    public ServerResult Close(){
        final ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ);
        socket.connect(this.connectionString);

        final RecognitionMessage message = RecognitionMessage.createDeactivate(this.sessionId);
        final byte[] messageData = SerializationUtils.serialize(message);
        socket.send(messageData, 0);

        final byte[] replyBytes = socket.recv(0);
        ServerResult reply = SerializationUtils.deserialize(replyBytes);
        System.out.println("Deactivation reply: " + reply);
        return reply;
    }

    @Override
    public RecognitionResult AddPhoto(String path, String userId, int groupId){
        Mat image = null;
        try {
            image = Imgcodecs.imread(path);
        }
        catch (Exception ex){
            System.out.println("Loading image file failed... Check path.");
            System.out.println(ex);
        }
        return this.AddPhoto(image, userId, groupId);
    }

    @Override
    public RecognitionResult AddPhoto(Mat mat, String userId, int groupId){
        if (mat.channels() == 1){
            mat = this.convertImage(mat);
        }

        byte[] dataBytes = this.matrixToBytes(mat);
        try(final ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            final RecognitionMessage message = RecognitionMessage.createAddPhoto(dataBytes, mat.height(), mat.width(), mat.type(), this.sessionId, userId, groupId);

            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] replyBytes = socket.recv(0);
            RecognitionResult reply = SerializationUtils.deserialize(replyBytes);
            return reply;
        }
    }

    @Override
    public boolean ContainsFace(Mat mat){
        throw new java.lang.UnsupportedOperationException();
    }

    @Override
    public boolean ContainsFace(String path){
        Mat image = null;
        try {
            image = Imgcodecs.imread(path);
        }
        catch (Exception ex){
            System.out.println("Loading image file failed... Check path.");
            System.out.println(ex);
        }
        return this.ContainsFace(mat);
    }

    public VerifyResult Verify(Mat mat, final String userId,  final int groupId, final float threshold){
        if (mat.empty()){
            return new VerifyResult(null, false, "No data in image. Check input.");
        }
        else if (mat.width() * mat.height() * mat.channels() > this.maxImageSize){
            return new VerifyResult(null, false, "Image exeeds max image size. Increase the default threshold or convert your image. This is set client.properties or in the FaceClient constructor. Currently the value is: " + this.maxImageSize);
        }
        else if(mat.channels() == 1){
            mat = this.convertImage(mat);
        }

        byte[] dataBytes = this.matrixToBytes(mat);
        try(final ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            final RecognitionMessage message = RecognitionMessage.createVerify(dataBytes, mat.height(), mat.width(), mat.type(), this.sessionId, userId, groupId, 1);

            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] replyBytes = socket.recv(0);
            VerifyResult reply = SerializationUtils.deserialize(replyBytes);

            // Import step
            if (reply.isSuccess() && reply.getTopMatch().getMatch() < threshold){
                return new VerifyResult(reply.getTopMatch(), false, "Verify Threshold not reached.");
            }
            return reply;
        }
    }

    public VerifyResult Verify(String path, final String userId, final int groupId, final float threshold){
        Mat image = null;
        try {
            image = Imgcodecs.imread(path);
        }
        catch (Exception ex){
            System.out.println("Loading image file failed... Check path.");
            System.out.println(ex);
        }
        return this.Verify(image, userId, groupId, threshold);
    }

    public VerifyResult Verify(String path, final String userId,  final int groupId){
        return this.Verify(path, userId, groupId, this.verifyThreshold);
    }

    public VerifyResult Verify(Mat mat, final String userId, final int groupId){
        return this.Verify(mat, userId, groupId, this.verifyThreshold);
    }

    public MatchResult Match(String path, final int groupId, final int maxResults){
        Mat image = null;
        try {
            image = Imgcodecs.imread(path);
        }
        catch (Exception ex){
            System.out.println("Loading image file failed... Check path.");
            System.out.println(ex);
        }
        return this.Match(image, groupId, maxResults);
    }

    public MatchResult Match(Mat mat, final int groupId, final int maxResults){
        if (mat.empty()){
            return new MatchResult(null, false, "No data in image. Check input.");
        }
        else if (mat.width() * mat.height() * mat.channels() > this.maxImageSize){
            return new MatchResult(null, false, "Image exeeds max image size. Increase the default threshold or convert your image. This is set client.properties or in the FaceClient constructor. Currently the value is: " + this.maxImageSize);
        }
        else if(mat.channels() == 1){
            mat = this.convertImage(mat);
        }

        byte[] dataBytes = this.matrixToBytes(mat);
        try(final ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            final RecognitionMessage message = RecognitionMessage.createMatch(dataBytes, mat.height(), mat.width(), mat.type(), this.sessionId, "", groupId, maxResults);

            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] replyBytes = socket.recv(0);
            MatchResult reply = SerializationUtils.deserialize(replyBytes);
            // System.out.println("Match reply: " + reply);
            return reply;
        }
    }

    public RecognitionResult AddPhoto(final BufferedImage image, String userId, int groupId){
        Mat mat = this.convertImage(image);
        if (mat.channels() == 1){
            mat = this.convertImage(mat);
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
            
            return reply;
        }
    }

    public ServerResult DeleteGroup(int groupId){
        try(final ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            final RecognitionMessage message = RecognitionMessage.createDeleteGroup(this.sessionId, groupId);

            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] replyBytes = socket.recv(0);
            ServerResult reply = SerializationUtils.deserialize(replyBytes);
            System.out.println("AddPhoto reply: " + reply);

            return reply;
        }

    }

    public ServerResult DeleteUser(String userId, int groupId){
        try(final ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            final RecognitionMessage message = RecognitionMessage.createDeleteUser(this.sessionId, userId, groupId);

            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] replyBytes = socket.recv(0);
            ServerResult reply = SerializationUtils.deserialize(replyBytes);
            System.out.println("Delete User reply: " + reply);

            return reply;
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
        int numberOfComponents =  image.getColorModel().getNumComponents();

        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        Mat imageFinal = new Mat(image.getWidth(), image.getHeight(), CvType.CV_8U, new Scalar(numberOfComponents));
        imageFinal.put(0, 0, pixels);
        return imageFinal;
    }

    private Mat convertImage(final Mat src){
        final Mat rgbFrame = new Mat(src.rows(), src.cols(), CvType.CV_8U, new Scalar(3));
        Imgproc.cvtColor(src, rgbFrame, Imgproc.COLOR_GRAY2RGB, 3);
        return rgbFrame;
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

    public static void main(final String[] args) {
        System.out.println(Core.NATIVE_LIBRARY_NAME);
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        FaceClient fc = null;
        try {
            fc = new FaceClient();
        }
        catch (final ConfigurationException ex){
            System.out.println(ex);
            System.exit(1);
        }
        int groupId = 1;
        String userId = "2";
        int maxResults = 3;

        String filePath = "/home/dzly/projects/countr_face_recognition/face_python/yalefaces/subject01.normal.jpg.png";
        final Mat image = Imgcodecs.imread(filePath);
        String filePath2 = "/home/dzly/projects/countr_face_recognition/face_python/yalefaces/trainer_reference.png";
        final Mat image2 = Imgcodecs.imread(filePath2);
        String filePath3 = "/home/dzly/projects/countr_face_recognition/face_python/yalefaces/subject02.normal.jpg.png";
        final Mat image3 = Imgcodecs.imread(filePath3);
        // fc.Recognize(image);
        fc.AddPhoto(image, filePath, groupId);
        fc.AddPhoto(image2, filePath2, groupId);
        fc.AddPhoto(image3, filePath3, groupId);
        fc.Match(image, groupId, maxResults);
        // fc.DeleteUser(userId, groupId);
        // System.out.println("Finished recognizing image 1");
        // System.out.println();

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
        // Mat m = fc.ReadCamera(0);
        // // fc.AddPhoto(image);
        // fc.Recognize(m);

        System.exit(0);
    }
}
