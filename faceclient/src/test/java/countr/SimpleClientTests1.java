package countr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import countr.common.EmbeddingResponse;
import countr.common.MatchResult;
import countr.faceclient.FaceClient;
import countr.faceclient.IFaceClient;

public class SimpleClientTests1 {
    @Test
    public void Test_AllCountRSamples() throws IOException {
        // Step 1: Load Face Client
        IFaceClient fc = null;
        try{
            fc = new FaceClient();
        }
        catch (Exception e){
            System.out.println(e);
            return;
        }
        int groupId = 20;
        int maxResults = 2;

        // Load some files to server instance
        ClientUsageExample1.ClearAndLoadResources(fc, groupId);
    }
}
