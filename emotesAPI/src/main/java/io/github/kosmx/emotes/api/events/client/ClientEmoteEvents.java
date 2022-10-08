package io.github.kosmx.emotes.api.events.client;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.impl.event.Event;
import dev.kosmx.playerAnim.core.impl.event.EventResult;

import java.util.UUID;

/**
 * This API will be used on logical CLIENT SIDE
 * Server-side events are in {@link io.github.kosmx.emotes.api.events.server.ServerEmoteEvents}
 */
public final class ClientEmoteEvents {

    /**
     * Client verify emote if it can be played, or it has to be cancelled.<br>
     * Return with {@link EventResult#PASS} if you allow it and {@link EventResult#FAIL} if you deny it.<br>
     * Invoking this event does not mean the emote will be played even if the event wasn't cancelled.
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
         */
        EventResult verify(KeyframeAnimation emote, UUID userID);
    }


    /**
     * Invoked when someone is starting an emote (can be the main player)
     * For checking and cancelling, use {@link ClientEmoteEvents#EMOTE_VERIFICATION}
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

    /**
     * A player is stopping the played emote by command
     * <p>
     * NOTE: Emote ending won't trigger any events
     */
    public static final Event<EmoteStopEvent> EMOTE_STOP = new Event<>(EmoteStopEvent.class, listeners -> (emote, userID) -> {
        for (EmoteStopEvent listener : listeners) {
            listener.onEmoteStop(emote, userID);
        }
    });


    @FunctionalInterface
    public interface EmoteStopEvent {

        /**
         * Used to create emote stop side effects
         * @param userID User ID
         */
        void onEmoteStop(UUID emoteID, UUID userID);
    }


    /**
     * The client player is stopping its own emote
     * It will trigger only if the command is from the client
     */
    public static final Event<LocalEmoteStopEvent> LOCAL_EMOTE_STOP = new Event<>(LocalEmoteStopEvent.class, listeners -> () -> {
        for (LocalEmoteStopEvent listener : listeners) {
            listener.onEmoteStop();
        }
    });


    @FunctionalInterface
    public interface LocalEmoteStopEvent {

        /**
         * Used to create emote stop side effects
         */
        void onEmoteStop();
    }
}
