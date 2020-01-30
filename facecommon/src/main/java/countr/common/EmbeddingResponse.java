package countr.common;

import java.io.Serializable;
import java.util.List;
import java.util.Arrays;

public class EmbeddingResponse extends ServerResult {
    final List<FaceEmbedding> embeddings;

    public EmbeddingResponse(List<FaceEmbedding> embeddings, boolean result) {
        super(result);
        this.embeddings = embeddings;
    }

    public EmbeddingResponse(List<FaceEmbedding> embeddings, boolean result, String message) {
        super(result, message);
        this.embeddings = embeddings;
    }

    public List<FaceEmbedding> getEmbeddings() {
        return embeddings;
    }

    @Override
    public String toString() {
        return super.toString() + "EmbeddingResponse [embeddings=" + Arrays.toString(embeddings.toArray()) + "]";
    }
}
