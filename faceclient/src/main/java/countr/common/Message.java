package countr.common;

public class Message {
    private byte[] image;
    private Integer sender;

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
