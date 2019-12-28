package countr.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.tzolov.cv.mtcnn.FaceAnnotation;
import net.tzolov.cv.mtcnn.MtcnnService;
import net.tzolov.cv.mtcnn.MtcnnUtil;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

/**
 * @author Christian Tzolov
 */
public class FaceDetection {

	public static void main(String[] args) throws IOException {

		MtcnnService mtcnnService = new MtcnnService(30, 0.709, new double[] { 0.6, 0.7, 0.7 });

		ObjectMapper jsonMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

		ResourceLoader resourceLoader = new DefaultResourceLoader();

		try (InputStream imageInputStream = resourceLoader.getResource("classpath:/pivotal-ipo-nyse.jpg").getInputStream()) {

			// 1. Load the input image (you can use http:/, file:/ or classpath:/ URIs to resolve the input image
			BufferedImage inputImage = ImageIO.read(imageInputStream);

			// 2. Run face detection
			FaceAnnotation[] faceAnnotations = mtcnnService.faceDetection(inputImage);

			// 3. Augment the input image with the detected faces
			BufferedImage annotatedImage = MtcnnUtil.drawFaceAnnotations(inputImage, faceAnnotations);

			// 4. Store face-annotated image
			ImageIO.write(annotatedImage, "png", new File("./AnnotatedImage.png"));

			// 5. Print the face annotations as JSON
			System.out.println("Face Annotations (JSON): " + jsonMapper.writeValueAsString(faceAnnotations));
		}
	}
}
}
