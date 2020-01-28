package countr.utils;

import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.lang.Math;

import countr.common.FaceEmbedding;
import countr.common.RecognitionMatch;

public class ComputeUtils {
    public static float EucDistance(float[] feature){
        float dot = getDotProduct(feature, feature);
        return (float) Math.sqrt(dot); 
    }

    public static float[] Normalize(float[] feature){
        float distance = EucDistance(feature);
        float[] normed = new float[feature.length];
        for(int i = 0; i < normed.length; i++){
            normed[i] = feature[i] / distance;
        }
        return normed;
    }

    public static RecognitionMatch[] Match(float[] toMatch, FaceEmbedding[] database, int numberOfResults){
        PriorityQueue<RecognitionMatch> maxHeap = new PriorityQueue<RecognitionMatch>(numberOfResults);
        for(FaceEmbedding embedding : database){
            float dotResult = getDotProduct(toMatch, embedding.getEmbedding());
            maxHeap.add(new RecognitionMatch(embedding.getId(), dotResult));
        }

        RecognitionMatch[] results = new RecognitionMatch[numberOfResults];
        for(int i = 0; i < numberOfResults; ++i){
            results[i] = maxHeap.poll();
        }
        return results;
    }    

    public static float getDotProduct(float[] left, float[] right){
        float sum = 0;
        for(int i = 0; i < left.length; ++i){
            sum += left[i] * right[i];
        }
        return sum;
    }

    public static void main(String[] args) {
        FaceEmbedding[] db = new FaceEmbedding[]{
            new FaceEmbedding("1", new float[]{1}, 1),
            new FaceEmbedding("2", new float[]{2}, 1),
            new FaceEmbedding("3", new float[]{3}, 1),
        };

        RecognitionMatch[] results = ComputeUtils.Match(new float[]{1}, db, 2);
        for (RecognitionMatch r : results) {
            System.out.println(r);
        }

        float[] normed = ComputeUtils.Normalize(new float[]{1, 5, 3});
        System.out.println(Arrays.toString(normed));
    }
}
