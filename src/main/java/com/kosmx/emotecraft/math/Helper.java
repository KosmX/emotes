package com.kosmx.emotecraft.math;

public class Helper {
    public static int colorHelper(int r, int g, int b, int a){
        return ((a & 255) << 24) | ((r & 255) << 16) | ((g & 255) << 8) | (b & 255);  //Sometimes minecraft uses ints as color... not my idea,
    }
}
