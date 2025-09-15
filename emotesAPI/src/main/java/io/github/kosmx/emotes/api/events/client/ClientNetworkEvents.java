package io.github.kosmx.emotes.api.events.client;

import com.zigythebird.playeranimcore.event.Event;
import io.github.kosmx.emotes.common.network.EmotePacket;

/**
 * Network-related events on the client
 * Can be used for compatibility with replaymod or flashback
 */
public class ClientNetworkEvents {
    /**
     * Used to manipulate the packet before sending it
     */
    public static final Event<PacketSendEvent> PACKET_SEND = new Event<>(listeners -> packet -> {
        for (PacketSendEvent listener : listeners) {
            listener.onPacketSend(packet);
        }
    });

    @FunctionalInterface
    public interface PacketSendEvent {
        /**
         * Used to manipulate the packet before sending it
         * @param packet Emote packet
         */
        void onPacketSend(EmotePacket.Builder packet);
    }
}
