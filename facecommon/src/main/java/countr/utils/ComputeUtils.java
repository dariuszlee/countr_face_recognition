package countr.utils;

import java.util.Arrays;
import java.util.HashSet;
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
        // Step 1: Get All Distances
        PriorityQueue<RecognitionMatch> maxHeap = new PriorityQueue<RecognitionMatch>();
        for(FaceEmbedding embedding : database){
            float dotResult = getDotProduct(toMatch, embedding.getEmbedding());
            maxHeap.add(new RecognitionMatch(embedding.getId(), dotResult));
        }

        // Step 2: Keep Only Unique Ids and Preserve Id
        PriorityQueue<RecognitionMatch> result = new PriorityQueue<RecognitionMatch>(numberOfResults);
        HashSet<RecognitionMatch> uniqueIds = new HashSet<RecognitionMatch>();
        int count = 0;
        for(RecognitionMatch match : maxHeap){
            if(uniqueIds.add(match) && result.add(match) && 
                    ++count == numberOfResults){
                break;
            }
        }

        return result.toArray(new RecognitionMatch[]{});
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
            new FaceEmbedding("3", new float[]{7}, 1),
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
