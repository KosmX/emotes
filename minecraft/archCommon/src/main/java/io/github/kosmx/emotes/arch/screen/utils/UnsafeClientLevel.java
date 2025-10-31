package io.github.kosmx.emotes.arch.screen.utils;

import com.google.gson.internal.UnsafeAllocator;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class UnsafeClientLevel extends ClientLevel implements LevelEntityGetter<Entity> {
    public static final UnsafeClientLevel INSTANCE;
    static {
        try {
            INSTANCE = UnsafeAllocator.INSTANCE.newInstance(UnsafeClientLevel.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * To avoid calling the constructor, we use unsafe
     */
    @SuppressWarnings("unused")
    private UnsafeClientLevel(ClientPacketListener connection, ClientLevelData levelData, ResourceKey<Level> dimension, Holder<DimensionType> dimensionTypeRegistration, int viewDistance, int serverSimulationDistance, LevelRenderer levelRenderer, boolean isDebug, long biomeZoomSeed, int seaLevel) {
        super(connection, levelData, dimension, dimensionTypeRegistration, viewDistance, serverSimulationDistance, levelRenderer, isDebug, biomeZoomSeed, seaLevel);
    }

    @Override
    public int getBrightness(LightLayer lightType, BlockPos blockPos) {
        return 0;
    }

    @Override
    public @NotNull Scoreboard getScoreboard() {
        return new Scoreboard();
    }

    @Override
    public boolean isOutsideBuildHeight(int y) {
        return true;
    }

    @Override
    public int getMinY() {
        return 1;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public boolean isRaining() {
        return false;
    }

    @Override
    protected @NotNull LevelEntityGetter<Entity> getEntities() {
        return this;
    }

    @Override
    public @Nullable Entity get(int id) {
        return null;
    }

    @Override
    public @Nullable Entity get(UUID uuid) {
        return null;
    }

    @Override
    public @NotNull Iterable<Entity> getAll() {
        return Collections.emptyList();
    }

    @Override
    public <U extends Entity> void get(EntityTypeTest<Entity, U> test, AbortableIterationConsumer<U> consumer) {
    }

    @Override
    public void get(AABB boundingBox, Consumer<Entity> consumer) {
    }

    @Override
    public <U extends Entity> void get(EntityTypeTest<Entity, U> test, AABB bounds, AbortableIterationConsumer<U> consumer) {
    }

    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus chunkStatus, boolean requireChunk) {
        return new EmptyLevelChunk(this, new ChunkPos(x, z), Holder.direct(null));
    }

    @Override
    public @NotNull BlockState getBlockState(BlockPos pos) {
        return Blocks.VOID_AIR.defaultBlockState();
    }

    @Override
    public long getGameTime() {
        return 0L;
    }

    @Override
    public long getDayTime() {
        return 0L;
    }

    private static final ClientLevelData CLIENT_LEVEL_DATA = new ClientLevelData(Difficulty.PEACEFUL, false, true);
    @Override
    public @NotNull ClientLevelData getLevelData() {
        return CLIENT_LEVEL_DATA;
    }

    @Override
    public @NotNull List<Entity> getPushableEntities(Entity entity, AABB boundingBox) {
        return Collections.emptyList();
    }

    private static final PalettedContainerFactory PALETTED_CONTAINER_FACTORY = new PalettedContainerFactory(
            Strategy.createForBlockStates(new IdMapper<>()), Blocks.AIR.defaultBlockState(), null,
            Strategy.createForBiomes(new IdMapper<>()), Holder.direct(null), null
    );
    @Override
    public @NotNull PalettedContainerFactory palettedContainerFactory() {
        return PALETTED_CONTAINER_FACTORY;
    }
}
