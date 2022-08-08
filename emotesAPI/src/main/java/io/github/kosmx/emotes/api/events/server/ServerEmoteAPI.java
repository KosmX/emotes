package io.github.kosmx.emotes.api.events.server;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.core.util.UUIDMap;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class ServerEmoteAPI {

    /**
     * Set the player to play emote.
     * Supply with null to stop played emote
     * However this is not recommended for verification. {@link ServerEmoteEvents#EMOTE_VERIFICATION} is used for that
     * @param emote the new emote
     */
    public static void setPlayerPlayingEmote(UUID player, @Nullable KeyframeAnimation emote) {
        INSTANCE.setPlayerPlayingEmoteImpl(player, emote, false);
    }

    /**
     * Set the player to FORCE play emote.
     * Forced emotes can only be stopped by a plugin, or by ending the emote.
     * @param emote the new emote
     */
    public static void forcePlayEmote(UUID player, @Nullable KeyframeAnimation emote) {
        INSTANCE.setPlayerPlayingEmoteImpl(player, emote, true);
    }

    /**
     * Get the played emote and the time for the player
     * @param player questionable player
     * @return Emote and time, NULL if not playing
     */
    @Nullable
    public static Pair<KeyframeAnimation, Integer> getPlayedEmote(UUID player) {
        return INSTANCE.getPlayedEmoteImpl(player);
    }


    /**
     * Returns a copy of the list of all loaded emotes
     * @return all server-side loaded emotes
     */
    public static HashMap<UUID, KeyframeAnimation> getLoadedEmotes() {
        return INSTANCE.getLoadedEmotesImpl();
    }

    /**
     *
     * @return The server-side hidden but loaded emotes. You can modify this list.
     */
    public static UUIDMap<KeyframeAnimation> getHiddenEmotes() {
        return INSTANCE.getHiddenEmotesImpl();
    }


    /**
     * Get emote from input stream
     * @param inputStream Emote data input stream
     * @param quarkName   If it is a quark emote, default name is required.
     * @param format      Format extension string. "What file extension would it have"
     *                   `emotecraft`   : Emotecraft binary format
     *                   `json`         : Emotecraft or Geckolib JSON format
     *                   `emote`        : Quark emote format UNSAFE
     * @return The serialized emotes, GeckoLib data can contain multiple emotes in one file.
     */
    public static List<KeyframeAnimation> unserializeEmote(InputStream inputStream, @Nullable String quarkName, String format) {
        return INSTANCE.unserializeEmoteImpl(inputStream, quarkName, format);
    }

    /**
     * Get the emote by its UUID
     * @param emoteID Emotes UUID
     * @return Emote or null if no such emote
     */
    @Nullable
    public static KeyframeAnimation getEmote(UUID emoteID) {
        return INSTANCE.getEmoteImpl(emoteID);
    }

    // ---- IMPLEMENTATION ---- //

    protected static ServerEmoteAPI INSTANCE;

    protected abstract void setPlayerPlayingEmoteImpl(UUID player, @Nullable KeyframeAnimation KeyframeAnimation, boolean isForced);
    protected abstract Pair<KeyframeAnimation, Integer> getPlayedEmoteImpl(UUID player);

    protected abstract HashMap<UUID, KeyframeAnimation> getLoadedEmotesImpl();

    protected abstract UUIDMap<KeyframeAnimation> getHiddenEmotesImpl();

    protected abstract List<KeyframeAnimation> unserializeEmoteImpl(InputStream inputStream, @Nullable String quarkName, String format);

    protected abstract KeyframeAnimation getEmoteImpl(UUID emoteID);

}
