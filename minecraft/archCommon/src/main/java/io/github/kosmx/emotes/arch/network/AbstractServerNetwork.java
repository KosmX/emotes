package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmoteStreamHelper;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.HashMap;

public abstract class AbstractServerNetwork implements INetworkInstance {

    @NotNull
    protected final EmoteStreamHelper streamHelper = new ServerStreamHelper();


    @NotNull
    protected abstract Connection getServerConnection();

    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        return getServerConnection().emotecraft$getRemoteVersions();
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        getServerConnection().emotecraft$setVersions(map);
    }

    abstract void sendEmotePacket(ByteBuffer buffer);

    abstract void sendStreamPacket(ByteBuffer buffer);


    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public int getRemoteVersion() {
        return CommonData.networkingVersion;
    }

    @Override
    public boolean isServerTrackingPlayState() {
        return true; // MC server does track this
    }

    @Override
    public int maxDataSize() {
        return Short.MAX_VALUE - 16; // channel ID is 12, one extra int makes it 16 (string)
        // this way we have 3 byte error
    }

    public @Nullable ByteBuffer receiveStreamChunk(ByteBuffer buffer) {
        return streamHelper.receiveStream(buffer);
    }


    protected class ServerStreamHelper extends EmoteStreamHelper {

        @Override
        protected int getMaxPacketSize() {
            return maxDataSize();
        }

        @Override
        protected void sendPlayPacket(ByteBuffer buffer) {
            sendEmotePacket(buffer);
        }

        @Override
        protected void sendStreamChunk(ByteBuffer buffer) {
            sendStreamPacket(buffer);
        }
    }
}
