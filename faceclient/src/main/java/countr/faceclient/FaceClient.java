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

    @Override
    public void Recognize(final Mat mat){
        DebugUtils.printMatrixInfo(mat);

        final int channels = mat.channels();
        final int imageType =  mat.type();
        final int depth =  mat.depth();
        final int height =  mat.height();
        final int width =  mat.width();
        final byte[] b = new byte[height * width * channels];
        mat.get(0,0, b);

        try (ZMQ.Socket socket = this.zeroMqContext.createSocket(SocketType.REQ)){
            socket.connect(this.connectionString);

            final RecognitionMessage message = RecognitionMessage.createRecognize(b, height, width, imageType, this.sessionId);
            final byte[] messageData = SerializationUtils.serialize(message);
            socket.send(messageData, 0);

            final byte[] reply = socket.recv(0);
        }

    }

    public void Recognize(BufferedImage bf){
        this.Recognize(this.convertImage(bf));
    }

    @Override
    public void Identify(){
        frameGrabber = new VideoCapture(0);
        try {
            // frameGrabber.start();
           for(int i = 0; i < 10; i++){
               final Mat matrix = new Mat();
               System.out.println(matrix.type());
               final boolean is_grabbed = frameGrabber.read(matrix);
               Imgcodecs.imwrite((i++) + "-aa.jpg", matrix);
           }
        } 
        catch (final Exception e) {
            e.printStackTrace();
        }
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

    public void AddPhoto(final BufferedImage image){
        
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
        final BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
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

    public void main2(final FaceClient faceClient){
        // Mat mat = Imgcodecs.imread("/home/dzlyy/projects/countr_face_recognition/yalefaces/subject01.normal.jpg.png");
        final Mat mat = this.ReadCamera();

        System.out.println("Connecting to hello world server");
        for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
            this.Recognize(mat);
        }
    }

    public static void main(final String[] args) {
        System.out.println(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        FaceClient fc = null;
        Mat m = null;
        try {
            fc = new FaceClient();
        }
        catch (final ConfigurationException ex){
            System.out.println(ex);
            System.exit(1);
        }
        // fc.main2(fc);
        try (FileInputStream f = new FileInputStream("/home/dzly/projects/countr_face_recognition/face_python/yalefaces/subject01.normal.jpg.png")) {
            final BufferedImage image = ImageIO.read(f);
            System.out.println(image);
            System.out.println();
            System.out.println(image);
            fc.Recognize(image);
        }
        catch (final Exception e) {

        }

        System.out.println();
        System.out.println();
        try (FileInputStream f = new FileInputStream("/home/dzly/projects/countr_face_recognition/face_python/yalefaces/trainer_reference.png")) {
            final BufferedImage image = ImageIO.read(f);
            System.out.println(image);
            System.out.println();
            System.out.println(image);

            fc.Recognize(m);
        }
        catch (final Exception e) {

        }

        m = fc.ReadCamera();
        System.out.println();
        System.out.println(m.channels());
        System.out.println(m.type());
        System.out.println(m.cols());
        System.out.println(m.rows());
        // fc.AddPhoto(image);
        fc.Recognize(m);

        System.exit(0);
    }
}
