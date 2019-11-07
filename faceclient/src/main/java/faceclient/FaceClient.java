package faceclient;

/**
 * Hello world!
 *
 */

public interface IFaceClient {
    public void Identify();
    public FaceRecognitionInfo GetSessionInfo();
    public void Close();
}

public FaceRecognitionInfo {
    private float confidenceInterval;
    private float id;

    public float getConfidenceInterval() {
        return confidenceInterval;
    }

    public Integer getId() {
        return id;
    }
}

public class FaceClient
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
