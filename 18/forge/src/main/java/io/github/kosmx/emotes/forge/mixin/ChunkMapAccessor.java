package io.github.kosmx.emotes.forge.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/*
It is from Fabric API ...
 */
@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {

    @Accessor("entityMap")
    Int2ObjectMap<TrackedEntityAccessor> getTrackedEntity();
}
