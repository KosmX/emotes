package io.github.kosmx.emotes.api.events.server;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.impl.event.Event;
import dev.kosmx.playerAnim.core.impl.event.EventResult;

import java.util.UUID;

public final class ServerEmoteEvents {

    /**
     * Server verify emote if it can be streamed, or it has to be cancelled.
     * Return with {@link EventResult#PASS} if you allow it and {@link EventResult#FAIL} if you deny it.
     */
    public static final Event<EmoteVerifier> EMOTE_VERIFICATION = new Event<>(EmoteVerifier.class, listeners -> (emote, userID) -> {
        for (EmoteVerifier listener : listeners) {
            EventResult result = listener.verify(emote, userID);
            if (result == EventResult.FAIL || result == EventResult.CONSUME) {
                return result;
            }
        }
        return EventResult.PASS;
    });

    @FunctionalInterface
    public interface EmoteVerifier {

        /**
         * Verify an emote
         * possible results:
         * <p>
         * {@link EventResult#FAIL}:
         * verification failed, won't allow user to play the emote
         * <p>
         * {@link EventResult#PASS} or {@link EventResult#SUCCESS}:
         * this callback allows the emote to be played. NOTE: other callbacks can refuse it
         * <p>
         * {@link EventResult#CONSUME}:
         * Emote will be allowed BUT no other callbacks will be invoked. DO NOT DO THIS
         * I don't even know, why do I allow this
         *
         * @param emote
         * @param userID
         * @return
         */
        EventResult verify(KeyframeAnimation emote, UUID userID);
    }


    /**
     * Invoked when someone is starting an emote
     * For checking and cancelling, use {@link ServerEmoteEvents#EMOTE_VERIFICATION}
     */
    public static final Event<EmotePlayEvent> EMOTE_PLAY = new Event<>(EmotePlayEvent.class, listeners -> (emote, userID) -> {
        for (EmotePlayEvent listener : listeners) {
            listener.onEmotePlay(emote, userID);
        }
    });

    @FunctionalInterface
    public interface EmotePlayEvent {

        /**
         * Used to create emote play side effects
         * @param emoteData The played emote data
         * @param userID User ID
         */
        void onEmotePlay(KeyframeAnimation emoteData, UUID userID);
    }

    public static final Event<EmoteStopEvent> EMOTE_STOP_BY_USER = new Event<>(EmoteStopEvent.class, listeners -> (emoteID, userID) -> {
        for (EmoteStopEvent listener : listeners) {
            listener.onStopEmote(emoteID, userID);
        }
    });

    @FunctionalInterface
    public interface EmoteStopEvent {

        /**
         * Only used when a user stops an emote-play,
         * Finishing an emote will NOT invoke it
         * @param emoteID the ID of the emote
         * @param userID the ID of the user
         */
        void onStopEmote(UUID emoteID, UUID userID);
    }
}
