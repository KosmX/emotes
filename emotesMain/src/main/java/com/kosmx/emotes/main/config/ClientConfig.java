package com.kosmx.emotes.main.config;

import com.kosmx.emotes.common.SerializableConfig;
import com.kosmx.emotes.common.tools.Pair;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.main.EmoteHolder;

import java.util.ArrayList;
import java.util.List;

public class ClientConfig extends SerializableConfig {

    public boolean dark = false;
    public boolean enableQuark = false;
    public boolean showIcons = true;
    public float stopThreshold = 0.04f;
    public float yRatio = 0.75f;
    public boolean loadBuiltinEmotes = true;
    public boolean enablePlayerSafety = true;
    public boolean enablePerspective = true;
    public boolean perspectiveReduxIntegration = true;

    public List<EmoteHolder> emotesWithKey = new ArrayList<>();
    public final EmoteHolder[] fastMenuEmotes = new EmoteHolder[8];

    public List<Pair<Integer, String>> emotesWithHash = new ArrayList<>();

    public void assignEmotes(){
        this.emotesWithKey = new ArrayList<>();
        for(int i = 0; i != 8; i++){
            if(fastMenuHash[i] == 0) continue;
            EmoteHolder emote = EmoteHolder.getEmoteFromHash(fastMenuHash[i]);
            this.fastMenuEmotes[i] = emote;
            if(emote == null){
                //Main.log(Level.ERROR, "Can't find emote from hash: " + fastMenuHash[i]);
            }
        }

        for(Pair<Integer, String> pair : emotesWithHash){
            EmoteHolder emote = EmoteHolder.getEmoteFromHash(pair.getLeft());
            if(emote != null){
                emote.keyBinding = EmoteInstance.instance.getDefaults().getKeyFromString(pair.getRight());
            }//Main.log(Level.ERROR, "Can't find emote from hash: " + pair.getLeft());

        }

        EmoteHolder.bindKeys(this);
    }
    public boolean modAvailableAtServer = true;
    public boolean correctServerVersion = true;
}
