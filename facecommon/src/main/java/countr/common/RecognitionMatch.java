package countr.common;

import java.io.Serializable;
import java.util.Comparator;

public class RecognitionMatch implements Comparable, Serializable{
    final private String id;
    final private float match;

    public RecognitionMatch(String id, float match) {
        this.id = id;
        this.match = match;
    }

	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
        if(((RecognitionMatch) arg0).getMatch() < this.getMatch()){
            return -1;
        }
        else{
            return 1;
        }
	}

	public int compare(RecognitionMatch arg0, RecognitionMatch arg1) {
		Float comp = arg1.getMatch() - arg0.getMatch();
        return comp.intValue(); 
	}

    public String getId() {
        return id;
    }

    public float getMatch() {
        return match;
    }

    @Override
    public String toString() {
        return "RecognitionMatch [id=" + id + ", match=" + match + "]";
    }

}
