package io.github.kosmx.bendylibForge.impl;

import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.ModelPart;

/**
 * To create custom Vertices
 * You can use {@link Vertex} or {@link RepositionableVertex}
 */
public interface IVertex {

    /**
     * get the vertex's position
     * @return pos
     */
    Vector3f getPos();

    /**
     * get texture u coordinate
     * @return texture U
     */
    float getU();

    /**
     * get texture v coordinate
     * @return texture V
     */
    float getV();

    /**
     * Keep the original position, set unique texture coordinates.
     * @param u Texture U
     * @param v Texture V
     * @return The remapped IVertex
     */
    IVertex remap(float u, float v);

    default ModelPart.Vertex toMojVertex(){
        return new ModelPart.Vertex(this.getPos(), getU(), getV());
    }

}
