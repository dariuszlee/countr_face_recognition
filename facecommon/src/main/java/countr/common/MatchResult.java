package countr.common;

import java.util.Arrays;

public class MatchResult extends ServerResult {
    RecognitionMatch[] matches;

    public MatchResult(RecognitionMatch[] matches, boolean success){
        super(success);
        this.matches = matches;
    }

    @Override
    public String toString() {
        return super.toString() + "MatchResult [matches=" + Arrays.toString(matches) + "]";
    }
}
