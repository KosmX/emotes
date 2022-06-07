package io.github.kosmx.bendylibForge.impl;

import com.mojang.math.Vector3f;

import java.util.Objects;

public class RememberingPos implements IPosWithOrigin{
    final Vector3f originPos;
    Vector3f currentPos = null;

    public RememberingPos(Vector3f originPos) {
        this.originPos = originPos;
    }

    public RememberingPos(float x, float y, float z){
        this(new Vector3f(x, y, z));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RememberingPos)) return false;

        RememberingPos that = (RememberingPos) o;

        if (!originPos.equals(that.originPos)) return false;
        return Objects.equals(currentPos, that.currentPos);
    }

    @Override
    public int hashCode() {
        int result = originPos.hashCode();
        result = 31 * result + (currentPos != null ? currentPos.hashCode() : 0);
        return result;
    }

    /**
     * It will return with a copy
     * @return copy of the original pos
     */
    @Override
    public Vector3f getOriginalPos() {
        return originPos.copy(); //I won't let anyone to change it.
    }

    @Override
    public Vector3f getPos() {
        return currentPos;
    }

    @Override
    public void setPos(Vector3f vector3f) {
        this.currentPos = vector3f;
    }
}
