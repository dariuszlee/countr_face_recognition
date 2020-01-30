package countr.utils;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class DebugUtils {
    public static void saveImage(BufferedImage bufferedImage){
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String directoryPath = classloader.getResource("debug/").getPath();

        String outLocation = directoryPath + "/debug_img_"+ new SimpleDateFormat("yyyymmdd-hh.mm.ss").format(new Date(System.currentTimeMillis())) + ".jpg";
        File outFile = new File(outLocation);
        try{
            ImageIO.write(bufferedImage, "jpg", outFile);
        }
        catch (IOException ex){
            System.out.println("Debugging image failed...");
        }
    }

    public static void saveImage(BufferedImage bufferedImage, String fileName){
        try{
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            String directoryPath = classloader.getResource("debug/").getPath();

            String outLocation = directoryPath + fileName + ".jpg";
            File outFile = new File(outLocation);
            ImageIO.write(bufferedImage, "jpg", outFile);
        }
        catch (IOException ex){
            System.out.println("Debugging image failed. Unable to open file.");
        }
        catch (Exception ex){
            System.out.println("Failed to save image.");
            System.out.println(ex);
        }
    }

    public static void saveImage(Mat image, String fileName){
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String directoryPath = classloader.getResource("debug/").getPath();
        String outLocation = directoryPath + fileName + ".png";
        boolean res = Imgcodecs.imwrite(outLocation, image);
        if(!res){
            System.out.println("Debugging image failed...");
        }
    }

    public static void printMatrixInfo(Mat mat){
        final int channels = mat.channels();
        final int imageType =  mat.type();
        final int depth =  mat.depth();
        final int height =  mat.height();
        final int width =  mat.width();
        final byte[] b = new byte[height * width * channels];

        System.out.println("Length of bytes "+ b.length);
        System.out.println("width "+ width);
        System.out.println("height "+ height);
        System.out.println("type "+ imageType);
        System.out.println("depth "+ depth);
        System.out.println("channels "+ mat.channels());
        System.out.println("Is empty " + mat.empty());
    }
}
