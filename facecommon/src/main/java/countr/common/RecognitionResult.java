package countr.common;

import java.io.Serializable;
import org.nd4j.linalg.cpu.nativecpu.NDArray;

public class RecognitionResult implements Serializable {
    final float[] result;
    final boolean success;

    public RecognitionResult(NDArray result, boolean success) {
        this.result = result;
        this.success = success;
    }

    public NDArray getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "RecognitionResult [success=" + success + "]";
    }
}
