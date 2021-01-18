package com.kosmx.emotecraft;

import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.config.Serializer;
import com.kosmx.emotecraft.gui.ingame.FastMenuScreen;
import com.kosmx.emotecraft.mixinInterface.EmotePlayerInterface;
import com.kosmx.emotecraft.network.ClientNetwork;
import com.kosmx.emotecraft.network.EmotePacket;
import com.kosmx.emotecraft.network.StopPacket;
import com.kosmx.quarktool.QuarkReader;
import io.netty.buffer.Unpooled;
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
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Client implements ClientModInitializer {

    private static KeyBinding emoteKeyBinding;
    private static KeyBinding debugEmote;
    private static KeyBinding stopEmote;
    public static final File externalEmotes = FabricLoader.getInstance().getGameDir().resolve("emotes").toFile();

    @Override
    public void onInitializeClient(){
        //There is the only client stuff
        //like get emote list, or add emote player key
        //EmoteSerializer.initilaizeDeserializer();
        //Every type of initializing process has it's own method... It's easier to see through that

        initKeyBindings();      //Init keyBinding, including debug key

        ClientNetwork.init();        //Init the Client-ide network manager. The Main will have a server-side

        initEmotes();       //Import the emotes, including both the default and the external.


    }

    public static void initEmotes(){
        //Serialize emotes
        EmoteHolder.clearEmotes();

        serializeInternalEmotes("waving");
        serializeInternalEmotes("clap");
        serializeInternalEmotes("crying");
        serializeInternalEmotes("point");
        serializeInternalEmotes("here");
        serializeInternalEmotes("palm");
        serializeInternalEmotes("backflip");
        serializeInternalEmotes("roblox_potion_dance");
        serializeInternalEmotes("kazotsky_kick");
        //TODO add internal emotes to the list


        if(! externalEmotes.isDirectory()) externalEmotes.mkdirs();
        serializeExternalEmotes();

        Main.config.assignEmotes();
    }

    private static void serializeInternalEmotes(String name){
        if(!Main.config.loadBuiltinEmotes){
            return;
        }
        InputStream stream = Client.class.getResourceAsStream("/assets/" + Main.MOD_ID + "/emotes/" + name + ".json");
        InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        Reader reader = new BufferedReader(streamReader);
        EmoteHolder emoteHolder = Serializer.serializer.fromJson(reader, EmoteHolder.class);
        EmoteHolder.addEmoteToList(emoteHolder);
        emoteHolder.bindIcon(("/assets/" + Main.MOD_ID + "/emotes/" + name + ".png"));
    }

    private static void serializeExternalEmotes(){
        for(File file : Objects.requireNonNull(Client.externalEmotes.listFiles((dir, name)->name.endsWith(".json")))){
            try{
                BufferedReader reader = Files.newBufferedReader(file.toPath());
                EmoteHolder emote = EmoteHolder.deserializeJson(reader);
                EmoteHolder.addEmoteToList(emote);
                reader.close();
                File icon = Client.externalEmotes.toPath().resolve(file.getName().substring(0, file.getName().length() - 5) + ".png").toFile();
                if(icon.isFile()) emote.bindIcon(icon);
            }catch(Exception e){
                Main.log(Level.ERROR, "Error while importing external emote: " + file.getName() + ".", true);
                Main.log(Level.ERROR, e.getMessage());
            }
        }

        if(Main.config.enableQuark){
            Main.log(Level.WARN, "Quark importer is active", true);
            initQuarkEmotes(Client.externalEmotes);
        }
    }

    private static void initQuarkEmotes(File path){
        for(File file : Objects.requireNonNull(path.listFiles((dir, name)->name.endsWith(".emote")))){
            Main.log(Level.INFO, "[Quarktool]  Importing Quark emote: " + file.getName());
            try{
                BufferedReader reader = Files.newBufferedReader(file.toPath());
                QuarkReader quarkReader = new QuarkReader();
                if(quarkReader.deserialize(reader, file.getName())){
                    EmoteHolder emote = quarkReader.getEmote();
                    EmoteHolder.addEmoteToList(emote);
                    File icon = Client.externalEmotes.toPath().resolve(file.getName().substring(0, file.getName().length() - 6) + ".png").toFile();
                    if(icon.isFile()) emote.bindIcon(icon);
                }
            }catch(Throwable e){ //try to catch everything
                if(Main.config.showDebug){
                    Main.log(Level.ERROR, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    private static void playDebugEmote(){
        Main.log(Level.INFO, "Playing debug emote");
        Path location = FabricLoader.getInstance().getGameDir().resolve("emote.json");
        try{
            BufferedReader reader = Files.newBufferedReader(location);
            EmoteHolder emoteHolder = EmoteHolder.deserializeJson(reader);
            reader.close();
            if(MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity){
                PlayerEntity entity = (PlayerEntity) MinecraftClient.getInstance().getCameraEntity();
                emoteHolder.playEmote(entity);
            }
        }catch(Exception e){
            Main.log(Level.ERROR, "Error while importing debug emote.", true);
            Main.log(Level.ERROR, e.getMessage());
            e.printStackTrace();
        }
    }


    private void initKeyBindings(){
        emoteKeyBinding = new KeyBinding("key.emotecraft.fastchoose", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B,        //because bedrock edition has the same key
                "category.emotecraft.keybinding");
        if(FabricLoader.getInstance().getGameDir().resolve("emote.json").toFile().isFile()){ //Secret feature//
            debugEmote = new KeyBinding("key.emotecraft.debug", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O,       //I don't know why... just
                    "category.emotecraft.keybinding");
            KeyBindingHelper.registerKeyBinding(debugEmote);
            ClientTickEvents.END_CLIENT_TICK.register(minecraftClient->{
                if(debugEmote.wasPressed()){
                    playDebugEmote();
                }
            });
        }
        KeyBindingHelper.registerKeyBinding(emoteKeyBinding);

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient->{
            if(emoteKeyBinding.wasPressed()){
                if(MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity){
                    MinecraftClient.getInstance().openScreen(new FastMenuScreen(new TranslatableText("emotecraft.fastmenu")));
                }
            }
        });

        stopEmote = new KeyBinding("key.emotecraft.stop", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.emotecraft.keybinding");
        KeyBindingHelper.registerKeyBinding(stopEmote);

        ClientTickEvents.END_CLIENT_TICK.register(minecraftClient->{
            if(stopEmote.wasPressed() && MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity && Emote.isRunningEmote(((EmotePlayerInterface) MinecraftClient.getInstance().getCameraEntity()).getEmote())){
                ClientNetwork.clientSendStop();
            }
        });

        KeyPressCallback.EVENT.register((EmoteHolder::playEmote));
    }
}
