package com.kosmx.bendylib.objects;

import net.minecraft.client.util.math.Vector3f;

/**
 * To create custom Vertices
 */
public interface IVertex {
    Vector3f getPos();

    float getU();
    float getV();

    IVertex remap(float u, float v);

}
