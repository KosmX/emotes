package com.kosmx.emotecraft.network;


import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.mixinInterface.IEmotecraftPresence;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;


/**
 * Emotecraft core package for networking
 * //ALWAYS// send packages with these methods. (or with the client methods)
 * This package will run on the server (including local server)
 */
public class ServerNetwork {


    public static final Identifier EMOTE_PLAY_NETWORK_PACKET_ID = new Identifier(Main.MOD_ID, "playemote");
    public static final Identifier EMOTE_STOP_NETWORK_PACKET_ID = new Identifier(Main.MOD_ID, "stopemote");
    /**
     * packet initializer, both for server and client side
     */
    public static void init(){
        ServerPlayNetworking.registerGlobalReceiver(EMOTE_PLAY_NETWORK_PACKET_ID, (server, player, handler, packetByteBuf, responseSender)->{
            EmotePacket packet = new EmotePacket();
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            if(! packet.read(packetByteBuf, Main.config.validateEmote)){
                //Todo kick player
                Main.log(Level.INFO, player.getEntityName() + " is trying to play invalid emote", true);
                return;
            }
            packet.write(buf);

            for(ServerPlayerEntity otherPlayer : PlayerLookup.tracking(player)){
                if(otherPlayer != player && ((IEmotecraftPresence)otherPlayer.networkHandler).hasEmotecraftInstalled()){
                    ServerPlayNetworking.send(otherPlayer, EMOTE_PLAY_NETWORK_PACKET_ID, buf);
                }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(EMOTE_STOP_NETWORK_PACKET_ID, (server, player, handler, packetByteBuf, responseSender)->{
            StopPacket packet = new StopPacket();
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            packet.read(packetByteBuf);
            packet.write(buf);

            for(ServerPlayerEntity otherPlayer : PlayerLookup.tracking(player)){
                if(otherPlayer != player && ((IEmotecraftPresence)otherPlayer.networkHandler).hasEmotecraftInstalled()){
                    ServerPlayNetworking.send(otherPlayer, EMOTE_STOP_NETWORK_PACKET_ID, buf);
                }
            }
        });
    }
}
