package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.network.StopPacket;
import com.kosmx.emotecraft.playerInterface.EmotePlayerInterface;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.io.IOException;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class EmotePlayerMixin extends PlayerEntity implements EmotePlayerInterface {

    @Nullable
    private Emote emote;

    public EmotePlayerMixin(World world, BlockPos blockPos, GameProfile gameProfile) {
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
        if(Emote.isRunningEmote(this.emote)){
            if(this != MinecraftClient.getInstance().getCameraEntity() || EmoteHolder.canRunEmote(this)) {
                this.emote.tick();
            }
            else {
                emote.stop();
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                StopPacket packet = new StopPacket(this);
                packet.write(buf);
                ClientSidePacketRegistry.INSTANCE.sendToServer(Main.EMOTE_STOP_NETWORK_PACKET_ID, buf);
            }
        }
    }
}
