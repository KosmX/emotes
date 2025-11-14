package io.github.kosmx.emotes.arch.screen.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class UnsafeMannequin extends ClientMannequin {
    public UnsafeMannequin(@Nullable ClientLevel clientLevel, GameProfile gameProfile) {
        super(Objects.requireNonNullElse(clientLevel, UnsafeClientLevel.INSTANCE), Minecraft.getInstance().playerSkinRenderCache());
        setProfile(ResolvableProfile.createResolved(gameProfile));
        setHideDescription(true);
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
    public void initEmotePerspective() {
        // no-op
    }

    @Override
    protected void updateSkin() {
        super.updateSkin();
        if (this.skinLookup != null) {
            this.skinLookup.thenAccept(playerSkin -> playerSkin.ifPresent(this::setSkin));
        }
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
        tickCount++;
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

    @Override
    public @NotNull Vec3 position() {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return super.position();
        return localPlayer.position();
    }
}
