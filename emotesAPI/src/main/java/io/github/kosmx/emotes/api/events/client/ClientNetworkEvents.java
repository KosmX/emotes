package io.github.kosmx.emotes.api.events.client;

import com.zigythebird.playeranimcore.event.Event;
import com.zigythebird.playeranimcore.event.EventResult;
import io.github.kosmx.emotes.common.network.EmotePacket;

/**
 * Network-related events on the client
 * Can be used for compatibility with replaymod or flashback
 */
public class ClientNetworkEvents {
    /**
     * Used to manipulate the packet before sending it
     * <p>
     * Return with {@link EventResult#PASS} if you want to continue sending, and {@link EventResult#FAIL} if otherwise
     */
    public static final Event<PacketSendEvent> PACKET_SEND = new Event<>(listeners -> packet -> {
        for (PacketSendEvent listener : listeners) {
            if (listener.onPacketSend(packet) == EventResult.FAIL) {
                return EventResult.FAIL;
            }
        }
        return EventResult.PASS;
    });

    @FunctionalInterface
    public interface PacketSendEvent {
        /**
         * Used to manipulate the packet before sending it
         * @param packet Emote packet
         */
        EventResult onPacketSend(EmotePacket.Builder packet);
    }
}
