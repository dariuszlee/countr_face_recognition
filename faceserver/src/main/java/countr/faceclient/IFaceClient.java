package countr.faceclient;

public interface IFaceClient {
    public void Identify();
    public FaceRecognitionInfo GetSessionInfo();
    public void Close();
}
