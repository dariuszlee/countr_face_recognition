package countr.common;

import org.nd4j.linalg.cpu.nativecpu.NDArray;

public class FaceEmbedding{
    private String Id;
    private NDArray Embedding;
    private int groupId;

    public FaceEmbedding(String id, NDArray embedding, int groupId) {
        this.Id = id;
        this.Embedding = embedding;
        this.groupId = groupId;
    }

    public NDArray getEmbedding() {
        return Embedding;
    }

    public void setEmbedding(NDArray embedding) {
        Embedding = embedding;
    }

    public void setId(String id) {
        Id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        groupId = groupId;
    }

    public String getId() {
        return Id;
    }
}
