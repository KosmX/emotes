package io.github.kosmx.emotes.hytale.library;

import com.hypixel.hytale.protocol.packets.connection.Connect;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PacketWatcher;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryClient;
import org.redlance.emotecraftlibrary.sdk.GameEmoteInfo;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Bridges the cloud emote library to Hytale players.
 * <p>
 * The SDK authorizes one <i>player</i> per client — {@code listLiked}/{@code search} return that account's own likes —
 * so every player gets their own {@link EmoteLibraryClient}. Authorization needs the player's session-service identity
 * token, which the server keeps private to the login-phase handshake handler; it is instead read straight off the
 * {@code Connect} packet, the only place a mod can see it.
 */
public final class EmoteLibrary implements AutoCloseable {
    private static final String BASE_URL = "https://emotes.redlance.org/";

    /** Every SDK call except the live stream blocks, and none of them may run on a world thread. */
    private static final Executor EXECUTOR = Executors.newCachedThreadPool(Thread.ofVirtual()
            .name("emotecraft-library-", 0)
            .factory()
    );

    /**
     * Identity tokens by connection. Weakly keyed so an entry dies with the connection that owns it — there is no
     * disconnect hook to clean up after, and holding a player's credential longer than their session would be careless.
     */
    private final Map<PacketHandler, String> tokens = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<UUID, EmoteLibraryClient> clients = new ConcurrentHashMap<>();

    private final String userAgent;
    private final PacketFilter watcher;

    public EmoteLibrary(String modVersion, String serverVersion) {
        this.userAgent = String.format("%s/%s (hytale; %s)", CommonData.MOD_NAME, modVersion, serverVersion);
        this.watcher = PacketAdapters.registerInbound((PacketWatcher) (handler, packet) -> {
            if (packet instanceof Connect connect && connect.identityToken != null) {
                this.tokens.put(handler, connect.identityToken);
            }
        });
    }

    /**
     * Runs a library call for one player on a background thread, authorizing that player's session on first use.
     *
     * @return the call's result, or a failed future if the player never presented an identity token
     */
    public <R> CompletableFuture<R> execute(PlayerRef player, Function<EmoteLibraryClient, R> request) {
        return CompletableFuture.supplyAsync(() -> request.apply(client(player)), EXECUTOR);
    }

    private EmoteLibraryClient client(PlayerRef player) {
        EmoteLibraryClient client = this.clients.computeIfAbsent(player.getUuid(),
                uuid -> new EmoteLibraryClient(BASE_URL, this.userAgent));

        // Sessions expire on an idle window, so re-authorizing is the normal path rather than an error case.
        if (!client.isAuthorized()) {
            String token = this.tokens.get(player.getPacketHandler());
            if (token == null) {
                throw new IllegalStateException("No identity token for " + player.getUsername());
            }
            client.authorizeHytale(token);
        }
        return client;
    }

    /**
     * Downloads a liked emote's body and parses it with the same reader the Minecraft client uses.
     * <p>
     * This is the call the library enforces a download quota on, so callers are expected to go through
     * {@code HytaleEmoteRegistry#publish}, which skips it entirely for an emote that is already published.
     */
    public CompletableFuture<Animation> download(PlayerRef player, UUID emoteId) {
        return execute(player, client -> client.getBinary(emoteId, EmotePacket.defaultVersions))
                .thenApply(EmoteLibrary::parse);
    }

    /** A page of the player's own liked emotes. */
    public CompletableFuture<List<GameEmoteInfo>> listLiked(PlayerRef player, int offset, int limit) {
        return execute(player, client -> client.listLiked(offset, limit).getData());
    }

    private static Animation parse(InputStream body) {
        return UniversalEmoteSerializer.readData(body, "emote.emotecraft").values().iterator().next();
    }

    @Override
    public void close() {
        PacketAdapters.deregisterInbound(this.watcher);
        this.clients.values().forEach(EmoteLibraryClient::close);
        this.clients.clear();
        this.tokens.clear();
    }
}
