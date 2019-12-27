package countr.common;

import java.io.Serializable;

public class RecognitionMessage implements Serializable {
    private byte[] image;
    private Integer sender;

    public RecognitionMessage(byte[] image, Integer sender){
        this.image = image;
        this.sender = sender;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
    public byte[] getImage() {
        return image;
    }
    public void setSender(Integer sender) {
        this.sender = sender;
    }
    public Integer getSender() {
        return sender;
    }
}
