package io.github.kosmx.emotes.arch.screen.utils;

import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class UnsafeRemotePlayer extends RemotePlayer {
    private final PlayerInfo playerInfo;

    public UnsafeRemotePlayer(@Nullable ClientLevel clientLevel, GameProfile gameProfile) {
        super(Objects.requireNonNullElse(clientLevel, UnsafeClientLevel.INSTANCE), gameProfile);
        this.playerInfo = new PlayerInfo(gameProfile, true);
    }

    @Override
    protected PlayerInfo getPlayerInfo() {
        return this.playerInfo;
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        return true;
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return true;
    }

    @Override
    public void initEmotePerspective(EmotePlayer emotePlayer) {
        // no-op
    }

    @Override
    public boolean touchingUnloadedChunk() {
        return true;
    }

    @Override
    public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> fluidTag, double motionScale) {
        return false;
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        return false;
    }

    @Override
    public void baseTick() {
        // no-op
    }

    @Override
    public void aiStep() {
        // no-op
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public @NotNull BlockState getInBlockState() {
        return Blocks.VOID_AIR.defaultBlockState();
    }
}
