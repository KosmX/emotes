package io.github.kosmx.emotes.hytale.asset;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.packets.setup.AssetFinalize;
import com.hypixel.hytale.protocol.packets.setup.AssetInitialize;
import com.hypixel.hytale.protocol.packets.setup.AssetPart;
import com.hypixel.hytale.protocol.packets.setup.RequestCommonAssetsRebuild;
import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.server.core.asset.common.CommonAssetModule;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.PlayerUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hands emote blobs to the clients that need them, and to no others.
 * <p>
 * The convenience registration call on {@code CommonAssetModule} pushes every asset to every connected player and
 * announces each one with a toast, which for a library of hundreds is neither affordable nor quiet. Registration and
 * delivery are separable — the registry call sends nothing — so this class does the sending, against the same audience
 * the animation itself reaches: the entity tracker already knows which players can see whom.
 * <p>
 * Nothing is ever sent twice to the same connection. The bookkeeping is weakly keyed, so a disconnect forgets what that
 * client held, which is correct — the next connection is a fresh client that may have kept nothing.
 */
public final class EmoteDelivery {
    /** An asset and the bytes behind it, held so a blob cannot be collected between resolving it and writing it. */
    public record Blob(CommonAsset asset, byte[] bytes) {
    }

    private final Map<PacketHandler, Set<String>> delivered = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Reads whatever is still on disk. Cached blobs are held weakly and re-read through the file behind them, so this
     * has to finish before anything touches a world thread.
     */
    public static CompletableFuture<List<Blob>> resolve(List<CommonAsset> assets) {
        List<CompletableFuture<Blob>> blobs = assets.stream()
                .map(asset -> asset.getBlob().thenApply(bytes -> new Blob(asset, bytes)))
                .toList();

        return CompletableFuture.allOf(blobs.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> blobs.stream().map(CompletableFuture::join).toList());
    }

    /**
     * Sends to every player that can see the entity, including the entity itself. That is the same set
     * {@code AnimationUtils} writes the animation packet to, so an emote and the clip it needs reach exactly the same
     * clients.
     */
    public void send(Ref<EntityStore> ref, ComponentAccessor<EntityStore> accessor, List<Blob> blobs) {
        PlayerUtil.forEachPlayerThatCanSeeEntity(ref, (viewerRef, viewer, componentAccessor) ->
                send(viewer.getPacketHandler(), blobs), accessor);
    }

    /** Sends whatever one connection has not been sent already. */
    public void send(PacketHandler handler, List<Blob> blobs) {
        Set<String> held = this.delivered.computeIfAbsent(handler, connection -> ConcurrentHashMap.newKeySet());

        List<ToClientPacket> packets = new ArrayList<>();
        for (Blob blob : blobs) {
            if (!held.add(blob.asset().getHash())) {
                continue;
            }

            packets.add(new AssetInitialize(blob.asset().toPacket(), blob.bytes().length));
            for (byte[] part : ArrayUtil.split(blob.bytes(), CommonAssetModule.MAX_FRAME)) {
                packets.add(new AssetPart(part));
            }
            packets.add(new AssetFinalize());
        }

        if (packets.isEmpty()) {
            return;
        }

        handler.write(packets.toArray(ToClientPacket[]::new));
        handler.writeNoCache(new RequestCommonAssetsRebuild());
    }

    /** Sends to one player, resolving off-thread first. */
    public void send(PlayerRef player, List<CommonAsset> assets) {
        PacketHandler handler = player.getPacketHandler();
        resolve(assets).thenAccept(blobs -> send(handler, blobs));
    }
}
