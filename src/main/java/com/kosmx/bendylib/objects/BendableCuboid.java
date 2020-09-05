package com.kosmx.bendylib.objects;

import com.kosmx.bendylib.math.Matrix4;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Quaternion;

/**
 * Bendable cuboid literally...
 * If you don't know the math behind it
 * (Vectors, matrices, quaternions)
 * don't try to edit.
 */
public class BendableCuboid implements ICuboid {
    protected final Quad[] sides = new Quad[10];
    protected final Matrix4f matrix;
    protected Matrix4f lastPosMatrix;
    protected final RepositionableVertex.Pos3f[] positions = new RepositionableVertex.Pos3f[8];
    protected final Vector3f[] origins = new Vector3f[4];
    public final float minX;
    public final float minY;
    public final float minZ;
    public final float maxX;
    public final float maxY;
    public final float maxZ;
    protected final float size;
    protected final Vector3f moveVec;
    //to shift the matrix to the center axis
    protected float fixX;
    protected float fixY;
    protected float fixZ;


    public BendableCuboid(int u, int v, float x, float y, float z, float sizeX, float sizeY, float sizeZ, boolean mirror, float textureWidth, float textureHeight, Direction direction, float fixX, float fixY, float fixZ, float extraX, float extraY, float extraZ) {
        this.matrix = new Matrix4f();
        this.matrix.loadIdentity();
        this.fixX = fixX;
        this.fixY = fixY;
        this.fixZ = fixZ;
        this.matrix.multiply(direction.getRotationQuaternion()); //Rotate it?
        this.minX = x;
        this.minY = y;
        this.minZ = z;
        this.maxX = x + sizeX;
        this.maxY = y + sizeY;
        this.maxZ = z + sizeZ;
        this.size = direction.getAxis() == Direction.Axis.X ? sizeX + 2*extraX : direction.getAxis() == Direction.Axis.Y ? sizeY + 2*extraY: sizeZ + 2*extraZ;
        this.moveVec = new Vector3f(0, size/2, 0);
        float f = x + sizeX;
        float g = y + sizeY;
        float h = z + sizeZ;
        x -= extraX;
        y -= extraY;
        z -= extraZ;
        f += extraX;
        g += extraY;
        h += extraZ;
        if (mirror) {
            float i = f;
            f = x;
            x = i;
        }

        float j = (float)u;
        float k = (float)u + sizeZ;
        float l = (float)u + sizeZ + sizeX;
        float m = (float)u + sizeZ + sizeX + sizeX;
        float n = (float)u + sizeZ + sizeX + sizeZ;
        float o = (float)u + sizeZ + sizeX + sizeZ + sizeX;
        float p = (float)v;
        float q = (float)v + sizeZ;
        float r = (float)v + sizeZ + sizeY;

        //creating the origin vertices
        IVertex[] iVertices = new IVertex[4];
        for(int i = 0; i < positions.length; i++){
            positions[i] = new RepositionableVertex.Pos3f(new Vector3f());
        }

        //create repositionableVertices bo assign
        IVertex[] repVertices = new IVertex[8];
        for(int i = 0; i < repVertices.length; i++){
            repVertices[i] = new RepositionableVertex(0, 0, positions[i]);
        }

        //Every direction has a different net
        switch (direction){
            case UP:
                origins[0] = new Vector3f(x, y, z);
                origins[1] = new Vector3f(f, y, z);
                origins[2] = new Vector3f(f, y, h);
                origins[3] = new Vector3f(x, y, h);
                iVertices[0] = new Vertex(origins[0], 0, 0);
                iVertices[1] = new Vertex(origins[1], 0, 0);
                iVertices[2] = new Vertex(origins[2], 0, 0);
                iVertices[3] = new Vertex(origins[3], 0, 0);
                this.sides[0] = new Quad(new IVertex[]{iVertices[2], iVertices[3], iVertices[0], iVertices[1]}, k, p, l, q, textureWidth, textureHeight, mirror);
                this.sides[1] = new Quad(new IVertex[]{repVertices[5], repVertices[4], repVertices[7], repVertices[6]}, l, q, m, p, textureWidth, textureHeight, mirror);
                this.sides[2] = new Quad(new IVertex[]{iVertices[0], iVertices[3], repVertices[3], repVertices[0]}, j, q, k, (q + r)/2, textureWidth, textureHeight, mirror);
                this.sides[3] = new Quad(new IVertex[]{repVertices[0], repVertices[3], repVertices[7], repVertices[4]}, j, (q + r)/2, k, r, textureWidth, textureHeight, mirror);
                this.sides[4] = new Quad(new IVertex[]{iVertices[1], iVertices[0], repVertices[0], repVertices[1]}, k, q, l, (q + r)/2, textureWidth, textureHeight, mirror);
                this.sides[5] = new Quad(new IVertex[]{repVertices[1], repVertices[0], repVertices[4], repVertices[5]}, k, (q + r)/2, l, r, textureWidth, textureHeight, mirror);
                this.sides[6] = new Quad(new IVertex[]{iVertices[2], iVertices[1], repVertices[1], repVertices[2]}, l, q, n, (q + r)/2, textureWidth, textureHeight, mirror);
                this.sides[7] = new Quad(new IVertex[]{repVertices[2], repVertices[1], repVertices[5], repVertices[6]}, l, (q + r)/2, n, r, textureWidth, textureHeight, mirror);
                this.sides[8] = new Quad(new IVertex[]{iVertices[3], iVertices[2], repVertices[2], repVertices[3]}, n, q, o, (q + r)/2, textureWidth, textureHeight, mirror);
                this.sides[9] = new Quad(new IVertex[]{repVertices[3], repVertices[2], repVertices[6], repVertices[7]}, n, (q + r)/2, o, r, textureWidth, textureHeight, mirror);
                break;
            case DOWN:
                break;
            default:

        }
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.loadIdentity();
        setRotation(matrix4f);
        //setRotationDeg(-45, -100); //debug stuff
    }

