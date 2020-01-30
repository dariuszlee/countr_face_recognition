package countr;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.Test;

public class ClientUsageExample1 {
    public static HashMap<String, String> idPaths(){
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String directoryPath = classloader.getResource("example_face_from_countr/ExampleDataFaceRecognition/ID_Images/").getFile();
        File directory = new File(directoryPath);
        File[] fileList = directory.listFiles();
        for(File file : fileList){
            System.out.println(file.getAbsolutePath());
        }
        return new HashMap<String, String>();
    }

    @Test
    public void Test_ShowNoRecognition() throws IOException {
        ClientUsageExample1.idPaths();

    }

    public void Test_ShowRecognition() {

    }

    public void Test_ShowVerify() {

    }

    public void Test_ShowNoVerify() {

    }
}
