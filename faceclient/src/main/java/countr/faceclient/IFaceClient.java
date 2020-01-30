package countr.faceclient;

import org.opencv.core.Mat;

import countr.common.EmbeddingResponse;
import countr.common.FaceEmbedding;
import countr.common.MatchResult;
import countr.common.RecognitionResult;
import countr.common.ServerResult;

import java.awt.image.BufferedImage;

public interface IFaceClient {
    public Mat ReadCamera(int deviceId);

    public RecognitionResult AddPhoto(String path, String userId, int groupId);
    public RecognitionResult AddPhoto(Mat mat, String userId, int groupId);

    public MatchResult Match(String path, final int groupId, final int maxResults);
    public MatchResult Match(Mat image, final int groupId, final int maxResults);

    public RecognitionResult Recognize(String imagePath, int groupId);
    public RecognitionResult Recognize(Mat image, int groupId);

    public ServerResult DeleteUser(String userId, int groupId);
    public ServerResult DeleteGroup(int groupId);

    public EmbeddingResponse GetEmbeddings(int groupId);

    public ServerResult Close();
}
