package com.kosmx.emotecraft.network;


import com.kosmx.emotecraftCommon.CommonData;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.mixinInterface.IEmotecraftPresence;
import com.kosmx.emotecraftCommon.network.DiscoveryPacket;
import com.kosmx.emotecraftCommon.network.EmotePacket;
import com.kosmx.emotecraftCommon.network.StopPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;



/**
 * Emotecraft core package for networking
 * //ALWAYS// send packages with these methods. (or with the client methods)
 * This package will run on the server (including local server)
 */
public class MainNetwork {

    public static final int networkingVersion = CommonData.networkingVersion;

    public static final Identifier EMOTE_PLAY_NETWORK_PACKET_ID = new Identifier(Main.MOD_ID, CommonData.playEmoteID);
    public static final Identifier EMOTE_STOP_NETWORK_PACKET_ID = new Identifier(Main.MOD_ID, CommonData.stopEmoteID);
    public static final Identifier EMOTECRAFT_DISCOVERY_PACKET_ID = new Identifier(Main.MOD_ID, CommonData.discoverEmoteID);
    /**
     * packet initializer, both for server and client side
     */
    public static void init(){
        ServerPlayNetworking.registerGlobalReceiver(EMOTE_PLAY_NETWORK_PACKET_ID, (server, player, handler, packetByteBuf, responseSender)->{
            EmotePacket packet = new EmotePacket();
            //PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            if(! packet.read(packetByteBuf, Main.config.validThreshold) && Main.config.validateEmote){
                Main.log(Level.INFO, player.getEntityName() + " is trying to play invalid emote", true);
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                StopPacket stopPacket = new StopPacket(player.getUuid());
                stopPacket.write(buf);
                handler.sendPacket(ServerPlayNetworking.createS2CPacket(EMOTE_STOP_NETWORK_PACKET_ID, buf));
                return;
            }
            //packet.write(buf);
            packet.setPlayer(player.getUuid());

            for(ServerPlayerEntity otherPlayer : PlayerLookup.tracking(player)){
                if(otherPlayer != player && ((IEmotecraftPresence)otherPlayer.networkHandler).getInstalledEmotecraft() != 0){
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    packet.write(buf, ((IEmotecraftPresence)(otherPlayer.networkHandler)).getInstalledEmotecraft());
                    //ServerPlayNetworking.canSend(otherPlayer, EMOTE_PLAY_NETWORK_PACKET_ID);
                    ServerPlayNetworking.send(otherPlayer, EMOTE_PLAY_NETWORK_PACKET_ID, buf);
                }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(EMOTE_STOP_NETWORK_PACKET_ID, (server, player, handler, packetByteBuf, responseSender)->{
            StopPacket packet = new StopPacket(player.getUuid());
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            //packet.read(packetByteBuf); //just the UUID but for security reasons, these are from the arguments
            packet.write(buf);

            for(ServerPlayerEntity otherPlayer : PlayerLookup.tracking(player)){
                if(otherPlayer != player && ((IEmotecraftPresence)otherPlayer.networkHandler).getInstalledEmotecraft() != 0){
                    ServerPlayNetworking.send(otherPlayer, EMOTE_STOP_NETWORK_PACKET_ID, buf);
                }
            }
        });

        //The client will make a response but in singlePlayer it will happen before the "login" and causes a crash...
        //the channel registration will happen after a success login
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ServerPlayNetworking.registerReceiver(handler, EMOTECRAFT_DISCOVERY_PACKET_ID, (server1, player, handler1, buf, responseSender) -> {
            DiscoveryPacket packet = new DiscoveryPacket();
            packet.read(buf);
            server1.execute(()->{
                ((IEmotecraftPresence)handler).setInstalledEmotecraft(packet.getVersion());
            });
        }));


        S2CPlayChannelEvents.REGISTER.register((handler, sender, server, channels) -> {
            if(channels.contains(EMOTECRAFT_DISCOVERY_PACKET_ID)) {
                DiscoveryPacket packet = new DiscoveryPacket(networkingVersion);
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                packet.write(buf);
                sender.sendPacket(EMOTECRAFT_DISCOVERY_PACKET_ID, buf);
            }
            if(channels.contains(EMOTE_PLAY_NETWORK_PACKET_ID) && ((IEmotecraftPresence)handler).getInstalledEmotecraft() == 0){
                ((IEmotecraftPresence)handler).setInstalledEmotecraft(2);
            }
        });

    }
}


