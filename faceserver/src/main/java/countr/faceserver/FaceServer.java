package countr.faceserver;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import countr.common.FaceDatabase;
import countr.common.FaceDetection;
import countr.common.FaceEmbedding;
import countr.common.MXNetUtils;
import countr.common.EmbeddingResponse;
import countr.common.MatchResult;
import countr.common.RecognitionMatch;
import countr.common.RecognitionMessage;
import countr.common.RecognitionMessage.MessageType;
import countr.common.RecognitionResult;
import countr.common.ServerResult;
import countr.common.VerifyResult;
import countr.utils.ComputeUtils;
import countr.utils.DebugUtils;


public class FaceServer implements IFaceServer{
    private final MXNetUtils resnet100;
    private final FaceDetection faceDetector;
    private final int port;
    private final ZContext zContext;
    private final FaceDatabase faceDb; 
    private final Logger log;

    public FaceServer(final boolean isGpu, final String modelDir, final int port, final boolean isDebug) throws IOException, SQLException {
        this.log = LoggerFactory.getLogger(this.getClass());

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL modelDirUri = classloader.getResource(modelDir);
        if (modelDirUri == null){
            this.log.error("Please put your model in src/main/resources/");
            throw new IOException("ArcFace Model is not in correct position. Please read readme.");
        }
        String modelDirString = modelDirUri.getPath();
        String modelPath = modelDirString +  "model";


        this.resnet100 = new MXNetUtils(isGpu, modelPath);
        this.faceDetector = new FaceDetection(isDebug);
        this.port = port;
        this.zContext = new ZContext();

        try {
            this.faceDb = new FaceDatabase();
        }
        catch (final SQLException ex){
            this.log.error("Unable to create database...");
            throw ex;
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
                this.log.info("Received message from " + message.getSender() + " of type: " + type);

                ServerResult response = null;
                try {
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
                        case DeleteGroup:
                            response = this.deleteGroup(message);
                            break;
                        case DeleteUser:
                            response = this.deleteUser(message);
                            break;
                        case Match:
                            response = this.getMatches(message);
                            break;
                        case Verify:
                            response = this.verify(message);
                            break;
                        default:
                            this.log.warn("Message not implemented...");
                    }
                }
                catch (Exception ex) {

                }

                // Send a response
                final byte [] responseBytes = SerializationUtils.serialize(response);
                socket.send(responseBytes, 0);
            }
        }
    }


    private VerifyResult verify(final RecognitionMessage message){
        List<FaceEmbedding> embeddings = null;
        try{
            embeddings = this.faceDb.get(message.getUserId(), message.getGroupId());
            if(embeddings.isEmpty()){
                return new VerifyResult(null, false, "No embeddings found for UserId and group combination.");
            }

            final float[] feature = this.imageToFeatures(message);
            if(feature != null){
                RecognitionMatch[] results = ComputeUtils.Match(feature, embeddings.toArray(new FaceEmbedding[]{}), 1);
                return new VerifyResult(results[0], true);
            }
        }
        catch (final SQLException ex){
            this.log.warn("Failed getting embeddings...");
            this.log.warn(ex.toString());
            return new VerifyResult(null, false, "Face database error.");
        }
        catch (ArrayIndexOutOfBoundsException ex){
            this.log.warn("There are no match results.");
            this.log.warn("Embeddings: " + embeddings);
            this.log.warn(ex.toString());
            return new VerifyResult(null, false, "No recognition results.");
        }
        catch (Exception ex){
            this.log.warn(ex.toString());
            return new VerifyResult(null, false, "Unknown Server Exception: " + ex.toString());
        }

        this.log.warn("Failed verifying message: " + message.getSender());
        return new VerifyResult(null, false, "Unknown error: Verify Failed");
    }

    private float[] imageToFeatures(final RecognitionMessage message){
        final byte[] data = message.getImage();
        final int width = message.getWidth();
        final int height = message.getHeight();
        final int imageType = message.getImageType();

        final Mat mat = new Mat(height, width, imageType);
        mat.put(0,0, data);
        final MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".png", mat, mob);

        float[] recognitionResult = null;
        try {
            final BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(mob.toArray()));
            final BufferedImage faceImage = this.faceDetector.detect(inputImage);
            if(faceImage != null){
                recognitionResult = ComputeUtils.Normalize(this.resnet100.predict(faceImage)); 
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
            this.log.warn("Feature Recognition failed..");
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
            this.log.warn("Problem deleting users...");
            this.log.warn(ex.toString());
            return new ServerResult(false);
        }
    }

    public ServerResult deleteGroup(final RecognitionMessage message){
        try {
            this.faceDb.deleteGroup(message.getGroupId());
            return new ServerResult(true);
        }
        catch (final SQLException ex){
            this.log.warn("Problem deleting group...");
            this.log.warn(ex.toString());
            return new ServerResult(false);
        }
    }

    public MatchResult getMatches(final RecognitionMessage message){
        int maxResults = message.getMaxResults();
        if (maxResults == 0)
        {
            return new MatchResult(new RecognitionMatch[]{}, false, "max_result must be greater than 0.");
        }

        List<FaceEmbedding> embeddings = null;
        try{
            embeddings = this.faceDb.get(message.getGroupId());
            if(embeddings.isEmpty()){
                return new MatchResult(null, false, "No embeddings for given group id.");
            }

            final float[] feature = this.imageToFeatures(message);
            if(feature != null){
                RecognitionMatch[] results = ComputeUtils.Match(feature, embeddings.toArray(new FaceEmbedding[]{}), maxResults);
                return new MatchResult(results, true);
            }
        }
        catch (final SQLException ex){
            this.log.warn("Failed getting embeddings...");
            this.log.warn(ex.toString());
            return new MatchResult(null, false);
        }

        this.log.warn("Failed matching message: " + message.getSender());
        return new MatchResult(null, false);
    }

    public static void main(final String[] args) {
        final Logger log = LoggerFactory.getLogger(FaceServer.class);

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
            log.error(cex.toString());
            System.exit(1);
        }

        log.info("Starting server...");
        FaceServer server = null;
        try {
            server = new FaceServer(isGpu, modelPath, port, isDebug);
        }
        catch (Exception ex){
            log.error("Error loading faceserver");
            log.error(ex.toString());
            System.exit(1);
        }

        log.info("Face Server initialized. Listening...");
        server.Listen();
        log.info("Exiting...");
    }
}
