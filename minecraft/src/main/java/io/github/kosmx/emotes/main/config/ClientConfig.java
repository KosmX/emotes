package io.github.kosmx.emotes.main.config;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.common.tools.BiMap;
import io.github.kosmx.emotes.server.config.CommonConfig;
import net.minecraft.client.CameraType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientConfig extends CommonConfig {
    public final List<ConfigEntry<?>> legacy = new ArrayList<>();

    /**
     * Wheel settings
     */
    // public final ConfigEntry<Boolean> dark = new ConfigEntry<>("dark", false, false, legacy);
    public final ConfigEntry<Boolean> oldChooseWheel = new ConfigEntry<>("oldChooseWheel", false, false, legacy);
    public final ConfigEntry<Boolean> showIconsIfPossible = new ConfigEntry<>("showIconsIfPossible", false, false, legacy);

    public final EnumConfigEntry<CloseWheel> closeWheelType = new EnumConfigEntry<>("closeWheelType", CloseWheel.PRESS, true, basics);
    public final ConfigEntry<Boolean> enableWheelKeyboardNav = new ConfigEntry<>("enableWheelKeyboardNav", true, false, basics);
    public final EnumConfigEntry<CameraType> cameraType = new EnumConfigEntry<>("cameraType", CameraType.THIRD_PERSON_FRONT, true, basics);
    public final ConfigEntry<Boolean> checkPose = new ConfigEntry<>("checkPose", true, true, expert);

    public final ConfigEntry<Boolean> alwaysOpenEmoteScreen = new ConfigEntry<>("alwaysOpenScreen", false, true, basics);
    //expert
    public final ConfigEntry<Boolean> displayNowPlaying = new ConfigEntry<>("displayNowPlaying", true, false, expert);
    public final ConfigEntry<Boolean> alwaysValidate = new ConfigEntry<>("alwaysValidateEmote", false, true, expert);
    public final ConfigEntry<Boolean> enablePlayerSafety = new ConfigEntry<>("playersafety", true, true, expert);
    public final ConfigEntry<Float> stopThreshold = new FloatConfigEntry("stopthreshold", "stopThreshold", 0.04f, true, expert, -3.912f, 8f);
    public final ConfigEntry<Float> yRatio = new FloatConfigEntry("yratio", "yRatio", 0.75f, true, expert, 0, 100);
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
}
