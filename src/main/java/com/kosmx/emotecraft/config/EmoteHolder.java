package com.kosmx.emotecraft.config;

import com.google.gson.JsonParseException;
import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.network.EmotePacket;
import com.kosmx.emotecraft.playerInterface.ClientPlayerEmotes;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.StringRenderable;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.Level;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

public class EmoteHolder {
    public final Emote emote;
    public final StringRenderable name;
    public final StringRenderable description;
    public final StringRenderable author;
    public final int hash;
    public static List<EmoteHolder> list = new ArrayList<EmoteHolder>();
    public InputUtil.Key keyBinding = InputUtil.UNKNOWN_KEY;

    /**
     *
     * @param emote {@link com.kosmx.emotecraft.Emote}
     * @param name Emote name
     * @param description Emote decription
     * @param author Name of the Author
     */
    EmoteHolder(Emote emote, StringRenderable name, StringRenderable description, StringRenderable author, int hash){
        this.emote = emote;
        this.name = name;
        this.author = author;
        this.description = description;
        this.hash = hash;
    }

    public static void bindKeys(SerializableConfig config) {
        config.emotesWithKey.removeAll(config.emotesWithKey);
        for(EmoteHolder emote:list){
            if(!emote.keyBinding.equals(InputUtil.UNKNOWN_KEY)){
                config.emotesWithKey.add(emote);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static ActionResult playEmote(InputUtil.Key key){
        if(MinecraftClient.getInstance() != null && MinecraftClient.getInstance().getCameraEntity() != null && MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity){
            for(EmoteHolder emote : Main.config.emotesWithKey){
                if(emote.keyBinding.equals(key)){
                    emote.playEmote((PlayerEntity) MinecraftClient.getInstance().getCameraEntity());
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.PASS;
    }

    public InputUtil.Key getKeyBinding(){
        return keyBinding;
    }

    //public void setKeyBinding(InputUtil.Key key, )

    public Emote getEmote(){
        return emote;
    }

    public static EmoteHolder getEmoteFromHash(int hash){
        for(EmoteHolder emote:list){
            if (emote.hash == hash){
                return emote;
            }
        }
        return null;
    }

    public static EmoteHolder deserializeJson(BufferedReader json) throws JsonParseException {     //throws BowlingBall XD
        return Serializer.serializer.fromJson(json, EmoteHolder.class);
    }
    public static void addEmoteToList(BufferedReader json) throws JsonParseException{
        list.add(deserializeJson(json));
    }
    public static void addEmoteToList(EmoteHolder hold){
        list.add(hold);
    }

    public static void playEmote(Emote emote, PlayerEntity player){
        try {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            EmotePacket emotePacket = new EmotePacket(emote, player);
            emotePacket.write(buf);
            ClientSidePacketRegistry.INSTANCE.sendToServer(Main.EMOTE_NETWORK_PACKET_ID, buf);
            ClientPlayerEmotes target = (ClientPlayerEmotes) player;
            target.playEmote(emote);
            emote.start();
        }
        catch (Exception e){
            Main.log(Level.ERROR, "cannot play emote reason: " + e.getMessage());
            if(Main.config.showDebug)e.printStackTrace();
        }
    }
    public void playEmote(PlayerEntity playerEntity){
        playEmote(this.getEmote(), playerEntity);
    }

}

