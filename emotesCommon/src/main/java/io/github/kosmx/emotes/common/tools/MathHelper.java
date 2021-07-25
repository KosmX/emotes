package io.github.kosmx.emotes.common.tools;

import io.github.kosmx.emotes.api.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

//like MC math helper but without MC
public class MathHelper {

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    public static int colorHelper(int r, int g, int b, int a){
        return ((a & 255) << 24) | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);  //Sometimes minecraft uses ints as color...
    }

    public static float clampToRadian(float f){
        final double a = Math.PI*2;
        double b = ((f + Math.PI)%a);
        if(b < 0){
            b += a;
        }
        return (float) (b - Math.PI);
    }


    /**
     * similar? to Java 9+ {@link InputStream#readAllBytes()}
     * because of compatibility, I can not use that
     * @param stream read this stream
     * @return ByteBuffer from stream
     * @throws IOException ...
     */
    public static ByteBuffer readFromIStream(InputStream stream) throws IOException {
        List<Pair<Integer, byte[]>> listOfBites = new LinkedList<>();
        int totalSize = 0;
        while (true){
            int estimatedSize = stream.available();
            byte[] bytes = new byte[Math.max(1, estimatedSize)];
            int i = stream.read(bytes);
            if(i < 1) break;
            totalSize += i;
            listOfBites.add(new Pair<>(i, bytes));
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(totalSize);
        for(Pair<Integer, byte[]> i:listOfBites){
            byteBuffer.put(i.getRight(), 0, i.getLeft());
        }
        byteBuffer.position(0); //set position to 0, we'll read it
        return byteBuffer;
    }
}
