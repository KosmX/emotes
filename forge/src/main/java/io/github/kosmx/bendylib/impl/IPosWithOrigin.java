package io.github.kosmx.bendylib.impl;

import com.mojang.math.Vector3f;

/**
 * Position vector what is storing its original position
 */
public interface IPosWithOrigin {
    /**
     * Return a COPY of its original position
     * @return this.vector.copy()
     */
    Vector3f getOriginalPos();

    /**
     * @return current position
     */
    Vector3f getPos();

    /**
     * set a new value to current pos
     * @param vector3f new position
     */
    void setPos(Vector3f vector3f);
}
