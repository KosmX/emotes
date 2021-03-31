package com.kosmx.emotes.main.config;

import com.kosmx.emotes.common.SerializableConfig;
import com.kosmx.emotes.common.tools.Pair;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.main.EmoteHolder;

import java.util.ArrayList;
import java.util.List;

public class ClientConfig extends SerializableConfig {

    public final BooleanConfigEntry dark = new BooleanConfigEntry("dark", false, false, basics);
    public final ConfigEntry<Boolean> enablePerspective = new BooleanConfigEntry("perspective", true, false, basics);
    public final ConfigEntry<Boolean> loadBuiltinEmotes = new BooleanConfigEntry("loadbuiltin", "loadBuiltin", true, true, basics);
    public final ConfigEntry<Boolean> showIcons = new BooleanConfigEntry("showicon", "showIcon", true, true, basics);
    public final ConfigEntry<Boolean> enableQuark = new BooleanConfigEntry("quark", "enablequark", false, true, basics);
    //expert
    public final ConfigEntry<Boolean> enablePlayerSafety = new BooleanConfigEntry("playersafety", true, true, expert);
    public final ConfigEntry<Float> stopThreshold = new FloatConfigEntry<Float>("stopthreshold", "stopThreshold", 0.04f, true, expert, "options.generic_value", -3f, 8f, 0f){
        @Override
        public double getConfigVal() {
            return Math.log(this.get());
        }

        @Override
        public void setConfigVal(double newVal) {
            this.set((float) Math.exp(newVal));
        }

    };
    public final ConfigEntry<Float> yRatio = new FloatConfigEntry<Integer>("yratio", "yRatio", 0.75f, true, expert, "options.percent_value", 0, 100, 1){
        @Override
        public double getConfigVal() {
            return this.get()*100f;
        }

        @Override
        public void setConfigVal(double newVal) {
            this.set((float) (newVal/100f));
        }

        @Override
        public Integer getTextVal() {
            return (int)this.getConfigVal();
        }
    };


    //------------------------ Advanced config stuff ------------------------//
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
