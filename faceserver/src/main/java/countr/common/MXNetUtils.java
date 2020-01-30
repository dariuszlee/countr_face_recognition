package countr.common;

import java.io.InputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.FileInputStream;

import org.datavec.image.loader.Java2DNativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;

import static org.nd4j.linalg.indexing.NDArrayIndex.all;
import static org.nd4j.linalg.indexing.NDArrayIndex.point;
import static org.nd4j.linalg.indexing.NDArrayIndex.interval;

import org.apache.mxnet.javaapi.Context;
import org.apache.mxnet.javaapi.DataDesc;
import org.apache.mxnet.javaapi.Shape;
import org.apache.mxnet.javaapi.DType;

import org.apache.mxnet.javaapi.NDArray;

import org.apache.mxnet.infer.javaapi.Predictor;

import java.util.ArrayList;
import java.util.List;

public class MXNetUtils {  
    private static Shape inputShape = new Shape(new int[]{1, 3, 112, 112});

    private List<Context> ctx;
    private Predictor resnet100;

    public MXNetUtils(boolean isGpu, String modelPath) {
        this.ctx = new ArrayList<>();
        if(isGpu){
            this.ctx.add(Context.gpu()); // Choosing CPU Context here
        }
        else {
            this.ctx.add(Context.cpu()); // Choosing CPU Context here
        }

        this.resnet100 = generatePredictor(modelPath);
    }

    private Predictor generatePredictor(String modelPath) {
        List<DataDesc> inputDesc = new ArrayList<>();
        inputDesc.add(new DataDesc("data", inputShape, DType.Float32(), "NCHW"));

        Predictor resnet100 = new Predictor(modelPath, inputDesc, this.ctx, 0);
        return resnet100;
    }

    public float[] predict(BufferedImage inputImage) throws IOException {
        Java2DNativeImageLoader imageLoader = new Java2DNativeImageLoader();
        INDArray ndImage3HW = imageLoader.asMatrix(inputImage).get(point(0), interval(0, 3), all(), all());
        float[] ints = ndImage3HW.data().asFloat();
        NDArray img = new NDArray(ints, inputShape, this.ctx.get(0));

        List<NDArray> imgs = new ArrayList<NDArray>();
        imgs.add(img);

        List<NDArray> res = this.resnet100.predictWithNDArray(imgs);
        return this.converNdArray(res.get(0));
    }

    // private org.nd4j.linalg.cpu.nativecpu.NDArray converNdArray(NDArray feature){
    //     float[] featureAsArray = feature.toArray();
    //     return new org.nd4j.linalg.cpu.nativecpu.NDArray(featureAsArray);
    // }

    private float[] converNdArray(NDArray feature){
        return feature.toArray();
    }

    public static void main(String[] args) {
        String modelPath = "/home/dzlyy/projects/countr_face_recognition/faceclient/model-r100-ii/model";
        boolean isGpu = false;

        MXNetUtils resnet100 = new MXNetUtils(isGpu, modelPath);

        String imgPath = "/home/dzlyy/projects/countr_face_recognition/faceclient/cropped.jpg";
		try (InputStream imageInputStream = new FileInputStream(imgPath)) {
            BufferedImage inputImage = ImageIO.read(imageInputStream);

            float[] res = resnet100.predict(inputImage);
            System.out.println(res);
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
}
