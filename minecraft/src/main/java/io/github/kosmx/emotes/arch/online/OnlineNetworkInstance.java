package io.github.kosmx.emotes.arch.online;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.EmotecraftModPlatform;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.library.EmoteLibrary;
import io.github.kosmx.emotes.arch.network.client.ClientNetwork;
import io.github.kosmx.emotes.arch.online.netty.HandshakeHandler;
import io.github.kosmx.emotes.arch.online.netty.NettyObjectFactory;
import io.github.kosmx.emotes.arch.online.netty.WebsocketHandler;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketBound;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.common.network.objects.SongPacket;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.IdleStateHandler;
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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Connection to the Online Emotes relay; binary frames start with the protocol version and the {@link FrameType}. */
public final class OnlineNetworkInstance extends BaseClientNetwork {
    private static final URI URI_ADDRESS = URI.create("wss://api.redlance.org:443/websockets/online-emotes");
    private static final int PAYLOAD_LENGTH = Integer.MAX_VALUE;
    private static final int RELAY_PAYLOAD_LENGTH = 2 * 1024 * 1024; // the largest frame the relay takes
    private static final long TOKEN_TIMEOUT = 5L; // seconds
    private static final long MAX_RECONNECT_DELAY = 300L; // seconds
    private static final long PING_INTERVAL = 30L; // seconds
    private static final long READ_TIMEOUT = 90L; // seconds

