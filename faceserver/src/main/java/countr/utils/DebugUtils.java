package countr.utils;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

public class DebugUtils {
    public static void saveImage(BufferedImage bufferedImage){
        String outLocation = "debug/debug_img_"+ new SimpleDateFormat("yyyymmdd-hh.mm.ss").format(new Date(System.currentTimeMillis())) + ".jpg";
        File outFile = new File(outLocation);
        try{
            ImageIO.write(bufferedImage, "jpg", outFile);
        }
        catch (IOException ex){
            System.out.println("Debugging image failed...");
        }
    }

    public static void saveImage(BufferedImage bufferedImage, String fileName){
        String outLocation = "debug/" + fileName + ".jpg";
        File outFile = new File(outLocation);
        try{
            ImageIO.write(bufferedImage, "jpg", outFile);
        }
        catch (IOException ex){
            System.out.println("Debugging image failed...");
        }
    }
}
