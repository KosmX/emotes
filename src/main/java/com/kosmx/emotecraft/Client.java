package com.kosmx.emotecraft;

import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.config.EmoteSerializer;
import com.kosmx.emotecraft.playerInterface.ClientPlayerEmotes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.nio.file.Path;

public class Client implements ClientModInitializer {

    private static KeyBinding emoteKeyBinding;
    private static KeyBinding debugEmote;
    @Override
    public void onInitializeClient() {
        //There will be something only client stuff
        //like get emote list, or add emote player key
        EmoteSerializer.initilaizeDeserializer();

        emoteKeyBinding = new KeyBinding(
                "key.emotecraft.fastchoose",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,        //because bedrock edition has the same key
                "category.emotecraft.keybinding"
        );
        debugEmote = new KeyBinding(
                "key.emotecraft.debug",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.emotecraft.keybinding"
        );
        KeyBindingHelper.registerKeyBinding(emoteKeyBinding);
        KeyBindingHelper.registerKeyBinding(debugEmote);

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (emoteKeyBinding.wasPressed()){
                if(MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity){
                    ClientPlayerEmotes entity = (ClientPlayerEmotes) MinecraftClient.getInstance().getCameraEntity();
                    entity.playEmote(EmoteHolder.list.get(0).getEmote());
                    entity.getEmote().start();
                }
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (debugEmote.wasPressed()){
                playDebugEmote();
            }
        });

        File externalEmotes = FabricLoader.getInstance().getGameDirectory().toPath().resolve("emotes").toFile();
        if(!externalEmotes.isDirectory())externalEmotes.mkdirs();
        serializeExternalEmotes(externalEmotes);
    }


    private static void serializeExternalEmotes(File path){
        for(File file:path.listFiles()){
            try{
                EmoteHolder.addEmoteToList(FileUtils.readFileToString(file, "UTF-8"));
            }
            catch (Exception e){
                Main.log(Level.ERROR, "Error while importing external emote: " + file.getName() + ".", true);
                Main.log(Level.ERROR, e.getMessage());
            }
        }
    }

    private static void playDebugEmote(){
        Main.log(Level.INFO, "Playing debug emote");
        File location = FabricLoader.getInstance().getGameDirectory().toPath().resolve("emote.json").toFile();
        try {
            EmoteHolder emoteHolder = EmoteHolder.deserializeJson(FileUtils.readFileToString(location, "UTF-8"));
            if(MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity){
                ClientPlayerEmotes entity = (ClientPlayerEmotes) MinecraftClient.getInstance().getCameraEntity();
                entity.playEmote(emoteHolder.getEmote());
                entity.getEmote().start();
            }
        }
        catch (Exception e){
            Main.log(Level.ERROR, "Error while importing debug emote.", true);
            Main.log(Level.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }
}
