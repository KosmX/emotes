package com.kosmx.emotes.common.tools;

/**
 * Easings form https://easings.net
 */
public enum Ease {
    LINEAR(0), CONSTANT(1),
    INSINE(6), OUTSINE(7), INOUTSINE(8), INCUBIC(9), OUTCUBIC(10), INOUTCUBIC(11),
    INQUAD(12), OUTQUAD(13), INOUTQUAD(14), INQUART(15), OUTQUART(16), INOUTQUART(17),
    INQUINT(18), OUTQUINT(19), INOUTQUINT(20), INEXPO(21), OUTEXPO(22), INOUTEXPO(23),
    INCIRC(24), OUTCIRC(25), INOUTCIRC(26), INBACK(27), OUTBACK(28), INOUTBACK(29),
    INELASTIC(30), OUTELASTIC(31), INOUTELASTIC(32), INBOUNCE(33), OUTBOUNCE(34), INOUTBOUNCE(35);

    final byte id;

    /**
     * @param id id
     */
    Ease(byte id){
        this.id = id;
    }

    /**
     * @param id id
     */
    Ease(int id) {
        this((byte) id);
    }

    public byte getId() {
        return id;
    }

    //To be able to send these as bytes instead of String names.
    public static Ease getEase(byte b){
        for(Ease ease:Ease.values()){
            if(ease.id == b) return ease;
        }
        return LINEAR;
    }
}
