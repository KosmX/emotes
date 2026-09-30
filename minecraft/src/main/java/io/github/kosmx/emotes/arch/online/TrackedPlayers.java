package io.github.kosmx.emotes.arch.online;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Changes of the other players this client tracks since the relay was last told. Client thread. */
final class TrackedPlayers {
    private final Set<UUID> added = new LinkedHashSet<>();
    private final Set<UUID> removed = new LinkedHashSet<>();
    private WeakReference<ClientLevel> level = new WeakReference<>(null); // only compared, so it must not keep the level

    void start(Entity entity) {
        if (isOtherPlayer(entity) && !this.removed.remove(entity.getUUID())) this.added.add(entity.getUUID());
    }

    void end(Entity entity) {
        if (isOtherPlayer(entity) && !this.added.remove(entity.getUUID())) this.removed.add(entity.getUUID());
    }

    /** @return whether the relay was last sent another level */
    boolean isStale(@Nullable ClientLevel level) {
        return this.level.get() != level;
    }

    /** @return every other player {@code level} tracks, which the relay gets instead of the changes */
    List<UUID> reset(@Nullable ClientLevel level) {
        this.level = new WeakReference<>(level);
        this.added.clear();
        this.removed.clear();

        if (level == null) return List.of();
        return level.players().stream().filter(TrackedPlayers::isOtherPlayer).map(Entity::getUUID).toList();
    }

    /** @return the changes since the relay was last told, {@code null} if there are none */
    @Nullable Changes flush() {
        if (this.added.isEmpty() && this.removed.isEmpty()) return null;

        Changes changes = new Changes(List.copyOf(this.added), List.copyOf(this.removed));
        this.added.clear();
        this.removed.clear();
        return changes;
    }

    private static boolean isOtherPlayer(Entity entity) {
        return entity instanceof AbstractClientPlayer player && !player.isLocalPlayer();
    }

    record Changes(List<UUID> added, List<UUID> removed) {}
}
