package countr.common;

public class DetectFaceResult extends ServerResult {
    private final int numberOfFaces;
    public DetectFaceResult(boolean success, String message){
        super(success, message);
        this.numberOfFaces = 0;
    }

    public DetectFaceResult(boolean success, String message, int numberOfFaces){
        super(success, message);
        this.numberOfFaces = numberOfFaces;
    }

    @Override
    public String toString() {
        return super.toString() + " DetectFaceResult [numberOfFaces=" + numberOfFaces + "]";
    }

    public int getNumberOfFaces() {
        return numberOfFaces;
    }
}
