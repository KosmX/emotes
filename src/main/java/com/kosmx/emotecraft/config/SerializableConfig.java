package com.kosmx.emotecraft.config;

import com.kosmx.emotecraft.Main;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;

public class SerializableConfig {
    @Environment(EnvType.CLIENT)
    public List<EmoteHolder> emotesWithKey = new ArrayList<>();
    @Environment(EnvType.CLIENT)
    public final EmoteHolder[] fastMenuEmotes = new EmoteHolder[8];

    public boolean validateEmote = false;
    public boolean showDebug = false;
    public int configVersion;
    @Environment(EnvType.CLIENT)
    public boolean dark = false;
    @Environment(EnvType.CLIENT)
    public boolean enableQuark = false;
    @Environment(EnvType.CLIENT)
    public boolean showIcons = true;
    @Environment(EnvType.CLIENT)
    public float stopThreshold = 0.04f;
    @Environment(EnvType.CLIENT)
    public float yRatio = 0.75f;
    @Environment(EnvType.CLIENT)
    public boolean loadBuiltinEmotes = true;

    public int[] fastMenuHash = new int[8];
    public List<Pair<Integer, String>> emotesWithHash = new ArrayList<>();

    public void assignEmotes(){
        this.emotesWithKey = new ArrayList<>();
        for(int i = 0; i != 8; i++){
            if(fastMenuHash[i] == 0) continue;
            EmoteHolder emote = EmoteHolder.getEmoteFromHash(fastMenuHash[i]);
            this.fastMenuEmotes[i] = emote;
            if(emote == null){
                Main.log(Level.ERROR, "Can't find emote from hash: " + fastMenuHash[i]);
            }
        }

        for(Pair<Integer, String> pair : emotesWithHash){
            EmoteHolder emote = EmoteHolder.getEmoteFromHash(pair.getLeft());
            if(emote != null){
                emote.keyBinding = InputUtil.fromTranslationKey(pair.getRight());
            }else{
                Main.log(Level.ERROR, "Can't find emote from hash: " + pair.getLeft());
            }
        }

        EmoteHolder.bindKeys(this);
    }
}
