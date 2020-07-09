package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.playerInterface.ClientPlayerEmotes;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Inject;

import javax.annotation.Nullable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class ClientPlayerMixin extends PlayerEntity implements ClientPlayerEmotes {

    @Nullable
    private Emote emote;

    public ClientPlayerMixin(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    @Override
    public void playEmote(Emote emote) {
        this.emote = emote;
    }

    @Override
    @Nullable
    public Emote getEmote(){
        return this.emote;
    }

    @Override
    public void tick() {
        super.tick();
        if(Emote.isRunningEmote(emote)) emote.tick();
    }
}
