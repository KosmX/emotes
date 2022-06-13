package io.github.kosmx.emotes.common.tools;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

//some Vector implementation. This is only for storing values
@Immutable
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector3)) return false;
        Vector3<?> vector3 = (Vector3<?>) o;
        return Objects.equals(x, vector3.x) && Objects.equals(y, vector3.y) && Objects.equals(z, vector3.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return "Vec3f[" + this.getX() + "; " + this.getY() + "; " + this.getZ() + "]";
    }

}
