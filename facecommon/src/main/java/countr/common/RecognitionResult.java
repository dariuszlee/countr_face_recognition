package countr.common;

import java.util.Arrays;

public class RecognitionResult extends ServerResult {
    final float[] result;

    public RecognitionResult(float[] result, boolean success) {
        super(success);
        this.result = result;
    }

    public RecognitionResult(float[] result, boolean success, String message) {
        super(success, message);
        this.result = result;
    }

    public float[] getResult() {
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "RecognitionResult [result=" + Arrays.toString(result) + "]";
    }
}
