package com.kosmx.emotecraft.math;

import com.kosmx.emotecraft.Main;
import org.apache.logging.log4j.Level;

public class Easing {

    /*
     * Easing functions from easings.net
     * All function have a string codename
     * EasingFromString
     *
     * All function needs an input between 0 and 1
     * except
     */
    public static float easingFromEnum(Ease type, float f){
        switch (type){
            case INOUTSINE:
                return inOutSine(f);
            case INSINE:
                return inSine(f);
            case OUTSINE:
                return outSine(f);
            case INCUBIC:
                return inCubic(f);
            case OUTCUBIC:
                return outCubic(f);
            case LINEAR:
                return f;
            case INOUTCUBIC:
                return inOutCubic(f);
            case INQUAD:
                return inQuad(f);
            case INQUART:
                return inQuart(f);
            case OUTQUAD:
                return outQuad(f);
            case OUTQUART:
                return outQuart(f);
            case INOUTQUAD:
                return inOutQuad(f);
            case INOUTQUART:
                return inOutQuart(f);
            case INBACK:
                return inBack(f);
            case INCIRC:
                return inCirc(f);
            case INEXPO:
                return inExpo(f);
            case INQUINT:
                return inQuint(f);
            case OUTBACK:
                return outBack(f);
            case OUTCIRC:
                return outCirc(f);
            case OUTEXPO:
                return outExpo(f);
            case INBOUNCE:
                return inBounce(f);
            case OUTQUINT:
                return outQuint(f);
            case INELASTIC:
                return inElastic(f);
            case INOUTBACK:
                return inOutBack(f);
            case INOUTCIRC:
                return inOutCirc(f);
            case INOUTEXPO:
                return inOutExpo(f);
            case OUTBOUNCE:
                return outBounce(f);
            case INOUTQUINT:
                return inOutQuint(f);
            case OUTELASTIC:
                return outElastic(f);
            case INOUTBOUNCE:
                return inOutBounce(f);
            case INOUTELASTIC:
                return inOutElastic(f);
            case CONSTANT:
                return 0;
            default:
                Main.log(Level.WARN, "easing function unknown: " + type);
                return f;
        }
    }

    /**
     *
     * @param string
     * @return ease
     */
    public static Ease easeFromString(String string){
        try {
            if(string.substring(0, 4).toUpperCase().equals("EASE")){
                string = string.substring(4);
            }
            return Ease.valueOf(string.toUpperCase());
        }
        catch (Exception exception){
            Main.log(Level.ERROR, "Ease name unknown: \"" + string + "\" using linear", true);
            Main.log(Level.WARN, exception.toString());
            return Ease.LINEAR;
        }
    }

    private static final float c1 = 1.70158f;
    private static final float c2 = c1 * 1.525f;
    private static final float c3 = c1 + 1;
    private static final float c4 = (float) ((2 * Math.PI) / 3);
    private static final float c5 = (float) ((2 * Math.PI) / 4.5);
    private static final float n1 = 7.5625f;
    private static final float d1 = 2.75f;


    public static float inSine(float f){
        return (float) (1-Math.cos((f * Math.PI)/2));
    }
    public static float outSine(float f){
        return (float) (Math.sin((f * Math.PI)/2));
    }
    public static float inOutSine(float f){
        return (float) (-(Math.cos(Math.PI * f) - 1) / 2);
    }
    public static float inCubic(float f){
        return f*f*f;
    }
    public static float outCubic(float f){
        return (float) (1 - Math.pow(1-f, 3));
    }
    public static float inOutCubic(float x){
        return (float) ((x < 0.5) ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2);
    }
    public static float inQuad(float x){
        return (float)(x * x);
    }
    public static float outQuad(float x){
        return (float)(1 - (1 - x) * (1 - x));
    }
    public static float inOutQuad(float x){
        return (float)((x < 0.5) ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2);
    }
    public static float inQuart(float x){
        return (float)(x * x * x * x);
    }
    public static float outQuart(float x){
        return (float)(1 - Math.pow(1 - x, 4));
    }
    public static float inOutQuart(float x){
        return (float)(x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2);
    }
    public static float inQuint(float x){
        return (float)(x * x * x * x * x);
    }
    public static float outQuint(float x){
        return (float)(1 - Math.pow(1 - x, 5));
    }
    public static float inOutQuint(float x){
        return (float)(x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2);
    }
    public static float inExpo(float x){
        return (float)(x == 0 ? 0 : Math.pow(2, 10 * x - 10));
    }
    public static float outExpo(float x){
        return (float)(x == 1 ? 1 : 1 - Math.pow(2, -10 * x));
    }
    public static float inOutExpo(float x){
        return (float)(x == 0
                ? 0
                : x == 1
                ? 1
                : x < 0.5 ? Math.pow(2, 20 * x - 10) / 2
                : (2 - Math.pow(2, -20 * x + 10)) / 2);
    }
    public static float inCirc(float x){
        return (float)(1 - Math.sqrt(1 - Math.pow(x, 2)));
    }
    public static float outCirc(float x){
        return (float)(Math.sqrt(1 - Math.pow(x - 1, 2)));
    }
    public static float inOutCirc(float x){
        return (float)(x < 0.5
                ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2
                : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2);
    }
    public static float inBack(float x){

        return c3 * x * x * x - c1 * x * x;
    }
    public static float outBack(float x){
        return (float)(1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
    }
    public static float inOutBack(float x){
        return (float)(x < 0.5
                ? (Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2
                : (Math.pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2);
    }
    public static float inElastic(float x){
        return (float)(x == 0
                ? 0
                : x == 1
                ? 1
                : -Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * c4));
    }
    public static float outElastic(float x){
        return (float)(x == 0
                ? 0
                : x == 1
                ? 1
                : Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1);
    }
    public static float inOutElastic(float x){
        return (float)(x == 0
                ? 0
                : x == 1
                ? 1
                : x < 0.5
                ? -(Math.pow(2, 20 * x - 10) * Math.sin((20 * x - 11.125) * c5)) / 2
                : (Math.pow(2, -20 * x + 10) * Math.sin((20 * x - 11.125) * c5)) / 2 + 1);
    }
    public static float inBounce(float x){
        return 1 - outBounce(1 - x);
    }
    public static float outBounce(float x){
        if(x < 1 / d1){
            return n1*x*x;
        }
        else if(x < 2 / d1){
            return (float) (n1 * (x -= 1.5 / d1) * x + 0.75);
        }
        else if(x < 2.5 / d1) {
            return (float) (n1 * (x -= 2.25 / d1) * x + 0.9375);
        }
        else {
            return (float) (n1 * (x -= 2.625 / d1) * x + 0.984375);
        }
    }
    public static float inOutBounce(float x){
        return x < 0.5 ? (1 - outBounce(1 - 2 * x)) / 2 : (1 + outBounce(2 * x - 1)) / 2;
    }
}
