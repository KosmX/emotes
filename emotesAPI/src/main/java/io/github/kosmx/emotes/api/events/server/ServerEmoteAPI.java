package io.github.kosmx.emotes.api.events.server;

import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.tools.UUIDMap;

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
    public static void setPlayerPlayingEmote(UUID player, @Nullable EmoteData emote) {
        INSTANCE.setPlayerPlayingEmoteImpl(player, emote, false);
    }

    /**
     * Set the player to FORCE play emote.
     * Forced emotes can only be stopped by a plugin, or by ending the emote.
     * @param emote the new emote
     */
    public static void forcePlayEmote(UUID player, @Nullable EmoteData emote) {
        INSTANCE.setPlayerPlayingEmoteImpl(player, emote, true);
    }

    /**
     * Get the played emote and the time for the player
     * @param player questionable player
     * @return Emote and time, NULL if not playing
     */
    @Nullable
    public static Pair<EmoteData, Integer> getPlayedEmote(UUID player) {
        return INSTANCE.getPlayedEmoteImpl(player);
    }


    /**
     * Returns a copy of the list of all loaded emotes
     * @return all server-side loaded emotes
     */
    public static HashMap<UUID, EmoteData> getLoadedEmotes() {
        return INSTANCE.getLoadedEmotesImpl();
    }

    /**
     *
     * @return The server-side hidden but loaded emotes. You can modify this list.
     */
    public static UUIDMap<EmoteData> getHiddenEmotes() {
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
    public static List<EmoteData> unserializeEmote(InputStream inputStream, @Nullable String quarkName, String format) {
        return INSTANCE.unserializeEmoteImpl(inputStream, quarkName, format);
    }

    /**
     * Get the emote by its UUID
     * @param emoteID Emotes UUID
     * @return Emote or null if no such emote
     */
    @Nullable
    public static EmoteData getEmote(UUID emoteID) {
        return INSTANCE.getEmoteImpl(emoteID);
    }

    // ---- IMPLEMENTATION ---- //

    protected static ServerEmoteAPI INSTANCE;

    protected abstract void setPlayerPlayingEmoteImpl(UUID player, @Nullable EmoteData emoteData, boolean isForced);
    protected abstract Pair<EmoteData, Integer> getPlayedEmoteImpl(UUID player);

    protected abstract HashMap<UUID, EmoteData> getLoadedEmotesImpl();

    protected abstract UUIDMap<EmoteData> getHiddenEmotesImpl();

    protected abstract List<EmoteData> unserializeEmoteImpl(InputStream inputStream, @Nullable String quarkName, String format);

    protected abstract EmoteData getEmoteImpl(UUID emoteID);

}
