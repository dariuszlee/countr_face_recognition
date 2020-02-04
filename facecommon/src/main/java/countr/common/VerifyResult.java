package countr.common;

import java.util.Arrays;

public class VerifyResult extends ServerResult {
    RecognitionMatch topMatch;

    public VerifyResult(RecognitionMatch topMatch, boolean success){
        super(success);
        this.topMatch = topMatch;
    }

    public VerifyResult(RecognitionMatch topMatch, boolean success, String message){
        super(success, message);
        this.topMatch = topMatch;
    }

    @Override
    public String toString() {
        return super.toString() + "VerifyResult [topMatch=" + topMatch + "]";
    }
}
