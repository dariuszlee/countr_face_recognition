package countr.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;

public class FaceLiveness {
    private final MultiLayerNetwork faceLivenessNetwork;
    public FaceLiveness() throws IOException {
        try {
            String path = "../Face-Liveness-Detection/models/anandfinal.hdf5";
            // FileInputStream modelStream = new FileInputStream(new File("../Face-Liveness-Detection/models/anandfinal.hdf5"));
            faceLivenessNetwork = KerasModelImport.importKerasSequentialModelAndWeights(path);
        }
        catch (IOException e){
            throw e;
        }
        catch (Exception e){
            throw new IOException(e.toString());
        }
    }        

    public void status(){
        System.out.println("Running...");
    }
}
