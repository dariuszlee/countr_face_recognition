package countr.common;

import org.opencv.core.Mat;

public class DebugUtils {
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
    }
}
