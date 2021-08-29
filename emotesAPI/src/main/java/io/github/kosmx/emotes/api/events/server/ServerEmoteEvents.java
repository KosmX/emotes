package io.github.kosmx.emotes.api.events.server;

import io.github.kosmx.emotes.api.events.impl.Event;
import io.github.kosmx.emotes.api.events.impl.EventResult;
import io.github.kosmx.emotes.common.emote.EmoteData;

import java.util.UUID;

public final class ServerEmoteEvents {

    /**
     * Server verify emote if it can be streamed, or it has to be cancelled.
     * Return with {@link EventResult#PASS} if you allow it and {@link EventResult#FAIL} if you deny it.
     */
    public static final Event<EmoteVerifier> EMOTE_VERIFICATION = new Event<>(EmoteVerifier.class, listeners -> (emote, userID) -> {
        for(EmoteVerifier listener: listeners){
            EventResult result = listener.verify(emote, userID);
            if(result == EventResult.FAIL || result == EventResult.CONSUME){
                return result;
            }
        }
        return EventResult.PASS;
    });

    public interface EmoteVerifier{

        /**
         * Verify an emote
         * possible results:
         *
         *  {@link EventResult#FAIL}:
         *  verification failed, won't allow user to play the emote
         *
         *  {@link EventResult#PASS} or {@link EventResult#SUCCESS}:
         *  this callback allows the emote to be played. NOTE: other callbacks can refuse it
         *
         *  {@link EventResult#CONSUME}:
         *  Emote will be allowed BUT no other callbacks will be invoked. DO NOT DO THIS
         *  I don't even know, why do I allow this
         *
         * @param emote
         * @param userID
         * @return
         */
        EventResult verify(EmoteData emote, UUID userID);
    }
}
