package countr.common;

import org.nd4j.linalg.cpu.nativecpu.NDArray;

public class FaceEmbedding{
    private int Id;
    private NDArray Embedding;

    public FaceEmbedding(int id, NDArray embedding) {
        Id = id;
        Embedding = embedding;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public NDArray getEmbedding() {
        return Embedding;
    }

    public void setEmbedding(NDArray embedding) {
        Embedding = embedding;
    }

}
