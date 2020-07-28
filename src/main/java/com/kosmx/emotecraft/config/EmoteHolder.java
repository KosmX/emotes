package com.kosmx.emotecraft.config;

import com.google.gson.JsonParseException;
import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.math.Ease;
import com.kosmx.emotecraft.network.EmotePacket;
import com.kosmx.emotecraft.playerInterface.EmotePlayerInterface;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.StringRenderable;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
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
    public EmoteHolder(Emote emote, StringRenderable name, StringRenderable description, StringRenderable author, int hash){
        this.emote = emote;
        this.name = name;
        this.author = author;
        this.description = description;
        this.hash = hash;
    }

    public static void bindKeys(SerializableConfig config) {
        config.emotesWithKey = new ArrayList<>();
        config.emotesWithHash = new ArrayList<>();
        for(EmoteHolder emote:list){
            if(!emote.keyBinding.equals(InputUtil.UNKNOWN_KEY)){
                config.emotesWithKey.add(emote);
                config.emotesWithHash.add(new Pair<>(emote.hash, emote.keyBinding.getTranslationKey()));
            }
        }
        config.fastMenuHash = new int[8];
        for(int i = 0; i != 8; i++){
            if(Main.config.fastMenuEmotes[i] != null){
                Main.config.fastMenuHash[i] = Main.config.fastMenuEmotes[i].hash;
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

    public static boolean playEmote(Emote emote, PlayerEntity player){
        if(canPlayEmote(player)) {
            try {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                EmotePacket emotePacket = new EmotePacket(emote, player);
                emotePacket.write(buf);
                ClientSidePacketRegistry.INSTANCE.sendToServer(Main.EMOTE_PLAY_NETWORK_PACKET_ID, buf);
                EmotePlayerInterface target = (EmotePlayerInterface) player;
                target.playEmote(emote);
                emote.start();
            } catch (Exception e) {
                Main.log(Level.ERROR, "cannot play emote reason: " + e.getMessage());
                if (Main.config.showDebug) e.printStackTrace();
            }
            return true;
        }
        else {
            return false;
        }
    }

    private static boolean canPlayEmote(PlayerEntity entity){
        if(!canRunEmote(entity))return false;
        if(entity != MinecraftClient.getInstance().getCameraEntity())return false;
        EmotePlayerInterface target = (EmotePlayerInterface)entity;
        return !Emote.isRunningEmote(target.getEmote());
    }

    public static boolean canRunEmote(Entity entity){
        if(!(entity instanceof AbstractClientPlayerEntity))return false;
        AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) entity;
        if(player.getPose() != EntityPose.STANDING)return false;
        //System.out.println(player.getPos().distanceTo(new Vec3d(player.prevX, player.prevY, player.prevZ)));
        return !(player.getPos().distanceTo(new Vec3d(player.prevX, player.prevY, player.prevZ)) > 0.04f);
    }

    public boolean playEmote(PlayerEntity playerEntity){
        return playEmote(this.getEmote(), playerEntity);
    }

}

