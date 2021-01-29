package com.kosmx.emotecraft.network;


import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.EmotecraftCallbacks;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.mixinInterface.EmotePlayerInterface;
import com.kosmx.emotecraft.mixinInterface.IEmotecraftPresence;
import com.kosmx.emotecraftCommon.network.DiscoveryPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.C2SPlayChannelEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * the client-only network stuff
 */
public class ClientNetwork {

    public static boolean versionWarn;

    public static void init(){
        ClientPlayNetworking.registerGlobalReceiver(MainNetwork.EMOTE_PLAY_NETWORK_PACKET_ID, (client, handler, packetByteBuf, rs)->{
            EmotePacket emotePacket;
            emotePacket = new EmotePacket();
            if(! emotePacket.read(packetByteBuf, false)) return;

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

        ClientPlayNetworking.registerGlobalReceiver(MainNetwork.EMOTECRAFT_DISCOVERY_PACKET_ID, (client, handler, packetByteBuf, rs)->{
            DiscoveryPacket discoveryPacket = new DiscoveryPacket();
            discoveryPacket.read(packetByteBuf);
            int ver = discoveryPacket.getVersion();
            boolean warn = ver != MainNetwork.networkingVersion;
            client.execute(()->{
                ((IEmotecraftPresence)handler).setInstalledEmotecraft(ver);
                versionWarn = warn;
            });

        });

        //Network discovery
        C2SPlayChannelEvents.REGISTER.register((handler, sender, client, channels) -> {
            if(channels.contains(MainNetwork.EMOTECRAFT_DISCOVERY_PACKET_ID)) {
                DiscoveryPacket packet = new DiscoveryPacket(MainNetwork.networkingVersion);
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                packet.write(buf);
                ClientPlayNetworking.send(MainNetwork.EMOTECRAFT_DISCOVERY_PACKET_ID, buf);
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
    public static boolean clientStartEmote(Emote emote, PlayerEntity player, @Nullable EmoteHolder emoteHolder) {
        boolean hasServerEmotecraftInstalled = MinecraftClient.getInstance().getNetworkHandler() != null && ((IEmotecraftPresence) MinecraftClient.getInstance().getNetworkHandler()).getInstalledEmotecraft() != 0;
        ActionResult result = EmotecraftCallbacks.startPlayClientEmote.invoker().playClientEmote(emote, player, emoteHolder, hasServerEmotecraftInstalled);
        if (result == ActionResult.FAIL) {
            return false;
        }
        sendEmotePacket(emote, player, false);
        EmotePlayerInterface target = (EmotePlayerInterface) player;
        target.playEmote(emote);
        emote.start(player);
        return true;
    }

    public static void sendEmotePacket(Emote emote, PlayerEntity player, boolean isRepeating){
        boolean test = ClientPlayNetworking.canSend(MainNetwork.EMOTE_PLAY_NETWORK_PACKET_ID);
        Main.log(Level.INFO, "canSend result:" + test);
        try{
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            EmotePacket emotePacket = new EmotePacket(emote, player);
            emotePacket.isRepeat = isRepeating;
            emotePacket.write(buf);
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
            if(!emotePacket.isRepeat || ! Emote.isRunningEmote(((EmotePlayerInterface) playerEntity).getEmote())){
                ActionResult result = EmotecraftCallbacks.startPlayReceivedEmote.invoker().playReceivedEmote(emotePacket.getEmote(), playerEntity, blocked);
                if(result == ActionResult.FAIL || blocked){
                    return;
                }
                ((EmotePlayerInterface) playerEntity).playEmote(emotePacket.getEmote());
                ((EmotePlayerInterface) playerEntity).getEmote().start(playerEntity);
            }else{
                ((EmotePlayerInterface) playerEntity).resetLastUpdated();
            }
        }
    }
    public static void clientSendStop(){

        Objects.requireNonNull(((EmotePlayerInterface) Objects.requireNonNull(MinecraftClient.getInstance().getCameraEntity())).getEmote()).stop();
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        StopPacket packet = new StopPacket((PlayerEntity) MinecraftClient.getInstance().getCameraEntity());
        packet.write(buf);
        ClientPlayNetworking.send(MainNetwork.EMOTE_STOP_NETWORK_PACKET_ID, buf);
    }
    public static void clientReceiveStop(StopPacket stopPacket){
        EmotePlayerInterface player = (EmotePlayerInterface) MinecraftClient.getInstance().world.getPlayerByUuid(stopPacket.getPlayer());
        if(player != null && Emote.isRunningEmote(player.getEmote())) player.getEmote().stop();
    }

}
