package io.github.kosmx.emotes.main.config;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.arch.library.LibraryStatus;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.server.config.CommonConfig;
import net.minecraft.client.CameraType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientConfig extends CommonConfig {
    /**
     * Wheel settings
     */
    // public final ConfigEntry<Boolean> dark = new ConfigEntry<>("dark", false, false, category("legacy"));
    public final ConfigEntry<Boolean> oldChooseWheel = new ConfigEntry<>("oldChooseWheel", false, false, category("legacy"));
    public final ConfigEntry<Boolean> showIconsIfPossible = new ConfigEntry<>("showIconsIfPossible", false, false, category("legacy"));

    public final EnumConfigEntry<CloseWheel> closeWheelType = new EnumConfigEntry<>("closeWheelType", CloseWheel.PRESS, true, category("general"));
    public final ConfigEntry<Boolean> enableWheelKeyboardNav = new ConfigEntry<>("enableWheelKeyboardNav", true, false, category("general"));
    public final EnumConfigEntry<CameraType> cameraType = new EnumConfigEntry<>("cameraType", CameraType.THIRD_PERSON_FRONT, true, category("general"));
    public final ConfigEntry<Boolean> checkPose = new ConfigEntry<>("checkPose", true, true, category("expert"));

    public final ConfigEntry<Boolean> alwaysOpenEmoteScreen = new ConfigEntry<>("alwaysOpenScreen", false, true, category("general"));
    //expert
    public final ConfigEntry<Boolean> displayNowPlaying = new ConfigEntry<>("displayNowPlaying", true, false, category("expert"));
    public final ConfigEntry<Boolean> alwaysValidate = new ConfigEntry<>("alwaysValidateEmote", false, true, category("expert"));
    public final ConfigEntry<Boolean> enablePlayerSafety = new ConfigEntry<>("playersafety", true, true, category("expert"));
    public final ConfigEntry<Float> stopThreshold = new FloatConfigEntry("stopthreshold", "stopThreshold", 0.04f, true, category("expert"), -3.912f, 8f);
    public final ConfigEntry<Float> yRatio = new FloatConfigEntry("yratio", "yRatio", 0.75f, true, category("expert"), 0f, 100f);
    public final ConfigEntry<Boolean> showHiddenConfig = new ConfigEntry<>("showHiddenConfig", false, true, category("expert"), false);
    // public final ConfigEntry<Boolean> neverRemoveBadIcon = new ConfigEntry<>("neverRemoveBadIcon", false, expert, true);
    // public final ConfigEntry<Boolean> exportBuiltin = new ConfigEntry<>("exportBuiltin", false, expert, true);
    public final EnumConfigEntry<LibraryStatus> cloudLibraryStatus = new EnumConfigEntry<>("cloudLibraryStatus", LibraryStatus.SHOW_FOLDER, false, category("library"), false);

    //------------------------ Client-only overrides ------------------------//

    /**
     * This will override default values before loading the config file.
     */
    public ClientConfig() {
        this.loadEmotesServerSide.set(false);
    }

    //------------------------ Advanced config stuff ------------------------//
    //public List<EmoteHolder> emotesWithKey = new ArrayList<>();
    //public final EmoteHolder[] fastMenuEmotes = new EmoteHolder[8];

    // Bindings store the emote itself (serialized as an animation), not a UUID: they survive updates/re-imports and
    // work offline, and a library emote is identical to a local one once bound. See ClientConfigSerializer.
    public Map<InputConstants.Key, EmoteHolder> keyBinds = new HashMap<>();
    public EmoteHolder[][] fastMenuEmotes = new EmoteHolder[15][8];

    // Set only when an old UUID-based config is read; resolved to holders once emotes finish loading, then cleared.
    public Map<InputConstants.Key, UUID> legacyKeyBinds;
    public UUID[][] legacyFastMenu;

    //------------------------ Random tweak stuff ------------------------//

    // public final ConfigEntry<Boolean> hideWarningMessage = new ConfigEntry<>("hideWarning", false, expert, true);
}
