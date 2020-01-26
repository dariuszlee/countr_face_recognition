package countr.common;

import java.io.Serializable;
import org.nd4j.linalg.cpu.nativecpu.NDArray;

public class RecognitionResult implements Serializable {
    final NDArray result;
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
}
