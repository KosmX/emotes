package io.github.kosmx.emotes.arch.online;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.EmotecraftModPlatform;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.library.EmoteLibrary;
import io.github.kosmx.emotes.arch.online.netty.HandshakeHandler;
import io.github.kosmx.emotes.arch.online.netty.NettyObjectFactory;
import io.github.kosmx.emotes.arch.online.netty.WebsocketHandler;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketBound;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.objects.SongPacket;
import io.github.kosmx.emotes.main.network.BaseClientNetwork;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.ScheduledFuture;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.referer.PlatformFileReferer;

import javax.net.ssl.SSLException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The connection to the Online Emotes relay, which streams emotes between players whose server doesn't. The relay
 * routes an emote to the clients that track its player, so this one keeps the relay told of the players it tracks.
 * <p>
 * Protocol 2, client to relay: a binary frame starts with the protocol version and the {@link FrameType}.
 * The handshake carries the EmotecraftLibrary account token, if there is one, to verify the account by. It is an
 * optional level of trust: the relay lets clients in without it too.
 */
public final class OnlineNetworkInstance extends BaseClientNetwork {
    private static final URI URI_ADDRESS = URI.create("wss://api.redlance.org:443/websockets/online-emotes");
    private static final int PAYLOAD_LENGTH = Integer.MAX_VALUE;
    private static final long TOKEN_TIMEOUT = 5L; // seconds

