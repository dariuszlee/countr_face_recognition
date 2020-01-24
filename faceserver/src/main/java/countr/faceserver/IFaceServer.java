package countr.faceserver;

import countr.common.RecognitionMessage;
import countr.common.RecognitionResult;

public interface IFaceServer {
    RecognitionResult Recognize(RecognitionMessage message); 
}
