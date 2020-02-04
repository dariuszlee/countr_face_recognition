package countr.common;

import java.util.Arrays;

public class MatchResult extends ServerResult {
    private final RecognitionMatch[] matches;

    public MatchResult(RecognitionMatch[] matches, boolean success){
        super(success);
        this.matches = matches;
    }

    public MatchResult(RecognitionMatch[] matches, boolean success, String message){
        super(success, message);
        this.matches = matches;
    }

    @Override
    public String toString() {
        return super.toString() + "MatchResult [matches=" + Arrays.toString(matches) + "]";
    }

    public RecognitionMatch[] getMatches() {
        return matches;
    }
}
