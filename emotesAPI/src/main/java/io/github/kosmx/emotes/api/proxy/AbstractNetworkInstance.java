package io.github.kosmx.emotes.api.proxy;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Implement this if you want to act as a proxy for EmoteX
 * This has most of the functions implemented as you might want, but you can override any.
 */
public abstract class AbstractNetworkInstance implements INetworkInstance{

    //Notable version parameters
    protected int remoteVersion = 0;
    protected boolean disableNBS = false;
    protected boolean doesServerTrackEmotePlay = false;

    protected int animationFormat = 1;

    /*
     * You have to implement at least one of these three functions
     * EmoteX packet (PacketBuilder) -> ByteBuffer -> byte[]
     */

    /**
     * If you want to send byte array
     * <p>
     * You can wrap bytes to Netty
     * {@code Unpooled.wrappedBuffer(bytes)}
     * or to Minecraft's PacketByteBuf (yarn mappings) / FriendlyByteBuf (official mappings)
     * {@code new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes))}
     *
     * @param bytes bytes to send
     * @param target target to send message, if null, everyone in the view distance
     */
    protected void sendMessage(byte[] bytes, @Nullable UUID target){
        //If code here were invoked, you have made a big mistake.
        throw new UnsupportedOperationException("You should have implemented send emote feature");
    }

    /**
     * Send a ByteBuffer
     * @param byteBuffer buffer to send
     * @param target target to send message, if null, everyone in the view distance
     */
    protected void sendMessage(ByteBuffer byteBuffer, @Nullable UUID target){
        sendMessage(safeGetBytesFromBuffer(byteBuffer), target);
    }

    /**
     * Send not prepared message, if you want to modify the message before sending, override this.
     * You can call the super, but if you do, you'll need to override another.
     * <p>
     * For example, you want to manipulate the data, before sending,
     * override this, edit the builder, call its super then override {@link AbstractNetworkInstance#sendMessage(byte[], UUID)}
     * to send the bytes data
     * <p>
     *
     * @param builder EmotePacket builder
     * @param target target to send message, if null, everyone in the view distance
     * @throws IOException throws IOException if packet writing has failed
     */
    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        this.sendMessage(builder.build().write(), target);    //everything is happening on the heap, there won't be any memory leak
    }

    /**
     * Receive message, but you don't know who sent this
     * The bytes data has to contain the identity of the sender
     * {@link #trustReceivedPlayer()} should return true as you don't have your own identifier system as alternative
     * @param bytes message
     */
    public void receiveMessage(byte[] bytes){
        this.receiveMessage(bytes, null);
    }

    /**
     * Receive message with or without the sender's identity
     * <p>
     * You can convert Netty ByteBuf (or Minecraft's packet buffer) to bytes[] with this snippet
     * <pre>
     *      if(byteBuf.isDirect() || byteBuf.isReadOnly()){
     *          byte[] bytes = new byte[byteBuf.readableBytes()];
     *          byteBuf.getBytes(byteBuf.readerIndex(), bytes);
     *          return bytes;
     *      }
     *      else {
     *          return byteBuf.array();
     *      }
     * </pre>
     *
     * @param bytes message
     * @param player the sender player, null if unknown
     */
    public void receiveMessage(byte[] bytes, UUID player){
        this.receiveMessage(ByteBuffer.wrap(bytes), player);
    }

    /**
     * When the network instance disconnects...
     */
    protected void disconnect(){
        EmotesProxyManager.disconnectInstance(this);
    }

    /**
     * If {@link ByteBuffer} is wrapped, it is safe to get the array
     * but if is direct manual read is required.
     * @param byteBuffer get the bytes from
     * @return the byte array
     */
    public static byte[] safeGetBytesFromBuffer(ByteBuffer byteBuffer){
        return INetworkInstance.safeGetBytesFromBuffer(byteBuffer);
    }

    /**
     * Returns its own version number, as the modern networking (mostly) understand higher version messages.
     * @return CommonData.networkingVersion;
     */
    @Override
    public int getRemoteVersion() {
        return CommonData.networkingVersion;
    }


    /**
     * Default client-side version config,
     * Please call super if you override it.
     * @param map version/config map
     */
    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        if (map.containsKey((byte) 3)) {
            disableNBS = map.get((byte) 3) == 0;
        }
        if (map.containsKey((byte) 8)) {
            remoteVersion = map.get((byte) 8); //8x8 :D
        }
        if (map.containsKey(PacketConfig.SERVER_TRACK_EMOTE_PLAY)) {
            this.doesServerTrackEmotePlay = map.get(PacketConfig.SERVER_TRACK_EMOTE_PLAY) != 0;
        }
        if (map.containsKey((byte) 0)) {
            animationFormat = map.get((byte) 0);
        }
    }

    /**
     * see {@link INetworkInstance#getRemoteVersions()}
     * it is just a default implementation
     */
    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        HashMap<Byte, Byte> map = new HashMap<>();
        if(disableNBS){
            map.put((byte)3, (byte) 0);
        }
        if (doesServerTrackEmotePlay) {
            map.put(PacketConfig.SERVER_TRACK_EMOTE_PLAY, (byte)1);
        }
        map.put((byte)0, (byte)this.animationFormat);
        return map;
    }

    @Override
    public boolean isServerTrackingPlayState() {
        return this.doesServerTrackEmotePlay;
    }

    @Override
    public void sendConfigCallback(){
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        //packetBuilder.setVersion(this.getVersions());
        packetBuilder.configureToConfigExchange(true);

        try {
            this.sendMessage(packetBuilder, null);
        }
        catch (Exception e){
            EmotesProxyManager.log(Level.WARNING, "Error while writing packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int maxDataSize() {
        return Short.MAX_VALUE;
    }
}