    public Matrix4f setRotation(Matrix4f rotation){
        Matrix4f shift = this.getMatrix();
        Matrix4f length = Matrix4f.translate(this.moveVec.getX(), this.moveVec.getY(), this.moveVec.getZ());
        shift.multiply(length);
        shift.multiply(Matrix4f.translate(this.fixX, this.fixY, this.fixZ));
        shift.multiply(rotation);
        shift.multiply(Matrix4f.translate(-this.fixX, -this.fixY, -this.fixZ));
        Matrix4f midpoint = shift.copy();
        setMiddle(midpoint);
        setPoints(midpoint, 0);
        shift.multiply(Matrix4f.translate(this.fixX, this.fixY, this.fixZ));
        shift.multiply(rotation);
        shift.multiply(Matrix4f.translate(-this.fixX, -this.fixY, -this.fixZ));
        shift.multiply(length);
        setPoints(shift, 4);
        this.lastPosMatrix = shift; //Store it for later use
        return shift;
    }

    private void setMiddle(Matrix4f matrix){
        Vector4f moved = new Vector4f(this.moveVec.getX(), moveVec.getY(), moveVec.getZ(), 1);
        moved.transform(matrix);
        Vector3f cross = this.moveVec.copy();
        cross.normalize();
        Vector3f transformed = new Vector3f(moved.getX(), moved.getY(), moved.getZ());
        transformed.normalize();
        float cosh = 1/cross.dot(transformed); //cosh
        cross.cross(transformed);
        cross.normalize();
        //Matrix3f rotate = new Matrix3f(Vector3f.NEGATIVE_Y.getDegreesQuaternion(90));
        Vector3f rotated = cross.copy();
        rotated.transform(new Matrix3f(Vector3f.NEGATIVE_Y.getDegreesQuaternion(-90)));
        rotated.scale(cosh);
        Matrix4 transformation = new Matrix4();
        transformation.fromEigenVector(cross, new Vector3f(0, 1, 0), rotated);
        matrix.multiply(transformation);
    }

