package faceserver;

import org.junit.Test;

import countr.common.FaceLiveness;
import junit.framework.Assert;

public class FaceLivenessTest {
    @Test
    public void LoadAndBasicTest(){
        FaceLiveness livenessDetector = null;
        try {
            livenessDetector = new FaceLiveness();
        }
        catch (Exception e){
            System.out.println("Test Exception: " + e);
            // Assert.assertTrue(false);
        }
    }
}
