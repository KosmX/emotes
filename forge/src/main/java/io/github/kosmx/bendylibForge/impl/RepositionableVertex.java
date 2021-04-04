package io.github.kosmx.bendylibForge.impl;

import com.mojang.math.Vector3f;

/**
 * This vertex's position can be changed.
 */
public class RepositionableVertex implements IRepositionableVertex {
    public final float u;
    public final float v;
    protected final RememberingPos pos;

    public RepositionableVertex(float u, float v, RememberingPos pos) {
        this.u = u;
        this.v = v;
        this.pos = pos;
    }


    @Override
    public RepositionableVertex remap(float u, float v){
        return new RepositionableVertex(u, v, this.pos);
    }

    @Override
    public Vector3f getPos() {
        return pos.getPos();
    }

    @Override
    public float getU() {
        return this.u;
    }

    @Override
    public float getV() {
        return this.v;
    }


    public RememberingPos getPosObject(){
        return this.pos;
    }
}
