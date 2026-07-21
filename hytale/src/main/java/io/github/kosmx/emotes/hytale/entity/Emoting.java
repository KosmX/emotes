package io.github.kosmx.emotes.hytale.entity;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.kosmx.emotes.hytale.asset.EmoteDelivery;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The emote an entity is performing right now, and the blobs a late arrival will need to see it.
 * <p>
 * Emotecraft's other backends keep the same thing per player: what is playing and since when, so that anyone who starts
 * tracking the performer mid-emote can be told about it. The blobs ride along because delivering them cannot wait on a
 * disk read once a viewer has already appeared — they are resolved when the emote starts and held for as long as it
 * lasts, which is also the only window in which anyone can need them.
 */
public final class Emoting implements Component<EntityStore> {
    private static ComponentType<EntityStore, Emoting> type;

    private String id;
    private List<EmoteDelivery.Blob> blobs = List.of();
    private long endsAt;

    public static ComponentType<EntityStore, Emoting> getComponentType() {
        return type;
    }

    /** Called once from the plugin, since a component type only exists after the store registry has been told about it. */
    public static void setComponentType(ComponentType<EntityStore, Emoting> componentType) {
        type = componentType;
    }

    public void start(String id, List<EmoteDelivery.Blob> blobs, long endsAt) {
        this.id = id;
        this.blobs = blobs;
        this.endsAt = endsAt;
    }

    /** Lets go of the blobs as well; holding a clip for an emote that has ended keeps it out of the weak cache. */
    public void stop() {
        this.id = null;
        this.blobs = List.of();
    }

    public String getId() {
        return this.id;
    }

    public List<EmoteDelivery.Blob> getBlobs() {
        return this.blobs;
    }

    public boolean hasEnded(long now) {
        return now >= this.endsAt;
    }

    @NotNull
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod") // matches how the core's own components clone
    public Component<EntityStore> clone() {
        Emoting copy = new Emoting();
        copy.start(this.id, this.blobs, this.endsAt);
        return copy;
    }
}