    private static final byte PROTOCOL_VERSION = 2;
    private static final StreamCodec<ByteBuf, List<UUID>> UUID_LIST = UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list());

    /** Set on a channel once its STATE went out: from then on the relay knows who sends what comes through it. */
    private static final AttributeKey<Boolean> ANNOUNCED = AttributeKey.valueOf("emotecraft_online_announced");

    public static final OnlineNetworkInstance INSTANCE = new OnlineNetworkInstance();

    private final EventLoopGroup group = NettyObjectFactory.newEventLoopGroup();
    private final Bootstrap bootstrap = new Bootstrap()
            .group(this.group)
            .channel(NettyObjectFactory.getSocketChannel());
    private final TrackedPlayers trackedPlayers = new TrackedPlayers(); // client thread

    private volatile @Nullable ScheduledFuture<?> reconnecting;
    private volatile @Nullable Channel channel;

    // Only ever touched on the event loop
    private boolean connecting;
    private boolean tokenRejected;

    /** Stays connected until {@link #disconnect()}, reconnecting whenever the connection drops. Client thread. */
    public void connect() {
        Channel channel = this.channel;
        if (isAnnounced(channel)) {
            announce(channel); // the relay learns about the new login
            return;
        }

        stopReconnecting(); // and start over, to try right away
        this.reconnecting = this.group.scheduleWithFixedDelay(this::reconnect, 0L,
                PlatformTools.getConfig().onlineReconnectDelay.get(), TimeUnit.SECONDS
        );
    }

    @Override
    public void disconnect() {
        stopReconnecting();

        Channel channel = this.channel;
        this.channel = null;
        if (channel != null) {
            channel.writeAndFlush(new CloseWebSocketFrame()).addListener(ChannelFutureListener.CLOSE);
        }

        super.disconnect();
    }

    private void stopReconnecting() {
        ScheduledFuture<?> reconnecting = this.reconnecting;
        this.reconnecting = null;
        if (reconnecting != null) reconnecting.cancel(false);
    }

    @Override
    public boolean isActive() {
        return isAnnounced(this.channel);
    }

    private static boolean isAnnounced(@Nullable Channel channel) {
        return channel != null && channel.isActive() && channel.hasAttr(ANNOUNCED);
    }

    private void reconnect() {
        if (this.connecting || this.channel != null) return; // connected, or on the way there
        this.connecting = true;
        CommonData.LOGGER.info("Connecting to Online Emotes...");

        // The token only adds trust, so the relay never waits long for it, nor goes unjoined over it
        CompletableFuture<@Nullable String> token;
        if (this.tokenRejected) { // the relay refused it: join without one, and have a fresh one for the next time
            this.tokenRejected = false;
            EmoteLibrary.getAccountToken(true);
            token = CompletableFuture.completedFuture(null);
        } else {
            token = EmoteLibrary.getAccountToken(false).completeOnTimeout(null, TOKEN_TIMEOUT, TimeUnit.SECONDS);
        }
        token.whenCompleteAsync((value, _) -> open(value), this.group);
    }

    private void open(@Nullable String token) {
        if (this.reconnecting == null) { // disconnected while signing in
            this.connecting = false;
            return;
        }

        HandshakeHandler handshake = new HandshakeHandler(WebSocketClientHandshakerFactory.newHandshaker(
                URI_ADDRESS, WebSocketVersion.V13, null, true, createHeaders(token), PAYLOAD_LENGTH
        ));

        this.bootstrap.clone()
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws SSLException {
                        ChannelPipeline pipeline = ch.pipeline();

                        if ("wss".equals(URI_ADDRESS.getScheme())) {
                            pipeline.addLast(SslContextBuilder.forClient().build()
                                    .newHandler(ch.alloc(), URI_ADDRESS.getHost(), URI_ADDRESS.getPort())
                            );
                        }

                        pipeline.addLast("http-codec", new HttpClientCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(PAYLOAD_LENGTH));
                        pipeline.addLast("ws-compression", new WebSocketClientCompressionHandler(PAYLOAD_LENGTH));
                        pipeline.addLast("handshaker", handshake);
                        pipeline.addLast("ws-handler", new WebsocketHandler(OnlineNetworkInstance.this));
                    }
                })
                .connect(URI_ADDRESS.getHost(), URI_ADDRESS.getPort())
                .addListener((ChannelFutureListener) future -> onConnected(future, handshake, token));
    }

    private void onConnected(ChannelFuture future, HandshakeHandler handshake, @Nullable String token) {
        this.connecting = false;
        if (!future.isSuccess()) {
            CommonData.LOGGER.warn("Failed to connect to Online Emotes!", future.cause()); // the next attempt comes as scheduled
            return;
        }

        Channel channel = future.channel();
        if (this.reconnecting == null) { // disconnected meanwhile
            channel.close();
            return;
        }

        this.channel = channel;
        channel.closeFuture().addListener(_ -> {
            if (this.channel == channel) this.channel = null; // for the next attempt to reconnect
        });

        handshake.handshakeFuture().addListener(result -> {
            if (result.isSuccess()) {
                Minecraft.getInstance().execute(() -> {
                    if (this.channel == channel) announce(channel);
                });
                return;
            }

            CommonData.LOGGER.warn("Online Emotes handshake failed!", result.cause());
            if (token != null && isUnauthorized(result.cause())) this.tokenRejected = true;
            channel.close();
        });
    }

    private static boolean isUnauthorized(Throwable cause) {
        return cause instanceof WebSocketClientHandshakeException exception && exception.response() != null
                && exception.response().status().code() == HttpResponseStatus.UNAUTHORIZED.code();
    }

    /** Tells the relay who this client is and what it tracks, then exchanges versions with it as with a server. Client thread. */
    private void announce(Channel channel) {
        sendState(channel);
        writeEmote(channel, createConfigurationPacket(true));
        channel.attr(ANNOUNCED).set(true);
    }

    private void sendState(Channel channel) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        GameProfile profile = player != null ? player.getGameProfile() : minecraft.getGameProfile();
        UUID worldId = player != null ? player.getUUID() : null;
        String serverAddress = player != null ? getServerAddress(player.connection.getConnection()) : null;
        List<UUID> trackedPlayers = this.trackedPlayers.reset(minecraft.level);

        write(channel, FrameType.STATE, buf -> {
            ByteBufCodecs.GAME_PROFILE.encode(buf, profile);
            FriendlyByteBuf.writeNullable(buf, worldId, UUIDUtil.STREAM_CODEC);
            FriendlyByteBuf.writeNullable(buf, serverAddress, ByteBufCodecs.STRING_UTF8);
            UUID_LIST.encode(buf, trackedPlayers);
        });
    }

    /** The address as protocol 1 sends it, since relay clients still on it are matched by that alone. */
    private static @Nullable String getServerAddress(Connection connection) {
        if (connection.isMemoryConnection()) return null; // this client hosts the world

        SocketAddress address = connection.getRemoteAddress();
        if (address instanceof InetSocketAddress inetSocketAddress) {
            return inetSocketAddress.getAddress().getHostAddress();
        }
        return address.toString();
    }

    /** The client level started tracking an entity. Client thread. */
    public void onTrackingStart(Entity entity) {
        if (isActive()) this.trackedPlayers.start(entity); // until then, announcing sends the whole list
    }

    /** The client level stopped tracking an entity. Client thread. */
    public void onTrackingEnd(Entity entity) {
        if (isActive()) this.trackedPlayers.end(entity);
    }

    /** Keeps the relay told of the players this client tracks. Client thread, every tick. */
    public void tick(Minecraft minecraft) {
        Channel channel = this.channel;
        if (!isAnnounced(channel)) return;

        if (this.trackedPlayers.isStale(minecraft.level)) {
            sendState(channel); // switching levels unloads nothing, so the new one goes whole
            return;
        }

        TrackedPlayers.Changes changes = this.trackedPlayers.flush();
        if (changes != null) write(channel, FrameType.TRACK, buf -> {
            UUID_LIST.encode(buf, changes.added());
            UUID_LIST.encode(buf, changes.removed());
        });
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, boolean updateVersions) {
        Channel channel = this.channel;
        if (!isAnnounced(channel)) {
            CommonData.LOGGER.error("Can't send packet to an inactive channel!");
            return;
        }
        if (updateVersions) builder.setVersion(getVersions());

        EmotePacket packet = writeEmote(channel, builder);
        if (packet.data.emoteData != null && packet.data.skippedPackets.contains(PacketConfig.NBS_CONFIG)
                && (packet.data.emoteData.data().has(SongPacket.OPUS_KEY) || packet.data.emoteData.data().has(SongPacket.NBS_KEY))) {
            OnlineEmotes.toast(Component.translatable("emotecraft.song_too_big_to_send"));
        }
    }

    private static EmotePacket writeEmote(Channel channel, EmotePacket.Builder builder) {
        EmotePacket packet = builder.setSizeLimit(PAYLOAD_LENGTH, false).build();
        write(channel, FrameType.EMOTE, buf -> packet.write(buf, PacketBound.SERVER));
        return packet;
    }

    private static void write(Channel channel, FrameType type, Consumer<ByteBuf> body) {
        ByteBuf buf = channel.alloc().buffer();
        try {
            buf.writeByte(PROTOCOL_VERSION);
            buf.writeByte(type.id);
            body.accept(buf);
        } catch (Throwable th) {
            buf.release();
            throw th;
        }
        channel.writeAndFlush(new BinaryWebSocketFrame(buf), channel.voidPromise());
    }

    private static HttpHeaders createHeaders(@Nullable String token) {
        DefaultHttpHeaders headers = new DefaultHttpHeaders();

        // The relay needs its client in front; Online Emotes ships with Emotecraft now, so both carry its version
        String version = EmotecraftModPlatform.INSTANCE.getModVersion(CommonData.MOD_ID);
        headers.add(HttpHeaderNames.USER_AGENT, String.format("online_emotes/%s %s/%s Minecraft/%s",
                version, CommonData.MOD_NAME, version, SharedConstants.getProtocolVersion()
        ));

        try {
            for (String referer : PlatformFileReferer.INSTANCE.getFileReferer(EmotecraftModPlatform.INSTANCE.getModFile(CommonData.MOD_ID))) {
                headers.add(HttpHeaderNames.REFERER, URLEncoder.encode(referer, StandardCharsets.UTF_8));
            }
        } catch (Throwable th) {
            headers.add(HttpHeaderNames.REFERER, URLEncoder.encode(th.toString(), StandardCharsets.UTF_8));
        }

        try { // Because LanguageManager is reloadable
            headers.add(HttpHeaderNames.ACCEPT_LANGUAGE, Minecraft.getInstance().getLanguageManager().getSelected());
        } catch (Throwable ignored) {}

        CommonData.LOGGER.info("Online Emotes headers: {}", headers.entries()); // before the token, which logs mustn't hold
        if (token != null) headers.add(HttpHeaderNames.AUTHORIZATION, "Bearer " + token);
        return headers;
    }

    @Override
    public boolean isTrackingPlayState() {
        return true; // the relay knows who tracks whom
    }

    @Override
    protected void onConfigurationDone() {
        if (PlatformTools.getConfig().onlineDebug.get()) OnlineEmotes.toast(Component.translatable("emotecraft.online.handshake_done",
                CommonComponents.optionStatus(ClientPacketManager.isInstanceOutdatedForStreaming(this))
        ));
    }
}
