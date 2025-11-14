package io.github.kosmx.emotes.main.config;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.common.tools.BiMap;
import net.minecraft.client.CameraType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientConfig extends SerializableConfig {
    public final List<ConfigEntry<?>> legacy = new ArrayList<>();

    /**
     * Wheel settings
     */
    // public final ConfigEntry<Boolean> dark = new ConfigEntry<>("dark", false, false, legacy);
    public final ConfigEntry<Boolean> oldChooseWheel = new ConfigEntry<>("oldChooseWheel", false, false, legacy);
    public final ConfigEntry<Boolean> showIconsIfPossible = new ConfigEntry<>("showIconsIfPossible", false, false, legacy);

    public final ConfigEntry<Boolean> enablePerspective = new ConfigEntry<>("perspective", true, false, basics);
    public final ConfigEntry<Boolean> frontAsTPPerspective = new ConfigEntry<>("default3rdPersonFront", false, false, basics);
    public final ConfigEntry<Boolean> checkPose = new ConfigEntry<>("checkPose", true, true, expert);

    public final ConfigEntry<Boolean> alwaysOpenEmoteScreen = new ConfigEntry<>("alwaysOpenScreen", false, true, basics);
    //expert
    public final ConfigEntry<Boolean> alwaysValidate = new ConfigEntry<>("alwaysValidateEmote", false, true, expert);
    public final ConfigEntry<Boolean> enablePlayerSafety = new ConfigEntry<>("playersafety", true, true, expert);
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
    public final ConfigEntry<Boolean> showHiddenConfig = new ConfigEntry<>("showHiddenConfig", false, true, expert, false);
    // public final ConfigEntry<Boolean> neverRemoveBadIcon = new ConfigEntry<>("neverRemoveBadIcon", false, expert, true);
    // public final ConfigEntry<Boolean> exportBuiltin = new ConfigEntry<>("exportBuiltin", false, expert, true);

    //------------------------ Client-only overrides ------------------------//

    /**
     * This will override default values before loading the config file.
     */
    public ClientConfig() {
        this.loadEmotesServerSide.set(false);
    }

    @Override
    public void iterate(Consumer<ConfigEntry<?>> consumer) {
        super.iterate(consumer);
        this.legacy.forEach(consumer);
    }

    //------------------------ Advanced config stuff ------------------------//
    //public List<EmoteHolder> emotesWithKey = new ArrayList<>();
    //public final EmoteHolder[] fastMenuEmotes = new EmoteHolder[8];

    public BiMap<UUID, InputConstants.Key> emoteKeyMap = new BiMap<>();
    public UUID[][] fastMenuEmotes = new UUID[15][8];

    //------------------------ Random tweak stuff ------------------------//

    // public final ConfigEntry<Boolean> hideWarningMessage = new ConfigEntry<>("hideWarning", false, expert, true);

    public CameraType getCameraType() {
        return frontAsTPPerspective.get() ? CameraType.THIRD_PERSON_FRONT : CameraType.THIRD_PERSON_BACK;
    }
}
