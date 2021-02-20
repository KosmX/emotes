package com.kosmx.emotes.common.tools;

import java.util.Vector;

//some Vector implementation. This is only for storing values
public class Vector3 <N extends Number>{
    N x, y, z;

    public Vector3(N x, N y, N z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public N getX() {
        return x;
    }

    public N getY() {
        return y;
    }

    public N getZ() {
        return z;
    }
}
