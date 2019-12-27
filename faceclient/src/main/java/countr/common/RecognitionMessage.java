package countr.common;

import java.io.Serializable;
import java.util.Arrays;

public class RecognitionMessage implements Serializable {
    private byte[] image;
    private Integer sender;
    private int type;
    private int width;
    private int height;

    public RecognitionMessage(byte[] image, int type, 
            int height, int width, Integer sender){
        this.image = image;
        this.sender = sender;
        this.type = type;
        this.width = width;
        this.height = height;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + height;
        result = prime * result + Arrays.hashCode(image);
        result = prime * result + ((sender == null) ? 0 : sender.hashCode());
        result = prime * result + type;
        result = prime * result + width;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RecognitionMessage other = (RecognitionMessage) obj;
        if (height != other.height)
            return false;
        if (!Arrays.equals(image, other.image))
            return false;
        if (sender == null) {
            if (other.sender != null)
                return false;
        } else if (!sender.equals(other.sender))
            return false;
        if (type != other.type)
            return false;
        if (width != other.width)
            return false;
        return true;
    }
}
