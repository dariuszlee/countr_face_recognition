package countr;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import countr.faceclient.FaceClient;
import countr.faceclient.IFaceClient;
import junit.framework.Assert;

public class ClientDetectFace {
    
    @Test
    public void Test_CascadeFail() throws IOException {
        IFaceClient fc = null;
        try{
            fc = new FaceClient();
        }
        catch (Exception e){
            System.out.println(e);
            return;
        }
        String filePath = "";

        Assert.assertTrue(!fc.ContainsFace(filePath).isSuccess());
    }

    @Test
    public void Test_CascadePass() throws IOException {
        IFaceClient fc = null;
        try{
            fc = new FaceClient();
        }
        catch (Exception e){
            System.out.println(e);
            return;
        }

        HashMap<String, List<String>> pathes = SimpleClientTests1.LoadYaleFaces();
        for(String s: pathes.keySet()){
            String filePath = pathes.get(s).get(0);
            Assert.assertTrue(fc.ContainsFace(filePath).isSuccess());
        }
    }
}