    private void setPoints(Matrix4f matrix, int n){
        for (int i = 0; i != 4; i++){
            Vector4f vector4f = new Vector4f(origins[i].getX(), origins[i].getY(), origins[i].getZ(), 1);
            vector4f.transform(matrix);
            positions[i + n].set(new Vector3f(vector4f.getX(), vector4f.getY(), vector4f.getZ()));
        }
    }

    public Matrix4f setRotation(Quaternion quaternion){
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.loadIdentity();
        matrix4f.multiply(quaternion);
        return setRotation(matrix4f);
    }

    public Matrix4f setRotationRad(float axisf, float value){
        value = value/2;
        Vector3f axis = new Vector3f((float) Math.cos(axisf), 0, (float) Math.sin(axisf));
        return this.setRotation(axis.getRadialQuaternion(value));
    }

    public void setRotationDeg(float axis, float val){
        this.setRotationRad(axis * 0.0174533f, val * 0.0174533f);
    }

    public Matrix4f getMatrix(){
        //The original Matrix4f should never change
        return this.matrix.copy();
    }

    @Override
    public void render(MatrixStack.Entry matrices, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int light, int overlay) {
        for(Quad quad:sides){
            quad.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        }
    }

    public Matrix4f getLastPosMatrix(){
        return this.lastPosMatrix.copy();
    }

    /*'//TODO remove me'*
     * A replica of {@link ModelPart.Quad}
     * with IVertex and render()
     */
    public static class Quad{
        public final IVertex[] vertices;

        public Quad(IVertex[] vertices, float u1, float v1, float u2, float v2, float squishU, float squishV, boolean flip){
            float f = 0/squishU;
            float g = 0/squishV;
            this.vertices = new IVertex[4];
            this.vertices[0] = vertices[0].remap(u2 / squishU - f, v1 / squishV + g);
            this.vertices[1] = vertices[1].remap(u1 / squishU + f, v1 / squishV + g);
            this.vertices[2] = vertices[2].remap(u1 / squishU + f, v2 / squishV - g);
            this.vertices[3] = vertices[3].remap(u2 / squishU - f, v2 / squishV - g);
            if(flip){
                int i = vertices.length;

                for(int j = 0; j < i / 2; ++j) {
                    IVertex vertex = vertices[j];
                    vertices[j] = vertices[i - 1 - j];
                    vertices[i - 1 - j] = vertex;
                }
            }
        }
        public void render(MatrixStack.Entry matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha){
            Vector3f direction = this.getDirection();
            direction.transform(matrices.getNormal());

            for (int i = 0; i != 4; ++i){
                IVertex vertex = this.vertices[i];
                Vector3f vertexPos = vertex.getPos();
                Vector4f pos = new Vector4f(vertexPos.getX()/16f, vertexPos.getY()/16f, vertexPos.getZ()/16f, 1);
                pos.transform(matrices.getModel());
                vertexConsumer.vertex(pos.getX(), pos.getY(), pos.getZ(), red, green, blue, alpha, vertex.getU(), vertex.getV(), overlay, light, direction.getX(), direction.getY(), direction.getZ());
            }
        }

        /**
         * calculate the normal vector from the vertices' coordinates with cross product
         * @return the normal vector (direction)
         */
        private Vector3f getDirection(){
            Vector3f buf = vertices[3].getPos().copy();
            buf.scale(-1);
            Vector3f vecB = vertices[1].getPos().copy();
            vecB.add(buf);
            buf = vertices[2].getPos().copy();
            buf.scale(-1);
            Vector3f vecA = vertices[0].getPos().copy();
            vecA.add(buf);
            vecA.cross(vecB);
            //Return the cross product, if it's zero then return anything non-zero to not cause crash...
            return vecA.normalize() ? vecA : Direction.NORTH.getUnitVector();
        }
    }
}
