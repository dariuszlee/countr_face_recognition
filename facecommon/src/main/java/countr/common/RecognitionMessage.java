package countr.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

public class RecognitionMessage implements Serializable {
    public static enum MessageType {
        Activate,
        Deactivate,
        Recognize,
        AddPhoto,
        DeleteUser,
        DeleteGroup,
        GetEmbeddings,
        Verify,
        Match
    }

    private final byte[] image;
    private final UUID sender;
    private final MessageType type;
    private final int width;
    private final int height;
    private final int imageType;

    private final String userId;
    private final int groupId;

    private final int maxResults;

    public RecognitionMessage(final byte[] image, final MessageType type, 
            final int height, final int width, final UUID sender, int imageType,
            String userId, int groupId, int maxResults){
        this.image = image;
        this.sender = sender;
        this.type = type;
        this.width = width;
        this.height = height;
        this.imageType = imageType;
        this.userId = userId;
        this.groupId = groupId;
        this.maxResults = maxResults;
    }

    public RecognitionMessage(final byte[] image, final MessageType type, 
            final int height, final int width, final UUID sender, int imageType,
            String userId, int groupId){
        this.image = image;
        this.sender = sender;
        this.type = type;
        this.width = width;
        this.height = height;
        this.imageType = imageType;
        this.userId = userId;
        this.groupId = groupId;
        this.maxResults = 0;
    }

    public RecognitionMessage(final byte[] image, final MessageType type, 
            final int height, final int width, final UUID sender, int imageType){
        this.image = image;
        this.sender = sender;
        this.type = type;
        this.width = width;
        this.height = height;
        this.imageType = imageType;
        this.userId = "";
        this.groupId = 0;
        this.maxResults = 0;
    }

    public static RecognitionMessage createActivate(final UUID uuid){
        return new RecognitionMessage(null, MessageType.Activate, 0, 0, uuid, 0);
    }

    public static RecognitionMessage createDeleteUser(final UUID uuid, String userId, int groupId){
        return new RecognitionMessage(null, MessageType.DeleteUser, 0, 0, uuid, 0, userId, groupId);
    }

    public static RecognitionMessage createDeleteGroup(final UUID uuid, int groupId){
        return new RecognitionMessage(null, MessageType.DeleteGroup, 0, 0, uuid, 0, "", groupId);
    }

    public static RecognitionMessage createDeactivate(final UUID uuid){
        return new RecognitionMessage(null, MessageType.Deactivate, 0, 0, uuid, 0);
    }

    public static RecognitionMessage createGetEmbeddings(final UUID uuid, final int groupId){
        return new RecognitionMessage(null, MessageType.GetEmbeddings, 0, 0, uuid, 0, "", groupId);
    }

    public static RecognitionMessage createRecognize(final byte[] b, int height, int width, int imageType, final UUID uuid){
        return new RecognitionMessage(b, RecognitionMessage.MessageType.Recognize, height, width, uuid, imageType);
    }

    public static RecognitionMessage createAddPhoto(final byte[] b, int height, int width, int imageType, final UUID uuid, final String userId, final int groupId){
        return new RecognitionMessage(b, RecognitionMessage.MessageType.AddPhoto, height, width, uuid, imageType, userId, groupId);
    }

    public static RecognitionMessage createMatch(final byte[] b, int height, int width, int imageType, final UUID uuid, final String userId, final int groupId, final int maxResults){
        return new RecognitionMessage(b, RecognitionMessage.MessageType.Match, height, width, uuid, imageType, userId, groupId, maxResults);
    }

    public static RecognitionMessage createVerify(final byte[] b, int height, int width, int imageType, final UUID uuid, final String userId, final int groupId, final int maxResults){
        return new RecognitionMessage(b, RecognitionMessage.MessageType.Verify, height, width, uuid, imageType, userId, groupId, maxResults);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final RecognitionMessage other = (RecognitionMessage) obj;
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

    public byte[] getImage() {
        return image;
    }

    public UUID getSender() {
        return sender;
    }

    public MessageType getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getImageType() {
        return imageType;
    }

    public String getUserId() {
        return userId;
    }

    public int getGroupId() {
        return groupId;
    }

    @Override
    public String toString() {
        return "RecognitionMessage [groupId=" + groupId + ", height=" + height + ", image=" + Arrays.toString(image)
                + ", imageType=" + imageType + ", sender=" + sender + ", type=" + type + ", userId=" + userId
                + ", width=" + width + "]";
    }

    public int getMaxResults() {
        return maxResults;
    }
}
