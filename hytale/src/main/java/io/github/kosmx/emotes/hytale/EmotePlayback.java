package io.github.kosmx.emotes.hytale;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.packets.entities.PlayEmote;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.kosmx.emotes.hytale.asset.EmoteDelivery;
import io.github.kosmx.emotes.hytale.bake.BlockyAnimBaker;
import io.github.kosmx.emotes.hytale.entity.Emoting;

/**
 * Plays an Emotecraft emote, making sure everyone who will see it holds the clip first.
 * <p>
 * Two things start an emote: our own page, and the client's emote wheel — which lists every registered emote asset,
 * ours included, and asks the server to play one over {@link PlayEmote}. The stock handler for that packet would play
 * it against clients that may never have received the clip, so emotes of ours are taken over: the packet is cancelled,
 * the blobs go out to the audience, and only then is the animation played. Everything else, including Hytale's own
 * emotes, is left to the stock handler untouched.
 */
public final class EmotePlayback implements AutoCloseable {
    private final HytaleEmoteRegistry registry;
    private final EmoteDelivery delivery;
    private final PacketFilter filter;

    public EmotePlayback(HytaleEmoteRegistry registry, EmoteDelivery delivery) {
        this.registry = registry;
        this.delivery = delivery;
        this.filter = PacketAdapters.registerInbound((PlayerPacketFilter) (player, packet) -> {
            if (!(packet instanceof PlayEmote emote)) {
                return false;
            }

            if (emote.emoteId == null || emote.emoteId.isEmpty()) {
                // A cancelled emote. The core stops the animation itself, but only our own state knows that a late
                // arrival must no longer be shown it, so that is cleared alongside - and the packet still passes.
                stop(player);
                return false;
            }

            if (this.registry.published(emote.emoteId) == null) {
                return false;
            }

            play(player, emote.emoteId);
            return true; // ours, and already handled
        });
    }

    /**
     * @param id the Hytale asset id of a published emote
     */
    public void play(PlayerRef player, String id) {
        HytaleEmoteRegistry.Published emote = this.registry.published(id);
        if (emote == null) {
            return;
        }

        Ref<EntityStore> ref = player.getReference();
        if (ref == null) {
            return;
        }

        // Reading a cached clip off disk and driving the animation are both off-limits here - one blocks, the other
        // touches components - so the blobs are resolved first and the rest hops onto the world thread.
        Store<EntityStore> store = ref.getStore();
        EmoteDelivery.resolve(emote.assets()).thenAccept(blobs -> store.getExternalData().getWorld().execute(() -> {
            if (!ref.isValid()) {
                return;
            }

            this.delivery.send(ref, store, blobs);
            AnimationUtils.playAnimation(ref, AnimationSlot.Emote, null, id, true, store);

            // The packet above only reaches whoever is watching right now. This is what carries the emote to anyone who
            // shows up while it is still running: the core replays an entity's active animation to new viewers, and
            // EmoteTrackerSystem puts the clip in their hands first and clears both once the emote is over.
            active(ref, store).setPlayingAnimation(AnimationSlot.Emote, id);
            emoting(ref, store).start(id, blobs, System.currentTimeMillis() + BlockyAnimBaker.duration(emote.frames()));
        }));
    }

    /** Forgets whatever the player was performing, so nobody arriving later is shown it. */
    public void stop(PlayerRef player) {
        Ref<EntityStore> ref = player.getReference();
        if (ref == null) {
            return;
        }

        Store<EntityStore> store = ref.getStore();
        store.getExternalData().getWorld().execute(() -> {
            if (!ref.isValid()) {
                return;
            }

            Emoting emoting = store.getComponent(ref, Emoting.getComponentType());
            if (emoting == null || emoting.getId() == null) {
                return;
            }

            emoting.stop();
            ActiveAnimationComponent active = store.getComponent(ref, ActiveAnimationComponent.getComponentType());
            if (active != null) {
                active.setPlayingAnimation(AnimationSlot.Emote, null);
            }
        });
    }

    private static ActiveAnimationComponent active(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, ActiveAnimationComponent> type = ActiveAnimationComponent.getComponentType();
        ActiveAnimationComponent component = store.getComponent(ref, type);
        if (component == null) {
            // Players are not given one: the core only puts it on NPCs, whose animations it drives itself.
            component = new ActiveAnimationComponent();
            store.putComponent(ref, type, component);
        }
        return component;
    }

    private static Emoting emoting(Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, Emoting> type = Emoting.getComponentType();
        Emoting component = store.getComponent(ref, type);
        if (component == null) {
            component = new Emoting();
            store.putComponent(ref, type, component);
        }
        return component;
    }

    @Override
    public void close() {
        PacketAdapters.deregisterInbound(this.filter);
    }
}
