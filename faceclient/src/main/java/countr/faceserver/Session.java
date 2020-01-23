package countr.faceserver;

import java.util.UUID;
import java.util.HashMap;
import java.util.List;

import org.nd4j.linalg.cpu.nativecpu.NDArray;

import java.sql.SQLException;

import org.apache.mxnet.javaapi.Context;
import countr.common.FaceDatabase;
import countr.common.FaceEmbedding;

public class Session {
    private UUID id;
    private int imageCount;
    private int imageCountThreshold;
    private float imageThreshold;
    private HashMap<String, Float> sessionScores;
    private Context ctx;
    private List<FaceEmbedding> faceEmbeddings;

    public Session(Context ctx, UUID id) throws SessionInitializationFailure {
        this.id = id;
        this.imageCount = 0;
        this.imageThreshold = 5;
        this.sessionScores = new HashMap<String, Float>();

        try {
            FaceDatabase faceDatabase = new FaceDatabase(ctx);
            this.faceEmbeddings = faceDatabase.get();
        }
        catch (SQLException e){
            throw new SessionInitializationFailure();
        }
    }

    public void AddSamples(NDArray recognition){
        for (FaceEmbedding emb : this.faceEmbeddings){
        }
    }

    public static void main(String[] args) {
    }
}

class SessionInitializationFailure extends Exception {

}
