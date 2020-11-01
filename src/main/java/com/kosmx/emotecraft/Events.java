package com.kosmx.emotecraft;

import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.mixinInterface.EmotePlayerInterface;
import com.kosmx.emotecraft.network.EmotePacket;
import com.kosmx.emotecraft.network.StopPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.Level;
import javax.annotation.Nullable;
import java.util.Objects;

public class Events {

    /**
     *
     * @param emote emote to play
     * @param player which player
     * @param emoteHolder If {@link EmoteHolder} is available
     * @return is success
     */
    public static boolean clientStartEmote(Emote emote, PlayerEntity player, @Nullable EmoteHolder emoteHolder){
        ActionResult result = EmotecraftCallbacks.startPlayClientEmote.invoker().playClientEmote(emote, player, emoteHolder);
        if(result == ActionResult.FAIL){
            return false;
        }
        try{
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            EmotePacket emotePacket = new EmotePacket(emote, player);
            emotePacket.write(buf);
            ClientSidePacketRegistry.INSTANCE.sendToServer(Main.EMOTE_PLAY_NETWORK_PACKET_ID, buf);
            EmotePlayerInterface target = (EmotePlayerInterface) player;
            target.playEmote(emote);
            emote.start(player);
        }catch(Exception e){
            Main.log(Level.ERROR, "cannot play emote reason: " + e.getMessage());
            if(Main.config.showDebug) e.printStackTrace();
        }
        return true;
    }
    static void clientReceiveEmote(EmotePacket emotePacket){
        PlayerEntity playerEntity = MinecraftClient.getInstance().world.getPlayerByUuid(emotePacket.getPlayer());
        if(playerEntity != null){
            if(!emotePacket.isRepeat || ! Emote.isRunningEmote(((EmotePlayerInterface) playerEntity).getEmote())){
                ActionResult result = EmotecraftCallbacks.startPlayReceivedEmote.invoker().playReceivedEmote(emotePacket.getEmote(), playerEntity);
                if(result == ActionResult.FAIL){
                    return;
                }
                ((EmotePlayerInterface) playerEntity).playEmote(emotePacket.getEmote());
                ((EmotePlayerInterface) playerEntity).getEmote().start(playerEntity);
            }else{
                ((EmotePlayerInterface) playerEntity).resetLastUpdated();
            }
        }
    }
    static void clientSendStop(){

        Objects.requireNonNull(((EmotePlayerInterface) Objects.requireNonNull(MinecraftClient.getInstance().getCameraEntity())).getEmote()).stop();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        StopPacket packet = new StopPacket((PlayerEntity) MinecraftClient.getInstance().getCameraEntity());
        packet.write(buf);
        ClientSidePacketRegistry.INSTANCE.sendToServer(Main.EMOTE_STOP_NETWORK_PACKET_ID, buf);
    }
    static void clientReceiveStop(StopPacket stopPacket){
        EmotePlayerInterface player = (EmotePlayerInterface) MinecraftClient.getInstance().world.getPlayerByUuid(stopPacket.getPlayer());
        if(player != null && Emote.isRunningEmote(player.getEmote())) player.getEmote().stop();
    }
}
