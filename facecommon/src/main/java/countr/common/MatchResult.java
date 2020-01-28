package countr.common;

import java.util.Arrays;

public class MatchResult extends ServerResult {
    RecognitionMatch[] matches;

    public MatchResult(RecognitionMatch[] matches, boolean success){
        super(success);
        this.matches = matches;
    }
}
