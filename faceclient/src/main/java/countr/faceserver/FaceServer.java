package countr.faceserver;

import countr.common.MXNetUtils;
import countr.common.FaceDetection;

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

    public FaceServer(boolean isGpu, String modelPath) {
        this.resnet100 = new MXNetUtils(isGpu, modelPath)
        this.faceDetector = new FaceDetection(true)
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
                    // BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(data));
                    BufferedImage faceImage = this.faceDetector.detect(inputImage);
                    System.out.println(this.resnet100.predict(faceImage));
                }
                catch(IOException e){

                }

                String outfile = String.format("/home/dzly/projects/countr_face_recognition/faceclient/output/%d.png", yourObject.getSender());
                boolean res = Imgcodecs.imwrite(outfile, mat);
                if(!res){
                    // throw Exception("Help");
                }

                // Send a response
                String response = "Hello, world!";
                socket.send(response.getBytes(ZMQ.CHARSET), 0);
            }
        }
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Configurations configs = new Configurations();
        try
        {
            Configuration config = configs.properties(new File("config.properties"));
            System.out.println(config.getBoolean("isdebug"));
        }
        catch (ConfigurationException cex)
        {
        }

        String modelPath = "/home/dzlyy/projects/countr_face_recognition/faceclient/model-r100-ii/model";
        boolean isGpu = false;
    }
}
