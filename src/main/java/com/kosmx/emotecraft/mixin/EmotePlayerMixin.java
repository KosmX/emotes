package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.mixinInterface.EmotePlayerInterface;
import com.kosmx.emotecraft.network.ClientNetwork;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayerEntity.class)
public abstract class EmotePlayerMixin extends PlayerEntity implements EmotePlayerInterface {

    @Nullable
    private Emote emote;

    private int lastUpdated;

    public EmotePlayerMixin(World world, BlockPos pos, float yaw, GameProfile profile){
        super(world, pos, yaw, profile);
    }

    @Override
    public void playEmote(Emote emote){
        this.emote = emote;
    }

    @Override
    @Nullable
    public Emote getEmote(){
        return this.emote;
    }

    @Override
    public void resetLastUpdated(){
        this.lastUpdated = 0;
    }

    @Override
    public void tick(){
        super.tick();
        if(Emote.isRunningEmote(this.emote)){
            this.bodyYaw = (this.bodyYaw * 3 + this.yaw) / 4; //to set the body to the correct direction smooth.
            //if not the clientPlayer playing this emote (not the camera or the camera is in someone else) OR I can play that emote
            if(this != MinecraftClient.getInstance().getCameraEntity() && MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity || EmoteHolder.canRunEmote(this)){
                this.emote.tick();
                this.lastUpdated++;
                if(this == MinecraftClient.getInstance().getCameraEntity() && MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity && lastUpdated >= 100){
                    if(emote.getStopTick() - emote.getCurrentTick() < 50 && ! emote.isInfinite()) return;
                    ClientNetwork.sendEmotePacket(emote, this, true);
                    lastUpdated = 0;
                }else if((this != MinecraftClient.getInstance().getCameraEntity() || MinecraftClient.getInstance().getCameraEntity() instanceof OtherClientPlayerEntity) && lastUpdated > 300){
                    this.emote.stop();
                }
            }else{
                emote.stop();
                ClientNetwork.clientSendStop();
            }
        }
    }
}
