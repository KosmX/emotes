package com.kosmx.emotecraft;

import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.config.Serializer;
import com.kosmx.emotecraft.network.EmotePacket;
import com.kosmx.emotecraft.network.StopPacket;
import com.kosmx.emotecraft.playerInterface.EmotePlayerInterface;
import com.kosmx.emotecraft.screen.ingame.FastMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Client implements ClientModInitializer {

    private static final ResourceManager resourceManager = new ReloadableResourceManagerImpl(ResourceType.CLIENT_RESOURCES);
    private static KeyBinding emoteKeyBinding;
    private static KeyBinding debugEmote;
    public static final File externalEmotes = FabricLoader.getInstance().getGameDir().resolve("emotes").toFile();
    @Override
    public void onInitializeClient() {
        //There will be something only client stuff
        //like get emote list, or add emote player key
        //EmoteSerializer.initilaizeDeserializer();
        //Every type of initializing process has it's own method... It's easier to see through that

        initKeyBindings();      //Init keyBinding, including debug key

        initNetworkClient();        //Init the Client-ide network manager. The Main'll have a server-side

        //initEmotes();       //Import the emotes, including both the default and the external.


    }

    private void initNetworkClient(){
        ClientSidePacketRegistry.INSTANCE.register(Main.EMOTE_PLAY_NETWORK_PACKET_ID, ((packetContext, packetByteBuf) -> {
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
                    ((EmotePlayerInterface) playerEntity).playEmote(emote);
                    ((EmotePlayerInterface) playerEntity).getEmote().start();
                }
            });
        }));

        ClientSidePacketRegistry.INSTANCE.register(Main.EMOTE_STOP_NETWORK_PACKET_ID, ((packetContex, packetByyeBuf) -> {
            StopPacket packet = new StopPacket();
            try{
                packet.read(packetByyeBuf);

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            packetContex.getTaskQueue().execute(()-> {
                EmotePlayerInterface player = (EmotePlayerInterface) MinecraftClient.getInstance().world.getPlayerByUuid(packet.getPlayer());
                if(player != null && Emote.isRunningEmote(player.getEmote()))player.getEmote().stop();
            });
        }));
    }

    public static void initEmotes(){
        //Serialize emotes


        serializeInternalEmotes("waving");
        //TODO add internal emotes to the list


        if(!externalEmotes.isDirectory())externalEmotes.mkdirs();
        serializeExternalEmotes(externalEmotes);
    }

    private static void serializeInternalEmotes(String name){
        InputStream stream = Client.class.getResourceAsStream("/assets/" + Main.MOD_ID + "/emotes/" + name + ".json");
        InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        Reader reader = new BufferedReader(streamReader);
        EmoteHolder.addEmoteToList(Serializer.serializer.fromJson(reader, EmoteHolder.class));
    }

    private static void serializeExternalEmotes(File path){
        for(File file:path.listFiles()){
            try{
                BufferedReader reader = Files.newBufferedReader(file.toPath());
                EmoteHolder.addEmoteToList(reader);
                reader.close();
            }
            catch (Exception e){
                Main.log(Level.ERROR, "Error while importing external emote: " + file.getName() + ".", true);
                Main.log(Level.ERROR, e.getMessage());
            }
        }
    }

    private static void playDebugEmote(){
        Main.log(Level.INFO, "Playing debug emote");
        Path location = FabricLoader.getInstance().getGameDir().resolve("emote.json");
        try {
            BufferedReader reader = Files.newBufferedReader(location);
            EmoteHolder emoteHolder = EmoteHolder.deserializeJson(reader);
            reader.close();
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
        if(FabricLoader.getInstance().getGameDir().resolve("emote.json").toFile().isFile()) { //Secret feature//
            debugEmote = new KeyBinding(
                    "key.emotecraft.debug",
                    InputUtil.Type.KEYSYM,
                    GLFW.GLFW_KEY_O,       //I don't know why... just
                    "category.emotecraft.keybinding"
            );
            KeyBindingHelper.registerKeyBinding(debugEmote);
            ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
                if (debugEmote.wasPressed()){
                    playDebugEmote();
                }
            });
        }
        KeyBindingHelper.registerKeyBinding(emoteKeyBinding);

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
            if (emoteKeyBinding.wasPressed()){
                if(MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity){
                    MinecraftClient.getInstance().openScreen(new FastMenuScreen(new TranslatableText("emotecraft.fastmenu")));
                }
            }
        });

        KeyPressCallback.EVENT.register((EmoteHolder::playEmote));

    }
}
