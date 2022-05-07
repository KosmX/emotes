package io.github.kosmx.playerAnim;

import io.github.kosmx.playerAnim.layered.AnimationStack;

import javax.annotation.Nullable;
import java.util.UUID;

public abstract class AnimationAPI {

    /**
     * Get the animation stack for a player entity on the client.
     * @param playerID the players UUID.
     * @return The animation stack, null if no such player found
     */
    @Nullable
    public static AnimationStack getPlayerAnimLayer(UUID playerID) {
        return instance().getPlayerAnimLayerImpl(playerID);
    }




    // ---IMPLEMENTATION--- ///

    protected static AnimationAPI INSTANCE;

    private static AnimationAPI instance() {
        if (INSTANCE == null) throw new RuntimeException("AnimationAPI is uninitialized");
        return INSTANCE;
    }

    protected abstract AnimationStack getPlayerAnimLayerImpl(UUID playerID);

}
