package com.kosmx.bendylib.objects;

import net.minecraft.client.util.math.Vector3f;

import java.util.function.Supplier;

public class RepositionableVertex implements IVertex {
    public final float u;
    public final float v;
    protected final Pos3f pos;

    public RepositionableVertex(float u, float v, Pos3f pos) {
        this.u = u;
        this.v = v;
        this.pos = pos;
    }

    /**
     * Vertex able to change position with setting the pos
     * @param u texture u
     * @param v texture v
     * @param pos If you will edit the Object use {@link Vector3f#copy()}
     */
    public RepositionableVertex(float u, float v, Vector3f pos){
        this(u, v, new Pos3f(pos));
    }

    @Override
    public RepositionableVertex remap(float u, float v){
        return new RepositionableVertex(u, v, this.pos);
    }

    @Override
    public Vector3f getPos() {
        return pos.get();
    }

    @Override
    public float getU() {
        return this.u;
    }

    @Override
    public float getV() {
        return this.v;
    }

    public void setPos(Vector3f vector3f){
        this.pos.set(vector3f);
    }
    protected static class Pos3f implements Supplier<Vector3f>{
        protected Vector3f pos;

        public Pos3f(Vector3f pos){
            this.pos = pos;
        }

        @Override
        public Vector3f get() {
            return this.pos;
        }
        public void set(Vector3f vector3f){
            this.pos = vector3f;
        }
    }
}
