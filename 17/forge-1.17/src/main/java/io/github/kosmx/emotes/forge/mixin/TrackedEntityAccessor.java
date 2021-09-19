package io.github.kosmx.emotes.forge.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(targets = "net/minecraft/server/level/ChunkMap$TrackedEntity")
public interface TrackedEntityAccessor {
    @Accessor("seenBy")
    Set<ServerPlayer> getPlayersTracking();
}
