package countr.faceclient;

import org.opencv.core.Mat;

public interface IFaceClient {
    public void Recognize(Mat m);
    public void Close();
}
