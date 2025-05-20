package io.github.kosmx.emotes.api.events.server;

import io.github.kosmx.emotes.api.PlayingAnimationData;
import io.github.kosmx.emotes.api.services.IEmotecraftService;
import io.github.kosmx.emotes.common.tools.ServiceLoaderUtil;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public abstract class ServerEmoteAPI implements IEmotecraftService {
    /**
     * Set the player to play emote.
     * @param player whom to play
     * @param data animation data, <code>null</code> to stop playing.
     */
    public static void playEmote(UUID player, @Nullable PlayingAnimationData data) {
        INSTANCE.setPlayerPlayingEmoteImpl(player, data);
    }

    /**
     * Get the played emote and the time for the player
     * @param player questionable player
     * @return Emote and time, NULL if not playing
     */
    @Nullable
    public static PlayingAnimationData getPlayedEmote(UUID player) {
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

    protected abstract void setPlayerPlayingEmoteImpl(UUID player, @Nullable PlayingAnimationData  data);
    protected abstract PlayingAnimationData getPlayedEmoteImpl(UUID player);

    protected abstract boolean isForcedEmoteImpl(UUID player);

    @Override
    public boolean isActive() {
        return true;
    }
}
