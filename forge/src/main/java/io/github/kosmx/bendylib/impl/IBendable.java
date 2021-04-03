package io.github.kosmx.bendylib.impl;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.core.Direction;

/**
 * Bending methods.
 * Interface to be usable via Mixin
 */
public interface IBendable {

    /**
     * Applies the transformation to every position in posSupplier
     * @param bendAxis axis for the bend
     * @param bendValue bend value
     * @param posSupplier iterable positions
     * @return the used transformation matrix
     */
    default Matrix4f applyBend(float bendAxis, float bendValue, IterableRePos posSupplier){
        Vector3f axis = new Vector3f((float) Math.cos(bendAxis), 0, (float) Math.sin(bendAxis));
        Matrix3f matrix3f = new Matrix3f(getBendDirection().getRotation());
        axis.transform(matrix3f);
        Matrix4f transformMatrix = new Matrix4f();
        transformMatrix.setIdentity();

        transformMatrix.multiply(Matrix4f.createTranslateMatrix(getBendX(), getBendY(), getBendZ()));
        transformMatrix.multiply(axis.rotation(bendValue));
        transformMatrix.multiply(Matrix4f.createTranslateMatrix(-getBendX(), -getBendY(), -getBendZ()));

        Vector3f directionUnit; //some temporarily variable;

        Plane basePlane = getBasePlane();
        Plane otherPlane = getOtherSidePlane();

        directionUnit = this.getBendDirection().step();
        directionUnit.cross(axis);
        //parallel to the bend's axis and to the cube's bend direction
        Plane bendPlane = new Plane(directionUnit, new Vector3f(this.getBendX(), this.getBendY(), this.getBendZ()));
        float halfSize = bendHeight()/2;

        boolean bl = getBendDirection() == Direction.UP || getBendDirection() == Direction.SOUTH || getBendDirection() == Direction.EAST;

        posSupplier.iteratePositions(iPosWithOrigin -> {
            Vector3f newPos = iPosWithOrigin.getOriginalPos();
            float distFromBend = bl ? -bendPlane.distanceTo(newPos) : bendPlane.distanceTo(newPos);
            float distFromBase = basePlane.distanceTo(newPos);
            float distFromOther = otherPlane.distanceTo(newPos);
            double s = Math.tan(bendValue/2)*distFromBend;
            Vector3f x = getBendDirection().step();
            if(Math.abs(distFromBase) < Math.abs(distFromOther)){
                x.mul((float) (-distFromBase/halfSize*s));
                newPos.add(x);
                Vector4f reposVector = new Vector4f(newPos);
                reposVector.transform(transformMatrix);
                newPos = new Vector3f(reposVector.x(), reposVector.y(), reposVector.z());
            }
            else {
                x.mul((float) (-distFromOther/halfSize*s));
                newPos.add(x);
            }
            iPosWithOrigin.setPos(newPos);
        });

        return transformMatrix;
    }

    Direction getBendDirection();

    /**
     * center x
     * @return x
     */
    float getBendX();
    float getBendY();
    float getBendZ();
    Plane getBasePlane();
    Plane getOtherSidePlane();

    /**
     * There are more efficient ways to calculate it
     * Try to override it (If you have size)
     * @return the size of the cube
     */
    default float bendHeight(){
        return this.getBasePlane().distanceTo(this.getOtherSidePlane());
    }

    /**
     * A plane in the 3d space
     * form a vector and a position
     * for distance calculation
     */
    class Plane{
        final Vector3f normal;
        final float normDistance;

        public Plane(Vector3f normal, Vector3f position){
            this.normal = normal.copy();
            this.normal.normalize();
            this.normDistance = -normal.dot(position);
        }

        /**
         * This will return with the SIGNED distance
         * @param pos some pos
         * @return the distance between the pos and this plane
         */
        public float distanceTo(Vector3f pos){
            return normal.dot(pos) + normDistance;
        }

        /**
         * This will return with the SIGNED distance
         * @param otherPlane some plane
         * @return the distance between the two planes. 0 if not parallel
         */
        public float distanceTo(Plane otherPlane){
            Vector3f tmp = this.normal.copy();
            tmp.cross(otherPlane.normal);
            //if the lines are parallel
            if(tmp.dot(tmp) < 0.01){
                return this.normDistance + this.normal.dot(otherPlane.normal) * otherPlane.normDistance; //if the normals point to the opposite direction
            }
            else {
                return 0;
            }
        }
    }
}
