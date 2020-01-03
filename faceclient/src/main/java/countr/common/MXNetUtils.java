package countr.common;

import org.apache.mxnet.javaapi.Context;
import java.util.ArrayList;
import java.util.List;

public class MXNetUtils {  
    public static void main(String[] args) {
        System.out.println("Hello world");
        List<Context> ctx = new ArrayList<>();
        ctx.add(Context.cpu()); // Choosing CPU Context here
    }
}
