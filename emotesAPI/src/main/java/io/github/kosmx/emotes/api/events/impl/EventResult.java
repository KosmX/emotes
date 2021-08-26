package io.github.kosmx.emotes.api.events.impl;

public enum EventResult {
    /**
     * You listener did nothing, in won't change the result of the event
     */
    PASS,

    /**
     * Cancel the event and success. see the event's documentation
     */
    SUCCESS,

    /**
     * Event failed, cancel the further processing, see the event's documentation
     */
    FAIL,

    /**
     * Cancel the event, then does nothing. sometimes the same as SUCCESS
     */
    CONSUME
}
