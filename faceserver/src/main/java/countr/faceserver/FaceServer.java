package countr.faceserver;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.mxnet.javaapi.NDArray;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import countr.common.FaceDetection;
import countr.common.MXNetUtils;
import countr.common.RecognitionMessage;
import countr.common.RecognitionMessage.MessageType;
import countr.common.RecognitionResult;


public class FaceServer implements IFaceServer{
    MXNetUtils resnet100;
    FaceDetection faceDetector;
    int port;
    ZContext zContext;

    public FaceServer(boolean isGpu, String modelPath, int port, boolean isDebug) {
        this.resnet100 = new MXNetUtils(isGpu, modelPath);
        this.faceDetector = new FaceDetection(isDebug);
        this.port = port;
        this.zContext = new ZContext();
    }

    public void Start(){
    };

    public RecognitionResult Recognize(RecognitionMessage message ){
        byte[] data = message.getImage();
        int width = message.getWidth();
        int height = message.getHeight();
        int imageType = message.getImageType();

        Mat mat = new Mat(height, width, imageType);
        mat.put(0,0, data);
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", mat, mob);
        try {
            BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(mob.toArray()));
            File newFile = new File("test_output.png");
            ImageIO.write(inputImage, "png", newFile);
            BufferedImage faceImage = this.faceDetector.detect(inputImage);
            if(faceImage != null){
                List<NDArray> recognitionVector = this.resnet100.predict(faceImage);
            }
        }
        catch(IOException e){
        }

        return new RecognitionResult();
    }

    public void Listen() {
        try(ZMQ.Socket socket = this.zContext.createSocket(ZMQ.REP))  {
            // Socket to talk to clients
            socket.bind("tcp://*:" + this.port);

            while (!Thread.currentThread().isInterrupted()) {
                // Block until a message is received
                byte[] reply = socket.recv(0);
                RecognitionMessage yourObject = SerializationUtils.deserialize(reply);

                MessageType type = yourObject.getType();
                System.out.println("Message Type: " + type);

                RecognitionResult response = null;
                switch (type){
                    case Activate:
                        break;
                    case Deactivate:
                        break;
                    case Recognize:
                        response = this.Recognize(message);
                    case AddPhoto:
                        break;
                }

                // Send a response
                byte [] responseBytes = SerializationUtils.serialize(response);
                socket.send(responseBytes, 0);
            }
        }
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Configurations configs = new Configurations();

        String modelPath = "";
        boolean isGpu = false;
        int port = -1;
        boolean isDebug = false;
        try
        {
            Configuration config = configs.properties(new File("server.properties"));
            port = config.getInt("server.port");
            isGpu = config.getBoolean("server.isgpu");
            modelPath = config.getString("server.modelpath");
            isDebug = config.getBoolean("server.isdebug");
        }
        catch (ConfigurationException cex)
        {
            System.out.println(cex);
            System.exit(1);
        }

        System.out.println("Starting server...");

        FaceServer server = new FaceServer(isGpu, modelPath, port, isDebug);
        server.Listen();

        System.out.println("Exiting...");
    }
}
