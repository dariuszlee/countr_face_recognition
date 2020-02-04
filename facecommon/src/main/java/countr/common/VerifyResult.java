package countr.common;

public class VerifyResult extends ServerResult {
    private final RecognitionMatch topMatch;

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

    public RecognitionMatch getTopMatch() {
        return topMatch;
    }
}
