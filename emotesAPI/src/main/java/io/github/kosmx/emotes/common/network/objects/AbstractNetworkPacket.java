package io.github.kosmx.emotes.common.network.objects;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public abstract class AbstractNetworkPacket {


    public abstract byte getID();
    public abstract byte getVer();

    public byte getVer(HashMap<Byte, Byte> versions){
        if(!versions.containsKey(this.getID()))throw new IllegalArgumentException("Versions should contain it's id");
        return (byte) Math.min(this.getVer(), versions.get(this.getID()));
    }

    /**
     * Read byte buf to T type
     * @param byteBuffer ByteBuffer
     * @param config Reader config
     * @return success
     */
    public abstract boolean read(ByteBuffer byteBuffer, NetData config, int version) throws IOException;

    public abstract void write(ByteBuffer byteBuffer, NetData config) throws IOException;

    public abstract boolean doWrite(NetData config);

    protected boolean getBoolean(ByteBuffer byteBuffer){
        return byteBuffer.get() != 0;
    }

    protected void putBoolean(ByteBuffer byteBuffer, boolean bl){
       byteBuffer.put((byte) (bl ? 1 : 0));
    }

    /**
     * Estimated size to create buffers
     * @param config some input data
     * @return the packet's size (estimated)
     */
    public abstract int calculateSize(NetData config);
}
