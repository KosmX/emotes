package com.kosmx.emotecraft.config;

import com.google.gson.JsonParseException;
import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.network.EmotePacket;
import com.kosmx.emotecraft.playerInterface.ClientPlayerEmotes;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.StringRenderable;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Level;

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



    public Emote getEmote(){
        return emote;
    }

    public static EmoteHolder deserializeJson(String json) throws JsonParseException {     //throws BowlingBall XD
        return EmoteSerializer.deserializer.fromJson(json, EmoteHolder.class);
    }
    public static void addEmoteToList(String json) throws JsonParseException{
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

