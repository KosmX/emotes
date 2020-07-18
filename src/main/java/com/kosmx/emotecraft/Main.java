package com.kosmx.emotecraft;

import com.kosmx.emotecraft.config.Config;
import com.kosmx.emotecraft.network.EmotePacket;
import io.netty.buffer.Unpooled;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.stream.Stream;


public class Main implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    //init and config variables

    public static final String MOD_ID = "emotecraft";
    public static final String MOD_NAME = "Emotecraft";

    public static Config config;

    public static final Identifier EMOTE_NETWORK_PACKET_ID = new Identifier(MOD_ID, "playemote");

    @Override
    public void onInitialize() {

        AutoConfig.register(Config.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(Config.class).getConfig();

        log(Level.INFO, "Initializing");

        initServerNetwork(); //Network handler both dedicated server and client internal server
    }

    public static void log(Level level, String message){
        log(level, message, false);
    }

    public static void log(Level level, String message, boolean force){
        if (force || config == null || config.showDebug) LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

    private void initServerNetwork(){
        ServerSidePacketRegistry.INSTANCE.register(EMOTE_NETWORK_PACKET_ID, ((packetContext, packetByteBuf) -> {EmotePacket packet = new EmotePacket();
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            try {
                packet.read(packetByteBuf); //TODO check exploiting
                packet.write(buf);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            Stream<PlayerEntity> players = PlayerStream.watching(packetContext.getPlayer());
            players.forEach(playerEntity -> { //TODO check correct emote and kick if not
                if (playerEntity == packetContext.getPlayer()) return;
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerEntity, EMOTE_NETWORK_PACKET_ID, buf);
            });
        }));
    }

}