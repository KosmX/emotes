package io.github.kosmx.emotes.api.events.server;

import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.common.emote.EmoteData;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class ServerEmoteAPI {

    /**
     * Set the player to play emote.
     * Supply with null to stop played emote
     * However this is not recommended for verification. {@link ServerEmoteEvents#EMOTE_VERIFICATION} is used for that
     * @param emote the new emote
     */
    public static void setPlayerPlayingEmote(UUID player, @Nullable EmoteData emote) {
        INSTANCE.setPlayerPlayingEmoteImpl(player, emote);
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

    // ---- IMPLEMENTATION ---- //

    protected static ServerEmoteAPI INSTANCE;

    protected abstract void setPlayerPlayingEmoteImpl(UUID player, @Nullable EmoteData emoteData);
    protected abstract Pair<EmoteData, Integer> getPlayedEmoteImpl(UUID player);
}
