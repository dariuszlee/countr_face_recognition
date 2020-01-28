package countr.faceserver;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.SerializationUtils;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import countr.common.FaceDatabase;
import countr.common.FaceDetection;
import countr.common.FaceEmbedding;
import countr.common.MXNetUtils;
import countr.common.EmbeddingResponse;
import countr.common.RecognitionMessage;
import countr.common.RecognitionMessage.MessageType;
import countr.common.RecognitionResult;
import countr.common.ServerResult;
import countr.utils.DebugUtils;


public class FaceServer implements IFaceServer{
    MXNetUtils resnet100;
    FaceDetection faceDetector;
    int port;
    ZContext zContext;
    FaceDatabase faceDb; 

    public FaceServer(final boolean isGpu, final String modelPath, final int port, final boolean isDebug) {
        this.resnet100 = new MXNetUtils(isGpu, modelPath);
        this.faceDetector = new FaceDetection(isDebug);
        this.port = port;
        this.zContext = new ZContext();
        try {
            this.faceDb = new FaceDatabase();
        }
        catch (final SQLException ex){
            System.out.println("Unable to create database...");    
        }
    }

    public void Start(){
    };

    public float[] imageToFeatures(final RecognitionMessage message ){
        final byte[] data = message.getImage();
        final int width = message.getWidth();
        final int height = message.getHeight();
        final int imageType = message.getImageType();

        final Mat mat = new Mat(height, width, imageType);
        mat.put(0,0, data);
        DebugUtils.printMatrixInfo(mat);
        DebugUtils.saveImage(mat, "received");
        final MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", mat, mob);

        float[] recognitionResult = null;
        try {
            final BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(mob.toArray()));
            final File newFile = new File("test_output.png");
            ImageIO.write(inputImage, "png", newFile);
            final BufferedImage faceImage = this.faceDetector.detect(inputImage);
            if(faceImage != null){
                recognitionResult =  this.resnet100.predict(faceImage); 
            }
        }
        catch(final IOException e){
        }

        return recognitionResult;
    }

    public RecognitionResult Recognize(final RecognitionMessage message){
        final float[] feature = this.imageToFeatures(message);
        return new RecognitionResult(feature, true);
    }

    public RecognitionResult AddPhoto(final RecognitionMessage message){
        final float[] feature = this.imageToFeatures(message);
        if(feature == null){
            System.out.println("Feature Recognition failed..");
            return new RecognitionResult(feature, false);
        }

        this.faceDb.Insert(message.getUserId(), feature, message.getGroupId());
        return new RecognitionResult(feature, true);
    }

    public EmbeddingResponse getEmbeddings(final RecognitionMessage message){
        try {
            final List<FaceEmbedding> embeddings = this.faceDb.get(message.getGroupId());
            return new EmbeddingResponse(embeddings, true);
        }
        catch (final SQLException ex){
            return new EmbeddingResponse(null, false);
        }
    }
    
    public ServerResult deleteUser(final RecognitionMessage message){
        try {
            this.faceDb.delete(message.getUserId(), message.getGroupId());
            return new ServerResult(true);
        }
        catch (final SQLException ex){
            System.out.println(ex);
            return new ServerResult(false);
        }
    }

    public void Listen() {
        try(ZMQ.Socket socket = this.zContext.createSocket(ZMQ.REP))  {
            // Socket to talk to clients
            socket.bind("tcp://*:" + this.port);

            while (!Thread.currentThread().isInterrupted()) {
                // Block until a message is received
                final byte[] reply = socket.recv(0);
                final RecognitionMessage message = SerializationUtils.deserialize(reply);

                final MessageType type = message.getType();
                System.out.println("Type: " + type);

                ServerResult response = null;
                switch (type){
                    case Activate:
                        break;
                    case Deactivate:
                        break;
                    case Recognize:
                        response = this.Recognize(message);
                        break;
                    case AddPhoto:
                        response = this.AddPhoto(message);
                        break;
                    case GetEmbeddings:
                        response = this.getEmbeddings(message);
                        break;
                    case DeleteUser:
                        response = this.deleteUser(message);
                        break;
                    default:
                        System.out.println("Message not implemented...");
                }

                // Send a response
                final byte [] responseBytes = SerializationUtils.serialize(response);
                socket.send(responseBytes, 0);
            }
        }
    }

    public static void main(final String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        final Configurations configs = new Configurations();

        String modelPath = "";
        boolean isGpu = false;
        int port = -1;
        boolean isDebug = false;
        try
        {
            final Configuration config = configs.properties(new File("server.properties"));
            port = config.getInt("server.port");
            isGpu = config.getBoolean("server.isgpu");
            modelPath = config.getString("server.modelpath");
            isDebug = config.getBoolean("server.isdebug");
        }
        catch (final ConfigurationException cex)
        {
            System.out.println(cex);
            System.exit(1);
        }

        System.out.println("Starting server...");

        final FaceServer server = new FaceServer(isGpu, modelPath, port, isDebug);
        server.Listen();

        System.out.println("Exiting...");
    }
}
