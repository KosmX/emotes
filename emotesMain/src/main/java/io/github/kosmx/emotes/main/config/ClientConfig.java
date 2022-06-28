package io.github.kosmx.emotes.main.config;

import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.common.tools.BiMap;
import io.github.kosmx.emotes.executor.dataTypes.InputKey;

import java.util.UUID;

public class ClientConfig extends SerializableConfig {

    public final BooleanConfigEntry dark = new BooleanConfigEntry("dark", false, false, basics);

    public final BooleanConfigEntry oldChooseWheel = new BooleanConfigEntry("oldChooseWheel", false, false, basics);
    public final ConfigEntry<Boolean> enablePerspective = new BooleanConfigEntry("perspective", true, false, basics);
    public final BooleanConfigEntry frontAsTPPerspective = new BooleanConfigEntry("default3rdPersonFront", false, false, basics);
    public final ConfigEntry<Boolean> showIcons = new BooleanConfigEntry("showicon", "showIcon", true, false, basics);
    public final ConfigEntry<Boolean> enableNSFW = new BooleanConfigEntry("enableNSFW", false, true, basics);
    //expert
    public final ConfigEntry<Boolean> alwaysValidate = new BooleanConfigEntry("alwaysValidateEmote", false, true, expert);
    public final ConfigEntry<Boolean> enablePlayerSafety = new BooleanConfigEntry("playersafety", true, true, expert);
    public final ConfigEntry<Float> stopThreshold = new FloatConfigEntry("stopthreshold", "stopThreshold", 0.04f, true, expert, "options.generic_value", -3.912f, 8f, 0f){
        @Override
        public double getConfigVal() {
            return Math.log(this.get());
        }

        @Override
        public void setConfigVal(double newVal) {
            this.set((float) Math.exp(newVal));
        }

    };
    public final ConfigEntry<Float> yRatio = new FloatConfigEntry("yratio", "yRatio", 0.75f, true, expert, "options.percent_value", 0, 100, 1){
        @Override
        public double getConfigVal() {
            return this.get()*100f;
        }

        @Override
        public void setConfigVal(double newVal) {
            this.set((float) (newVal/100f));
        }

        @Override
        public double getTextVal() {
            return this.getConfigVal();
        }
    };
    public final ConfigEntry<Boolean> showHiddenConfig = new BooleanConfigEntry("showHiddenConfig", false, true, expert, false);
    public final ConfigEntry<Boolean> neverRemoveBadIcon = new BooleanConfigEntry("neverRemoveBadIcon", false, expert, true);
    public final ConfigEntry<Boolean> exportBuiltin = new BooleanConfigEntry("exportBuiltin", false, expert, true);



    //------------------------ Client-only overrides ------------------------//

    //This will override default values before loading the config file.
    public ClientConfig(){
        loadEmotesServerSide.set(false);
    }

    //------------------------ Advanced config stuff ------------------------//
    //public List<EmoteHolder> emotesWithKey = new ArrayList<>();
    //public final EmoteHolder[] fastMenuEmotes = new EmoteHolder[8];

    public BiMap<UUID, InputKey> emoteKeyMap = new BiMap<>();
    public UUID[][] fastMenuEmotes = new UUID[10][8];

    //------------------------ Random tweak stuff ------------------------//

    public final ConfigEntry<Boolean> hideWarningMessage = new BooleanConfigEntry("hideWarning", false, expert, true);
}