    private static final byte PROTOCOL_VERSION = 2;
    private static final StreamCodec<ByteBuf, List<UUID>> UUID_LIST = UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs.list());
    private static final Component CONNECTED = Component.translatable("emotecraft.online.connected");
    private static final Component DISCONNECTED = Component.translatable("emotecraft.online.disconnected");

    /** Set on a channel once its STATE went out: from then on the relay knows who sends what comes through it. */
    private static final AttributeKey<Boolean> ANNOUNCED = AttributeKey.valueOf("emotecraft_online_announced");

    public static final OnlineNetworkInstance INSTANCE = new OnlineNetworkInstance();

    // Created by the first connect(), which knows whether the native transport is enabled
    private volatile EventLoopGroup group;
    private Bootstrap bootstrap;

    // Client thread
    private final TrackedPlayers trackedPlayers = new TrackedPlayers();
    private final Map<UUID, UUID> relayedEmotes = new HashMap<>(); // player, emote the relay started

    private volatile boolean running;
    private volatile @Nullable Channel channel;

    // Event loop
    private @Nullable ScheduledFuture<?> retry;
    private long retryDelay; // seconds
    private boolean connecting;
    private @Nullable String refusedToken; // by the relay with 401, so the next attempt renews it
    private @Nullable SslContext sslContext; // reused, so TLS sessions resume
    private @Nullable List<String> referers;

    /** Stays connected until {@link #disconnect()}, reconnecting whenever the connection drops. Client thread. */
    public void connect() {
        Channel channel = this.channel;
        if (isAnnounced(channel)) {
            announce(channel); // the relay learns about the new login
            return;
        }

        EventLoopGroup group = this.group;
        if (group == null) {
            boolean nativeTransport = Minecraft.getInstance().options.useNativeTransport();
            group = NettyObjectFactory.newEventLoopGroup(nativeTransport);
            this.bootstrap = new Bootstrap().group(group).channel(NettyObjectFactory.getSocketChannel(nativeTransport));
            this.group = group;
        }

        this.running = true;
        group.execute(() -> { // right away, skipping a pending retry
            this.retryDelay = 0L;
            attempt();
        });
    }

    /** Any thread: Fabric calls it on the network thread. */
    @Override
    public void disconnect() {
        this.running = false;
        EventLoopGroup group = this.group;
        if (group != null) group.execute(this::cancelRetry);

        Channel channel = this.channel;
        this.channel = null;
        if (channel != null) {
            channel.writeAndFlush(new CloseWebSocketFrame()).addListener(ChannelFutureListener.CLOSE);
        }

        Minecraft.getInstance().execute(() -> {
            stopRelayedEmotes();
            super.disconnect();
        });
    }

    @Override
    public boolean isActive() {
        return isAnnounced(this.channel);
    }

    private static boolean isAnnounced(@Nullable Channel channel) {
        return channel != null && channel.isActive() && channel.hasAttr(ANNOUNCED);
    }

    private void attempt() {
        cancelRetry();
        if (!this.running || this.connecting || this.channel != null) return; // connected, or on the way there
        this.connecting = true;
        CommonData.LOGGER.info("Connecting to Online Emotes...");

        CompletableFuture<@Nullable String> token;
        try {
            token = this.refusedToken != null ? EmoteLibrary.renewAccountToken(this.refusedToken) : EmoteLibrary.getAccountToken();
        } catch (Throwable th) { // the relay lets clients in without one
            CommonData.LOGGER.warn("Failed to get the EmotecraftLibrary account token!", th);
            token = CompletableFuture.completedFuture(null);
        }
        this.refusedToken = null;

        // Connects without the token rather than waiting long for it
        token.completeOnTimeout(null, TOKEN_TIMEOUT, TimeUnit.SECONDS).whenCompleteAsync((value, _) -> open(value), this.group);
    }

    private void open(@Nullable String token) {
        if (!this.running) { // disconnected while signing in
            this.connecting = false;
            return;
        }

        try {
            if (this.sslContext == null && "wss".equals(URI_ADDRESS.getScheme())) this.sslContext = SslContextBuilder.forClient().build();
            SslContext sslContext = this.sslContext;
            HandshakeHandler handshake = new HandshakeHandler(WebSocketClientHandshakerFactory.newHandshaker(
                    URI_ADDRESS, WebSocketVersion.V13, null, true, createHeaders(token), PAYLOAD_LENGTH
            ));

            this.bootstrap.clone()
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            if (sslContext != null) {
                                pipeline.addLast(sslContext.newHandler(ch.alloc(), URI_ADDRESS.getHost(), URI_ADDRESS.getPort()));
                            }

                            pipeline.addLast("http-codec", new HttpClientCodec());
                            pipeline.addLast("aggregator", new HttpObjectAggregator(PAYLOAD_LENGTH));
                            pipeline.addLast("ws-compression", new WebSocketClientCompressionHandler(PAYLOAD_LENGTH));
                            pipeline.addLast("handshaker", handshake);
                            pipeline.addLast("idle", new IdleStateHandler(READ_TIMEOUT, PING_INTERVAL, 0L, TimeUnit.SECONDS));
                            pipeline.addLast("ws-handler", new WebsocketHandler(OnlineNetworkInstance.this));
                        }
                    })
                    .connect(URI_ADDRESS.getHost(), URI_ADDRESS.getPort())
                    // Back onto the event loop: a channel that failed to register completes on another thread
                    .addListener((ChannelFutureListener) future -> this.group.execute(() -> onConnected(future, handshake, token)));
        } catch (Throwable th) { // keep retrying
            CommonData.LOGGER.warn("Failed to connect to Online Emotes!", th);
            this.connecting = false;
            retryLater();
        }
    }

    private void onConnected(ChannelFuture future, HandshakeHandler handshake, @Nullable String token) {
        this.connecting = false;
        if (!future.isSuccess()) {
            CommonData.LOGGER.warn("Failed to connect to Online Emotes: {}", future.cause().toString());
            retryLater();
            return;
        }

        // Set first: disconnect() clears running, then takes the channel, so one of the two closes it
        Channel channel = future.channel();
        this.channel = channel;
        if (!this.running) {
            this.channel = null;
            channel.close();
            return;
        }

        channel.closeFuture().addListener(_ -> onClosed(channel));
        handshake.handshakeFuture().addListener(result -> {
            if (result.isSuccess()) {
                OnlineEmotes.debugToast(CONNECTED);
                Minecraft.getInstance().execute(() -> {
                    if (this.channel == channel) announce(channel);
                });
                return;
            }

            CommonData.LOGGER.warn("Online Emotes handshake failed: {}", result.cause().toString());
            if (token != null && isUnauthorized(result.cause())) this.refusedToken = token;
            channel.close();
        });
    }

    private void onClosed(Channel channel) {
        if (channel.hasAttr(ANNOUNCED)) OnlineEmotes.debugToast(DISCONNECTED);
        if (this.channel != channel) return; // disconnect() let it go
        this.channel = null;
        Minecraft.getInstance().execute(this::stopRelayedEmotes);
        retryLater(); // with backoff after a 401 too, as the new token may be refused as well
    }

    /** Doubles the delay after every failed attempt, from the configured one up to {@link #MAX_RECONNECT_DELAY}. */
    private void retryLater() {
        if (!this.running) return;

        this.retryDelay = this.retryDelay == 0L ? PlatformTools.getConfig().onlineReconnectDelay.get()
                : Math.min(this.retryDelay * 2, MAX_RECONNECT_DELAY);
        this.retry = this.group.schedule(this::attempt, this.retryDelay, TimeUnit.SECONDS);
    }

    private void cancelRetry() {
        if (this.retry != null) {
            this.retry.cancel(false);
            this.retry = null;
        }
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

        // The relay keeps the play state, so it gets the running emote, if emotes of this server go through it
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.isPlayingEmote() && ClientPacketManager.isProxyNeeded()) {
            EmotePlayer emote = player.emotecraft$getEmote();
            sendMessage(new EmotePacket.Builder()
                    .configureToStreamEmote(emote.getCurrentAnimationInstance())
                    .configureEmoteTick(emote.getAnimationTicks())
                    .setSizeLimit(maxDataSize(), false), true
            );
        }
    }

    @Override
    public void receiveMessage(EmotePacket packet) {
        if (!isActive()) return; // from a connection already let go

        NetData data = packet.data;
        if (data.purpose == PacketTask.CONFIG) { // the relay always answers like a legacy server, so without super's warning
            setVersions(data.versions);
            onConfigurationDone();
            return;
        }

        if (data.player != null && data.purpose == PacketTask.STREAM && data.emoteData != null) {
            this.relayedEmotes.put(data.player, data.emoteData.uuid());
        } else if (data.player != null && data.purpose == PacketTask.STOP) {
            this.relayedEmotes.remove(data.player);
        }
        super.receiveMessage(packet);
    }

    /** A lost connection brings no stops, so what it started stops here, unless the server streams emotes too. */
    private void stopRelayedEmotes() {
        if (!ClientNetwork.INSTANCE.isActive()) this.relayedEmotes.forEach((player, emote) -> {
            NetData stop = new EmotePacket.Builder().configureToSendStop(emote, player).data();
            stop.isForced = true; // no "not allowed" toast
            executeMessage(stop, this); // as if received, which also drops an emote waiting for its player to load
        });
        this.relayedEmotes.clear();
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
        EmotePacket packet = builder.build();
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

    private HttpHeaders createHeaders(@Nullable String token) {
        DefaultHttpHeaders headers = new DefaultHttpHeaders();

        // The relay needs its client in front; Online Emotes ships with Emotecraft now, so both carry its version
        String version = EmotecraftModPlatform.INSTANCE.getModVersion(CommonData.MOD_ID);
        headers.add(HttpHeaderNames.USER_AGENT, String.format("online_emotes/%s %s/%s Minecraft/%s",
                version, CommonData.MOD_NAME, version, SharedConstants.getProtocolVersion()
        ));

        if (this.referers == null) this.referers = findReferers();
        for (String referer : this.referers) {
            headers.add(HttpHeaderNames.REFERER, referer);
        }

        try { // Because LanguageManager is reloadable
            headers.add(HttpHeaderNames.ACCEPT_LANGUAGE, Minecraft.getInstance().getLanguageManager().getSelected());
        } catch (Throwable ignored) {}

        CommonData.LOGGER.debug("Online Emotes headers: {}", headers.entries()); // before the token, which must not be logged
        if (token != null) headers.add(HttpHeaderNames.AUTHORIZATION, "Bearer " + token);
        return headers;
    }

    /** Where the mod was downloaded from, URL-encoded, or the error that kept it from being known. */
    private static List<String> findReferers() {
        try {
            Path file = EmotecraftModPlatform.INSTANCE.getModFile(CommonData.MOD_ID);
            if (file == null) return List.of();
            return PlatformFileReferer.INSTANCE.getFileReferer(file).stream()
                    .map(referer -> URLEncoder.encode(referer, StandardCharsets.UTF_8))
                    .toList();
        } catch (Throwable th) {
            return List.of(URLEncoder.encode(th.toString(), StandardCharsets.UTF_8));
        }
    }

    @Override
    public boolean isTrackingPlayState() {
        return true; // the relay knows who tracks whom
    }

    @Override
    public int maxDataSize() {
        return RELAY_PAYLOAD_LENGTH - 2; // the protocol version and the frame type
    }

    @Override
    protected void onConfigurationDone() {
        this.group.execute(() -> this.retryDelay = 0L); // only now, as the relay may still refuse a client after the handshake
        OnlineEmotes.debugToast(Component.translatable("emotecraft.online.handshake_done",
                CommonComponents.optionStatus(ClientPacketManager.isInstanceOutdatedForStreaming(this))
        ));
    }
}
