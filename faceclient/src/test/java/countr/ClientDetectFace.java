package countr;

import org.junit.Test;
public class ClientDetectFace {
    
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
        String filePath = "";

        Assert.assertTrue(fc.ContainsFace(filePath));
    }

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
        Assert.assertFalse(fc.ContainsFace(filePath));
    }
}
