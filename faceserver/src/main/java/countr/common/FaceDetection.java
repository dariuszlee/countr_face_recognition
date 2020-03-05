package countr.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.tzolov.cv.mtcnn.FaceAnnotation;
import net.tzolov.cv.mtcnn.FaceAnnotation.BoundingBox;
import net.tzolov.cv.mtcnn.MtcnnService;
import net.tzolov.cv.mtcnn.MtcnnUtil;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import countr.utils.DebugUtils;

import org.datavec.image.loader.Java2DNativeImageLoader;
import org.nd4j.linalg.api.ndarray.INDArray;
import static org.nd4j.linalg.indexing.NDArrayIndex.all;
import static org.nd4j.linalg.indexing.NDArrayIndex.point;
import static org.nd4j.linalg.indexing.NDArrayIndex.interval;

public class FaceDetection {
    MtcnnService  mtcnnService;
    boolean isDebug;

    public FaceDetection(boolean isDebug){
		this.mtcnnService = new MtcnnService(30, 0.709, new double[] { 0.6, 0.7, 0.7 });
        this.isDebug = isDebug;
    }

    public BufferedImage detect(BufferedImage inputImage) throws IOException {
        // 2. Run face detection
        Java2DNativeImageLoader imageLoader = new Java2DNativeImageLoader();
        INDArray ndImage3HW = imageLoader.asMatrix(inputImage).get(point(0), interval(0, 3), all(), all());
        FaceAnnotation[] faceAnnotations = this.mtcnnService.faceDetection(ndImage3HW);

        System.out.println("Face length: " + faceAnnotations.length);
        if(faceAnnotations.length == 0) {
            DebugUtils.saveImage(inputImage, "received_after");
            return null;
        }

        if(this.isDebug){
            BufferedImage annotatedImage = MtcnnUtil.drawFaceAnnotations(inputImage, faceAnnotations);
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            String directoryPath = classloader.getResource("debug/").getPath();
            String imageDebugPath = directoryPath + "./AnnotatedImage.png";
            ImageIO.write(annotatedImage, "png", new File(imageDebugPath));
        }


        int margin = 44;
        int alignedImageSize = 112;
        FaceAnnotation bbox = this.getBiggestFace(faceAnnotations);
        INDArray alignedFace = mtcnnService.faceAlignment(ndImage3HW, bbox, margin, alignedImageSize, false);
        BufferedImage image = new Java2DNativeImageLoader().asBufferedImage(alignedFace);

        return image;
    }

    private FaceAnnotation getBiggestFace(FaceAnnotation [] faces){
        int largestArea = 0;
        FaceAnnotation largestAreaIndex = null;
        for(FaceAnnotation face : faces){
            BoundingBox bbox = face.getBoundingBox();
            int area =  bbox.getW() * bbox.getH();
            if(area > largestArea){
                largestAreaIndex = face;
                largestArea = area;
            }
        }
        return largestAreaIndex;
    }

	public static void main(String[] args) throws IOException {
		ResourceLoader resourceLoader = new DefaultResourceLoader();

        FaceDetection faceDetector = new FaceDetection(true);

		try (InputStream imageInputStream = resourceLoader.getResource("classpath:/trainer_reference.jpg").getInputStream()) {

			// 1. Load the input image (you can use http:/, file:/ or classpath:/ URIs to resolve the input image
			BufferedImage inputImage = ImageIO.read(imageInputStream);

            BufferedImage image = faceDetector.detect(inputImage);

            ImageIO.write(image, "jpg", new File("./cropped" + ".jpg"));
            System.out.println(".");
		}
	}
}
