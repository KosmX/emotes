package io.github.kosmx.emotes.main;

import dev.kosmx.playerAnim.core.data.AnimationFormat;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.inline.TmpGetters;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Initializing client and other load stuff...
 *
 */
public class MainClientInit {

    public static final ResourceLocation TEXTURE_NO_MOD = new ResourceLocation("emotecraft:textures/icons/emotecraft_not_installed.png");//TODO HAS MODE icon
    @Nullable
    public static HashSet<UUID> playerHasMode = null;//TODO HAS MODE null because check on render if server not have mod/plugin

    public static void init(){
        loadEmotes();//:D

        ClientPacketManager.init(); //initialize proxy service
        ClientEmotePlay.init();
    }


    public static void loadEmotes() {
        UniversalEmoteSerializer.loadEmotes();

        EmoteHolder.clearEmotes();

        EmoteHolder.addEmoteToList(UniversalEmoteSerializer.hiddenServerEmotes);

    }

    /**
     * play the test emote
     */
    public static void playDebugEmote(){
        EmoteInstance.instance.getLogger().log(Level.INFO, "Playing debug emote");
        Path location = null;
        for(AnimationFormat source:AnimationFormat.values()){
            location = EmoteInstance.instance.getGameDirectory().resolve("emote." + source.getExtension());
            if(location.toFile().isFile()){
                break;
            }
        }
        if(location == null)return;
        try{
            InputStream reader = Files.newInputStream(location);
            EmoteHolder emoteHolder = new EmoteHolder(UniversalEmoteSerializer.readData(reader, location.getFileName().toString()).get(0));
            reader.close();
            if(TmpGetters.getClientMethods().getMainPlayer() != null){
                emoteHolder.playEmote(TmpGetters.getClientMethods().getMainPlayer());
            }
        }catch(Exception e){
            EmoteInstance.instance.getLogger().log(Level.INFO, "Error while importing debug emote.", true);
            EmoteInstance.instance.getLogger().log(Level.INFO, e.getMessage());
            EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
        }
    }
}
