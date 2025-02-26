package io.github.kosmx.emotes.api.events.server;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Pair;

import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public abstract class ServerEmoteAPI {

    /**
     * Set the player to play emote.
     * Supply with null to stop played emote
     * However this is not recommended for verification. {@link ServerEmoteEvents#EMOTE_VERIFICATION} is used for that
     * @param emote the new emote
     */
    public static void setPlayerPlayingEmote(UUID player, @Nullable KeyframeAnimation emote) {
        ServerEmoteAPI.setPlayerPlayingEmote(player, emote, 0);
    }

    /**
     * Set the player to play emote.
     * Supply with null to stop played emote
     * However this is not recommended for verification. {@link ServerEmoteEvents#EMOTE_VERIFICATION} is used for that
     * @param emote the new emote
     * @param tick First tick
     */
    public static void setPlayerPlayingEmote(UUID player, @Nullable KeyframeAnimation emote, int tick) {
        INSTANCE.setPlayerPlayingEmoteImpl(player, emote, tick, false);
    }

    /**
     * Set the player to FORCE play emote.
     * Forced emotes can only be stopped by a plugin, or by ending the emote.
     * @param emote the new emote
     */
    public static void forcePlayEmote(UUID player, @Nullable KeyframeAnimation emote) {
        ServerEmoteAPI.forcePlayEmote(player, emote, 0);
    }

    /**
     * Set the player to FORCE play emote.
     * Forced emotes can only be stopped by a plugin, or by ending the emote.
     * @param emote the new emote
     * @param tick First tick
     */
    public static void forcePlayEmote(UUID player, @Nullable KeyframeAnimation emote, int tick) {
        INSTANCE.setPlayerPlayingEmoteImpl(player, emote, tick, true);
    }

    /**
     * Set the player to play emote.
     * @param player whom to play
     * @param emote animation, <code>null</code> to stop playing.
     * @param forced can they stop
     */
    public static void playEmote(UUID player, @Nullable KeyframeAnimation emote, boolean forced) {
        ServerEmoteAPI.playEmote(player, emote, 0, forced);
    }

    /**
     * Set the player to play emote.
     * @param player whom to play
     * @param emote animation, <code>null</code> to stop playing.
     * @param tick First tick
     * @param forced can they stop
     */
    public static void playEmote(UUID player, @Nullable KeyframeAnimation emote, int tick, boolean forced) {
        INSTANCE.setPlayerPlayingEmoteImpl(player, emote, tick, forced);
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
     * Check if the player is forced to play an emote
     * @param player who
     * @return forced
     */
    public static boolean isForcedEmote(UUID player) {
        return INSTANCE.isForcedEmoteImpl(player);
    }

    // ---- IMPLEMENTATION ---- //

    protected static ServerEmoteAPI INSTANCE;

    protected abstract void setPlayerPlayingEmoteImpl(UUID player, @Nullable KeyframeAnimation KeyframeAnimation, int tick, boolean isForced);
    protected abstract Pair<KeyframeAnimation, Integer> getPlayedEmoteImpl(UUID player);

    protected abstract boolean isForcedEmoteImpl(UUID player);

}
