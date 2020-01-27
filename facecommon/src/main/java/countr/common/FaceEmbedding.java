package countr.common;

import java.io.Serializable;
import java.util.Arrays;

public class FaceEmbedding implements Serializable{
    private String id;
    private float[] embedding;
    private int groupId;

    public FaceEmbedding(String id, float[] embedding, int groupId) {
        this.id = id;
        this.embedding = embedding;
        this.groupId = groupId;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "FaceEmbedding [embedding=" + Arrays.toString(embedding) + ", groupId=" + groupId + ", id=" + id + "]";
    }
}
