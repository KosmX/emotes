package com.kosmx.emotecraft.network;


import com.kosmx.emotecraft.EmotecraftCallbacks;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.mixinInterface.EmotePlayerInterface;
import com.kosmx.emotecraft.mixinInterface.IEmotecraftPresence;
import com.kosmx.emotecraft.model.EmotePlayer;
import com.kosmx.emotecraftCommon.EmoteData;
import com.kosmx.emotecraftCommon.network.DiscoveryPacket;
import com.kosmx.emotecraftCommon.network.EmotePacket;
import com.kosmx.emotecraftCommon.network.StopPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * the client-only network stuff
 */
public class ClientNetwork {

    public static void init(){
        ClientPlayNetworking.registerGlobalReceiver(MainNetwork.EMOTE_PLAY_NETWORK_PACKET_ID, (client, handler, packetByteBuf, rs)->{
            EmotePacket emotePacket;
            emotePacket = new EmotePacket();
            if(! emotePacket.read(packetByteBuf, Main.config.validThreshold) && Main.config.validateEmote) return;

            client.execute(()->{
                clientReceiveEmote(emotePacket);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(MainNetwork.EMOTE_STOP_NETWORK_PACKET_ID, (client, handler, packetByteBuf, rs)->{
            StopPacket stopPacket = new StopPacket();
            stopPacket.read(packetByteBuf);

            client.execute(()->{
                clientReceiveStop(stopPacket);
            });
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ClientPlayNetworking.registerReceiver(MainNetwork.EMOTECRAFT_DISCOVERY_PACKET_ID, (client1, handler1, buf, responseSender) -> {
            DiscoveryPacket packet = new DiscoveryPacket();
            packet.read(buf);
            client1.execute(()->{
                ((IEmotecraftPresence) handler1).setInstalledEmotecraft(packet.getVersion());
            });
        }));

        //Network discovery
        C2SPlayChannelEvents.REGISTER.register((handler, sender, client, channels) -> {
            if(channels.contains(MainNetwork.EMOTECRAFT_DISCOVERY_PACKET_ID)) {
                DiscoveryPacket packet = new DiscoveryPacket(MainNetwork.networkingVersion);
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                packet.write(buf);
                ClientPlayNetworking.send(MainNetwork.EMOTECRAFT_DISCOVERY_PACKET_ID, buf);
            }
            if(channels.contains(MainNetwork.EMOTE_PLAY_NETWORK_PACKET_ID) && ((IEmotecraftPresence)handler).getInstalledEmotecraft() == 0){
                ((IEmotecraftPresence)handler).setInstalledEmotecraft(2);
            }
        });
    }

    /**
     *
     * @param emote emote to play
     * @param player which player
     * @param emoteHolder If {@link EmoteHolder} is available
     * @return is success
     */
    public static boolean clientStartEmote(EmoteData emote, PlayerEntity player, @Nullable EmoteHolder emoteHolder) {
        boolean hasServerEmotecraftInstalled = MinecraftClient.getInstance().getNetworkHandler() != null && ((IEmotecraftPresence) MinecraftClient.getInstance().getNetworkHandler()).getInstalledEmotecraft() != 0;
        ActionResult result = EmotecraftCallbacks.startPlayClientEmote.invoker().playClientEmote(emote, player, emoteHolder, hasServerEmotecraftInstalled);
        if (result == ActionResult.FAIL) {
            return false;
        }
        sendEmotePacket(emote, player, false);
        EmotePlayerInterface target = (EmotePlayerInterface) player;
        target.playEmote(emote);
        //emote.start(player);
        return true;
    }

    public static void sendEmotePacket(EmoteData emote, PlayerEntity player, boolean isRepeating){
        try{
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            EmotePacket emotePacket = new EmotePacket(emote, player.getUuid());
            emotePacket.isRepeat = isRepeating;
            emotePacket.write(buf, ((IEmotecraftPresence)(MinecraftClient.getInstance().getNetworkHandler())).getInstalledEmotecraft());
            ClientPlayNetworking.send(MainNetwork.EMOTE_PLAY_NETWORK_PACKET_ID, buf);
        }
        catch (Exception e){
            Main.log(Level.ERROR, "cannot play emote reason: " + e.getMessage());
            if(Main.config.showDebug) e.printStackTrace();
        }
    }


    public static void clientReceiveEmote(EmotePacket emotePacket){
        PlayerEntity playerEntity = MinecraftClient.getInstance().world.getPlayerByUuid(emotePacket.getPlayer());
        boolean blocked = MinecraftClient.getInstance().shouldBlockMessages(emotePacket.getPlayer()) && Main.config.enablePlayerSafety;
        if(playerEntity != null){
            if(!emotePacket.isRepeat || ! EmotePlayer.isRunningEmote(((EmotePlayerInterface) playerEntity).getEmote())){
                ActionResult result = EmotecraftCallbacks.startPlayReceivedEmote.invoker().playReceivedEmote(emotePacket.getEmote(), playerEntity, blocked);
                if(result == ActionResult.FAIL || blocked){
                    return;
                }
                ((EmotePlayerInterface) playerEntity).playEmote(emotePacket.getEmote());
            }else{
                ((EmotePlayerInterface) playerEntity).resetLastUpdated();
            }
        }
    }
    public static void clientSendStop(){

        Objects.requireNonNull(((EmotePlayerInterface) Objects.requireNonNull(MinecraftClient.getInstance().getCameraEntity())).getEmote()).stop();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        StopPacket packet = new StopPacket(MinecraftClient.getInstance().getCameraEntity().getUuid());
        packet.write(buf);
        ClientPlayNetworking.send(MainNetwork.EMOTE_STOP_NETWORK_PACKET_ID, buf);
    }
    public static void clientReceiveStop(StopPacket stopPacket){
        EmotePlayerInterface player = (EmotePlayerInterface) MinecraftClient.getInstance().world.getPlayerByUuid(stopPacket.getPlayer());
        if(player != null && EmotePlayer.isRunningEmote(player.getEmote())) player.getEmote().stop();
        if(player == MinecraftClient.getInstance().player){
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new TranslatableText("emotecraft.blockedEmote"));
        }
    }

}
