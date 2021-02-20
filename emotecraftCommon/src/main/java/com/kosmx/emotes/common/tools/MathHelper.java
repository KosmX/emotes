package com.kosmx.emotes.common.tools;

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

}
