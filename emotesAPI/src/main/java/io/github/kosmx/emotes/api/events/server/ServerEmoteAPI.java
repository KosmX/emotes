package io.github.kosmx.emotes.api.events.server;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.api.services.IEmotecraftService;
import io.github.kosmx.emotes.common.tools.ServiceLoaderUtil;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public abstract class ServerEmoteAPI implements IEmotecraftService {

    /**
     * Set the player to play emote.
     * Supply with null to stop played emote
     * However this is not recommended for verification. {@link ServerEmoteEvents#EMOTE_VERIFICATION} is used for that
     * @param emote the new emote
     */
    public static void setPlayerPlayingEmote(UUID player, @Nullable Animation emote) {
        ServerEmoteAPI.setPlayerPlayingEmote(player, emote, 0);
    }

    /**
     * Set the player to play emote.
     * Supply with null to stop played emote
     * However this is not recommended for verification. {@link ServerEmoteEvents#EMOTE_VERIFICATION} is used for that
     * @param emote the new emote
     * @param tick First tick
     */
    public static void setPlayerPlayingEmote(UUID player, @Nullable Animation emote, float tick) {
        INSTANCE.setPlayerPlayingEmoteImpl(player, emote, tick, false);
    }

    /**
     * Set the player to FORCE play emote.
     * Forced emotes can only be stopped by a plugin, or by ending the emote.
     * @param emote the new emote
     */
    public static void forcePlayEmote(UUID player, @Nullable Animation emote) {
        ServerEmoteAPI.forcePlayEmote(player, emote, 0);
    }

    /**
     * Set the player to FORCE play emote.
     * Forced emotes can only be stopped by a plugin, or by ending the emote.
     * @param emote the new emote
     * @param tick First tick
     */
    public static void forcePlayEmote(UUID player, @Nullable Animation emote, float tick) {
        INSTANCE.setPlayerPlayingEmoteImpl(player, emote, tick, true);
    }

    /**
     * Set the player to play emote.
     * @param player whom to play
     * @param emote animation, <code>null</code> to stop playing.
     * @param forced can they stop
     */
    public static void playEmote(UUID player, @Nullable Animation emote, boolean forced) {
        ServerEmoteAPI.playEmote(player, emote, 0, forced);
    }

    /**
     * Set the player to play emote.
     * @param player whom to play
     * @param emote animation, <code>null</code> to stop playing.
     * @param tick First tick
     * @param forced can they stop
     */
    public static void playEmote(UUID player, @Nullable Animation emote, float tick, boolean forced) {
        INSTANCE.setPlayerPlayingEmoteImpl(player, emote, tick, forced);
    }

    /**
     * Get the played emote and the time for the player
     * @param player questionable player
     * @return Emote and time, NULL if not playing
     */
    @Nullable
    public static Pair<Animation, Float> getPlayedEmote(UUID player) {
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

    protected static final ServerEmoteAPI INSTANCE = ServiceLoaderUtil.loadService(ServerEmoteAPI.class);

    protected abstract void setPlayerPlayingEmoteImpl(UUID player, @Nullable Animation KeyframeAnimation, float tick, boolean isForced);
    protected abstract Pair<Animation, Float> getPlayedEmoteImpl(UUID player);

    protected abstract boolean isForcedEmoteImpl(UUID player);

    @Override
    public boolean isActive() {
        return true;
    }
}
