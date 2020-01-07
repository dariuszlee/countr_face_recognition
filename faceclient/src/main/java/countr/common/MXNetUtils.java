package countr.common;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.FileInputStream;

import org.datavec.image.loader.Java2DNativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.buffer.DataBuffer;

import static org.nd4j.linalg.indexing.NDArrayIndex.all;
import static org.nd4j.linalg.indexing.NDArrayIndex.point;
import static org.nd4j.linalg.indexing.NDArrayIndex.interval;

import org.apache.mxnet.javaapi.Context;
import org.apache.mxnet.javaapi.DataDesc;
import org.apache.mxnet.javaapi.Shape;
import org.apache.mxnet.javaapi.DType;

import org.apache.mxnet.javaapi.Image;
import org.apache.mxnet.javaapi.NDArray;

import org.apache.mxnet.infer.javaapi.Predictor;

import java.util.ArrayList;
import java.util.List;

public class MXNetUtils {  
    public static Shape inputShape = new Shape(new int[]{1, 3, 112, 112});

    public static Predictor generatePredictor(String modelPath, boolean isGpu) {
        List<Context> ctx = new ArrayList<>();
        if(isGpu){
            ctx.add(Context.gpu()); // Choosing CPU Context here
        }
        else {
            ctx.add(Context.cpu()); // Choosing CPU Context here
        }

        List<DataDesc> inputDesc = new ArrayList<>();
        inputDesc.add(new DataDesc("data", inputShape, DType.Float32(), "NCHW"));

        Predictor resnet100 = new Predictor(modelPath, inputDesc, ctx, 0);
        return resnet100;
    }

    public static List<NDArray> predict(BufferedImage inputImage, Predictor resnet100){
        Java2DNativeImageLoader imageLoader = new Java2DNativeImageLoader();
        INDArray ndImage3HW = imageLoader.asMatrix(inputImage).get(point(0), interval(0, 3), all(), all());
        float[] ints = ndImage3HW.data().asFloat();
        NDArray img = new NDArray(ints, inputShape, ctx.get(0));
        List<NDArray> imgs = new ArrayList<NDArray>();
        imgs.add(img);
        List<NDArray> res = resnet100.predictWithNDArray(imgs);
        System.out.println(res);
        return res;
    }


    public static void main(String[] args) {
        String modelPath = "/home/dzly/projects/countr_face_recognition/faceclient/model-r100-ii/model";
        Predictor resnet100 = MXNetUtils.generatePredictor(modelPath, false);


        String imgPath = "/home/dzly/projects/countr_face_recognition/faceclient/cropped.jpg";
		try (InputStream imageInputStream = new FileInputStream(imgPath)) {
            BufferedImage inputImage = ImageIO.read(imageInputStream);
            Java2DNativeImageLoader imageLoader = new Java2DNativeImageLoader();
            INDArray ndImage3HW = imageLoader.asMatrix(inputImage).get(point(0), interval(0, 3), all(), all());
            float[] ints = ndImage3HW.data().asFloat();
            System.out.println(ints);
            NDArray img = new NDArray(ints, inputShape, ctx.get(0));
            // NDArray img = Image.imDecode(ints);
            // System.out.println(img);
            // NDArray img2 = img.reshape(new int[]{1, 3, 112, 112});
            // NDArray img3 = img2.asType(DType.UInt8());
            // System.out.println(img3);
            // // System.out.println("Type: " + img2.dtype());
            List<NDArray> imgs = new ArrayList<NDArray>();
            imgs.add(img);
            List<NDArray> res = resnet100.predictWithNDArray(imgs);
            System.out.println(res);
        }
        catch(Exception e) {
            System.out.println(e);
        }
    }
}
