package countr.faceclient;

import org.opencv.core.Mat;

public interface IFaceClient {
    public void Identify();
    public void Recognize(Mat m);
    public void Close();
}
