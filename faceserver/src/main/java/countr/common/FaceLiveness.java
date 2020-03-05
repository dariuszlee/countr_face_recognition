package countr.common;

import java.io.FileInputStream;
import java.io.File;

import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;

public class FaceLiveness {
    private final ComputationGraph faceLivenessNetwork;
    public FaceLiveness(){
        try {
            FileInputStream modelStream = new FileInputStream(new File("../Face-Liveness-Detection/models/anandfinal.hdf5"));
            faceLivenessNetwork = KerasModelImport.importKerasModelAndWeights(modelStream);
        }
        catch (Exception e){
            throw new 
        }
    }        

    public void status(){
        System.out.println("Running...");
    }
}
