package io.github.kosmx.emotes.server.moderation;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.event.EventResult;
import io.github.kosmx.emotes.api.events.server.ServerEmoteEvents;
import io.github.kosmx.emotes.common.CommonData;

import java.util.UUID;

/**
 * Handles server-side emote moderation using the whitelist system.
 * This class integrates with the ServerEmoteEvents to intercept and validate emotes.
 */
public class EmoteModerator {
    private static boolean initialized = false;
    
    /**
     * Initialize the emote moderator by registering event handlers
     */
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        // Register the emote verification handler
        ServerEmoteEvents.EMOTE_VERIFICATION.register(EmoteModerator::verifyEmote);
        
        initialized = true;
        CommonData.LOGGER.info("Emote moderator initialized successfully");
    }
    
    /**
     * Verify if an emote is allowed to be played
     * @param emote The emote being played
     * @param userID The player trying to play the emote
     * @return EventResult.PASS if allowed, EventResult.FAIL if denied
     */
    private static EventResult verifyEmote(Animation emote, UUID userID) {
        EmoteWhitelistManager whitelistManager = EmoteWhitelistManager.getInstance();
        
        // If whitelist is not enabled, allow all emotes
        if (!whitelistManager.isWhitelistEnabled()) {
            return EventResult.PASS;
        }
        
        // Check if the emote is allowed
        boolean isAllowed = whitelistManager.isEmoteAllowed(emote, userID);
        
        if (isAllowed) {
            return EventResult.PASS;
        } else {
            return EventResult.FAIL;
        }
    }
}
