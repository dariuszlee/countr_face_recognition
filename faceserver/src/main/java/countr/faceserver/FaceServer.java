package countr.faceserver;

import countr.common.MXNetUtils;
import countr.common.FaceDetection;

import java.util.List;
import org.apache.mxnet.javaapi.NDArray;

import java.io.IOException;

import org.apache.commons.lang3.SerializationUtils;

import countr.common.RecognitionMessage;
import countr.faceserver.IFaceServer;

import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;

import java.io.ByteArrayInputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import java.io.File;


public class FaceServer implements IFaceServer{
    MXNetUtils resnet100;
    FaceDetection faceDetector;
    int port;

    public FaceServer(boolean isGpu, String modelPath, int port, boolean isDebug) {
        this.resnet100 = new MXNetUtils(isGpu, modelPath);
        this.faceDetector = new FaceDetection(isDebug);
        this.port = port;
    }

    public void Start(){
    };

    public void Recognize(){
    }

    public void Listen() {
        try (ZContext context = new ZContext()) {
            // Socket to talk to clients
            ZMQ.Socket socket = context.createSocket(ZMQ.REP);
            socket.bind("tcp://*:5555");

            while (!Thread.currentThread().isInterrupted()) {
                // Block until a message is received
                String response = "Failed";
                byte[] reply = socket.recv(0);
                RecognitionMessage yourObject = SerializationUtils.deserialize(reply);

                // Print the message
                System.out.println("Id: " + yourObject.getSender());
                byte[] data = yourObject.getImage();
                System.out.println("Id Length: " + yourObject.getImage().length);

                int width = yourObject.getWidth();
                int height = yourObject.getHeight();
                int type = yourObject.getType();
                Mat mat = new Mat(height, width, type);
                mat.put(0,0, data);

                MatOfByte mob=new MatOfByte();
                Imgcodecs.imencode(".png", mat, mob);
                try {
                    BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(mob.toArray()));
                    File newFile = new File("test_output.png");
                    ImageIO.write(inputImage, "png", newFile);
                    BufferedImage faceImage = this.faceDetector.detect(inputImage);
                    if(faceImage != null){
                        List<NDArray> recognitionVector = this.resnet100.predict(faceImage);
                        response = "More";
                    }
                }
                catch(IOException e){
                }

                // String outfile = String.format("/home/dzly/projects/countr_face_recognition/faceclient/output/%d.png", yourObject.getSender());
                // boolean res = Imgcodecs.imwrite(outfile, mat);
                // throw Exception("Help");

                // Send a response
                socket.send(response.getBytes(ZMQ.CHARSET), 0);
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
