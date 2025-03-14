package io.github.kosmx.emotes.api.events.client;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;

import io.github.kosmx.emotes.api.services.IEmotecraftService;
import io.github.kosmx.emotes.common.tools.ServiceLoaderUtil;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

public abstract class ClientEmoteAPI implements IEmotecraftService {
    /**
     * Stop play an emote.
     */
    public static boolean stopEmote() {
        return ClientEmoteAPI.playEmote(null);
    }

    /**
     * Start playing an emote.
     * @param animation animation, <code>null</code> to stop playing.
     * @return          Can the emote be played: this doesn't check server-side verification
     */
    public static boolean playEmote(@Nullable KeyframeAnimation animation) {
        return ClientEmoteAPI.playEmote(animation, 0);
    }

    /**
     * Start playing an emote.
     * @param animation animation, <code>null</code> to stop playing.
     * @param tick First tick
     * @return          Can the emote be played: this doesn't check server-side verification
     */
    public static boolean playEmote(@Nullable KeyframeAnimation animation, int tick) {
        return INSTANCE.playEmoteImpl(animation, tick);
    }

    /**
     * A list of client-side active emotes.
     * You can not modify the list.
     * @return Client-side active emotes
     */
    public static Collection<KeyframeAnimation> clientEmoteList() {
        return INSTANCE.clientEmoteListImpl();
    }

    // ---- IMPLEMENTATION ---- //

    protected static final ClientEmoteAPI INSTANCE = ServiceLoaderUtil.loadService(ClientEmoteAPI.class);

    protected abstract boolean playEmoteImpl(KeyframeAnimation animation, int tick);

    protected abstract Collection<KeyframeAnimation> clientEmoteListImpl();

    @Override
    public boolean isActive() {
        return true;
    }
}
