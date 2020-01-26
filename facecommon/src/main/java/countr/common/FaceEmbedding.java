package countr.common;

import org.nd4j.linalg.cpu.nativecpu.NDArray;

public class FaceEmbedding{
    private String Id;
    private NDArray Embedding;
    private int GroupId;

    public FaceEmbedding(String id, NDArray embedding) {
        Id = id;
        Embedding = embedding;
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
        return GroupId;
    }

    public void setGroupId(int groupId) {
        GroupId = groupId;
    }

    public String getId() {
        return Id;
    }
}
