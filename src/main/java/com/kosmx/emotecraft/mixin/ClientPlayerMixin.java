package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.playerInterface.ClientPlayerEmotes;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class ClientPlayerMixin extends PlayerEntity implements ClientPlayerEmotes {

    @Nullable
    private Emote emote;

    public ClientPlayerMixin(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }


    @Override
    public boolean isPlayingEmote() {
        return this.emote != null;
    }

    @Override
    public void playEmote(Emote emote) {
        this.emote = emote;
    }

    @Override
    public Emote getEmote(){
        return this.emote;
    }
}
