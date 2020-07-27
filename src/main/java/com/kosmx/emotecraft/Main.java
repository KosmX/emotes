package com.kosmx.emotecraft;

import com.google.gson.JsonParseException;
import com.kosmx.emotecraft.config.SerializableConfig;
import com.kosmx.emotecraft.config.Serializer;
import com.kosmx.emotecraft.network.EmotePacket;
import com.kosmx.emotecraft.network.StopPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;


public class Main implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    //init and config variables

    public static final String MOD_ID = "emotecraft";
    public static final String MOD_NAME = "Emotecraft";
    public static final Path CONFIGPATH = FabricLoader.getInstance().getConfigDir().resolve("emotecraft.json");

    public static SerializableConfig config;

    public static final Identifier EMOTE_PLAY_NETWORK_PACKET_ID = new Identifier(MOD_ID, "playemote");
    public static final Identifier EMOTE_STOP_NETWORK_PACKET_ID = new Identifier(MOD_ID, "stopemote");

    @Override
    public void onInitialize() {
        Serializer.initializeSerializer();/*
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT){ //I can't do it in the client initializer because I need it to serialize the config
            Client.initEmotes();
        }
        */
        if(CONFIGPATH.toFile().isFile()){
            try {
                BufferedReader reader = Files.newBufferedReader(CONFIGPATH);
                config = Serializer.serializer.fromJson(reader, SerializableConfig.class);
                reader.close();
                //config = Serializer.serializer.fromJson(FileUtils.readFileToString(CONFIGPATH, "UTF-8"), SerializableConfig.class);
            }
            catch (Throwable e){
                config = new SerializableConfig();
                if(e instanceof IOException){
                    Main.log(Level.ERROR, "Can't access to config file: " + e.getLocalizedMessage(), true);
                }
                else if(e instanceof JsonParseException){
                    Main.log(Level.ERROR, "Config is invalid Json file: " + e.getLocalizedMessage(), true);
                }
                else {
                    e.printStackTrace();
                }
            }
        }
        else {
            config = new SerializableConfig();
        }

        log(Level.INFO, "Initializing");

        initServerNetwork(); //Network handler both dedicated server and client internal server
    }

    public static void log(Level level, String message){
        log(level, message, false);
    }

    public static void log(Level level, String message, boolean force){
        if (force || (config != null && config.showDebug)) LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

    private void initServerNetwork(){
        ServerSidePacketRegistry.INSTANCE.register(EMOTE_PLAY_NETWORK_PACKET_ID, ((packetContext, packetByteBuf) -> {EmotePacket packet = new EmotePacket();
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            try {
                if(!packet.read(packetByteBuf) && config.validateEmote){
                    //Todo kick player
                    Main.log(Level.INFO,  packetContext.getPlayer().getEntityName() + " is trying to play invalid emote");
                    return;
                }
                packet.write(buf);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            Stream<PlayerEntity> players = PlayerStream.watching(packetContext.getPlayer());
            players.forEach(playerEntity -> {                                   //TODO check correct emote and kick if not
                if (playerEntity == packetContext.getPlayer()) return;
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerEntity, EMOTE_PLAY_NETWORK_PACKET_ID, buf);
            });
        }));

        ServerSidePacketRegistry.INSTANCE.register(EMOTE_STOP_NETWORK_PACKET_ID, ((packetContex, packetByteBuf) -> {
            StopPacket packet = new StopPacket();
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            try{
                packet.read(packetByteBuf);
                packet.write(buf);
            }
            catch (IOException e){
                e.printStackTrace();
                return;
            }
            Stream<PlayerEntity> players = PlayerStream.watching(packetContex.getPlayer());
            players.forEach(player -> {
                if(player == packetContex.getPlayer())return;
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, EMOTE_STOP_NETWORK_PACKET_ID, buf);
            });
        }));
    }


}