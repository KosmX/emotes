package io.github.kosmx.emotes.api.proxy;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;

import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Implement this if you want to act as a proxy for EmoteX
 * This has most of the functions implemented as you might want, but you can override any.
 */
public abstract class AbstractNetworkInstance implements INetworkInstance {
    private final HashMap<Byte, Byte> versions = new HashMap<>(EmotePacket.defaultVersions);

    /**
     * If you want to send byte array
     * <p>
     * You can wrap bytes to Netty
     * {@code Unpooled.wrappedBuffer(bytes)}
     * or to Minecraft's PacketByteBuf (yarn mappings) / FriendlyByteBuf (official mappings)
     * {@code new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes))}
     *
     * @param packet bytes to send
     * @param target target to send message, if null, everyone in the view distance
     */
    public void sendMessage(EmotePacket packet, @Nullable UUID target) {
        // If code here were invoked, you have made a big mistake.
        throw new UnsupportedOperationException("You should have implemented send emote feature");
    }

    /**
     * Send not prepared message, if you want to modify the message before sending, override this.
     * You can call the super, but if you do, you'll need to override another.
     * <p>
     * For example, you want to manipulate the data, before sending,
     * override this, edit the builder, call its super then override {@link AbstractNetworkInstance#sendMessage(EmotePacket, UUID)}
     * to send the bytes data
     * <p>
     *
     * @param builder EmotePacket builder
     * @param target target to send message, if null, everyone in the view distance
     * @throws IOException throws IOException if packet writing has failed
     */
    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        this.sendMessage(builder.build(), target); // everything is happening on the heap, there won't be any memory leak
    }

    /**
     * Receive message, but you don't know who sent this
     * The bytes data has to contain the identity of the sender
     * {@link #trustReceivedPlayer()} should return true as you don't have your own identifier system as alternative
     * @param packet message
     */
    public void receiveMessage(EmotePacket packet) {
        this.receiveMessage(packet, null);
    }

    /**
     * When the network instance disconnects...
     */
    protected void disconnect() {
        EmotesProxyManager.disconnectInstance(this);
    }

    /**
     * Default client-side version config,
     * Please call super if you override it.
     * @param map version/config map
     */
    @Override
    public void setVersions(Map<Byte, Byte> map) {
        this.versions.clear();
        this.versions.putAll(map);
    }

    /**
     * see {@link INetworkInstance#getRemoteVersions()}
     * it is just a default implementation
     */
    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        return this.versions;
    }

    @Override
    public boolean isServerTrackingPlayState() {
        return this.versions.containsKey(PacketConfig.SERVER_TRACK_EMOTE_PLAY) &&
                this.versions.get(PacketConfig.SERVER_TRACK_EMOTE_PLAY) != 0;
    }

    @Override
    public void sendC2SConfig(Consumer<EmotePacket.Builder> consumer) {
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToConfigExchange();

        try {
            consumer.accept(packetBuilder);
        } catch (Exception e){
            CommonData.LOGGER.warn("Error while writing packet!", e);
        }
    }
}
