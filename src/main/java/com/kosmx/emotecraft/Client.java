package com.kosmx.emotecraft;

import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.config.EmoteSerializer;
import com.kosmx.emotecraft.network.EmotePacket;
import com.kosmx.emotecraft.playerInterface.ClientPlayerEmotes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class Client implements ClientModInitializer {

    private static final ResourceManager resourceManager = new ReloadableResourceManagerImpl(ResourceType.CLIENT_RESOURCES);
    private static KeyBinding emoteKeyBinding;
    private static KeyBinding debugEmote;
    @Override
    public void onInitializeClient() {
        //There will be something only client stuff
        //like get emote list, or add emote player key
        EmoteSerializer.initilaizeDeserializer();
        //Every type of initializing process has it's own method... It's easier to see through that

        initKeyBindings();      //Init keyBinding, including debug key

        initNetworkClient();        //Init the Client-ide network manager. The Main'll have a server-side

        initEmotes();       //Import the emotes, including both the default and the external.


    }

    private void initNetworkClient(){
        ClientSidePacketRegistry.INSTANCE.register(Main.EMOTE_NETWORK_PACKET_ID, ((packetContext, packetByteBuf) -> {
            EmotePacket emotePacket;
            Emote emote;
            try{
                emotePacket = new EmotePacket();
                emotePacket.read(packetByteBuf);
            }
            catch(IOException e) {
                Main.log(Level.ERROR, e.getMessage());
                if (Main.config.showDebug) e.printStackTrace();
                return;
            }
            emote = emotePacket.getEmote();
            packetContext.getTaskQueue().execute(() ->{
                PlayerEntity playerEntity = MinecraftClient.getInstance().world.getPlayerByUuid(emotePacket.getPlayer());
                if(playerEntity != null) {
                    ((ClientPlayerEmotes) playerEntity).playEmote(emote);
                    ((ClientPlayerEmotes) playerEntity).getEmote().start();
                }
            });
        }));
    }

    private void initEmotes(){
        //Serialize emotes


        serializeInternalEmotes("waving");
        //TODO add internal emotes to the list

        File externalEmotes = FabricLoader.getInstance().getGameDir().resolve("emotes").toFile();
        if(!externalEmotes.isDirectory())externalEmotes.mkdirs();
        serializeExternalEmotes(externalEmotes);
    }

    private static void serializeInternalEmotes(String name){
        InputStream stream = Client.class.getResourceAsStream("/assets/" + Main.MOD_ID + "/emotes/" + name + ".json");
        InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        Reader reader = new BufferedReader(streamReader);
        EmoteHolder.addEmoteToList(EmoteSerializer.deserializer.fromJson(reader, EmoteHolder.class));
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
        File location = FabricLoader.getInstance().getGameDir().resolve("emote.json").toFile();
        try {
            EmoteHolder emoteHolder = EmoteHolder.deserializeJson(FileUtils.readFileToString(location, "UTF-8"));
            if(MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity){
                PlayerEntity entity = (PlayerEntity) MinecraftClient.getInstance().getCameraEntity();
                emoteHolder.playEmote(entity);
            }
        }
        catch (Exception e){
            Main.log(Level.ERROR, "Error while importing debug emote.", true);
            Main.log(Level.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }


    private void initKeyBindings(){
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
                    EmoteHolder.list.get(0).playEmote((AbstractClientPlayerEntity)entity);
                }
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (debugEmote.wasPressed()){
                playDebugEmote();
            }
        });
    }
}
