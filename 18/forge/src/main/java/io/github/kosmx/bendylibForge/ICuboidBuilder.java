package io.github.kosmx.bendylibForge;

import io.github.kosmx.bendylibForge.impl.ICuboid;

/**
 * Can be passed as a lambda, get a data, returns a cuboid
 */
@FunctionalInterface
public interface ICuboidBuilder<C extends ICuboid> {

    C build(Data data);

    class Data{
        /**
         * Size parameters
         */
        public float x, y, z, sizeX, sizeY, sizeZ;
        public float extraX, extraY, extraZ;
        public int u, v;
        public boolean mirror = false;
        public int textureWidth, textureHeight; //That will be int
        //public float bendX, bendY, bendZ;

        public Data(){}

        public Data(int u, int v, float x, float y, float z, float sizeX, float sizeY, float sizeZ, float extraX, float extraY, float extraZ, boolean mirror, float textureWidth, float textureHeight){
            this.u = u;
            this.v = v;
            this.x = x;
            this.y = y;
            this.z = z;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
            this.extraX = extraX;
            this.extraY = extraY;
            this.extraZ = extraZ;
            this.mirror = mirror;
            //Casting
            this.textureWidth = (int) textureWidth;
            this.textureHeight = (int) textureHeight;
        }

    }
}
