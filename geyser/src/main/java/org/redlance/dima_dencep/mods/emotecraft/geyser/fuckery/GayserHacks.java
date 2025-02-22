package org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery;

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.geysermc.geyser.registry.PacketTranslatorRegistry;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.PacketTranslator;
import org.geysermc.mcprotocollib.network.packet.Packet;

import java.io.IOException;

@SuppressWarnings({"unchecked","rawtypes"})
public class GayserHacks {
    public static <T extends BedrockPacket> void addCustomBedrockTranslator(Class<T> packet, ChainedPacketTranslator<T> chained) {
        GayserHacks.addCustomTranslator(Registries.BEDROCK_PACKET_TRANSLATORS, packet, chained);
    }

    public static <T extends Packet> void addCustomJavaTranslator(Class<T> packet, ChainedPacketTranslator<T> chained) {
        GayserHacks.addCustomTranslator(Registries.JAVA_PACKET_TRANSLATORS, packet, chained);
    }

    public static <T> void addCustomTranslator(PacketTranslatorRegistry registry, Class<T> packet, ChainedPacketTranslator<T> chained) {
        PacketTranslator<T> translator = (PacketTranslator<T>) registry.get(packet);
        registry.register(packet, new WrappedTranslator<>(translator, chained));
    }

    private static class WrappedTranslator<T> extends PacketTranslator<T> {
        private final PacketTranslator<T> original;
        private final ChainedPacketTranslator<T> chained;

        private WrappedTranslator(PacketTranslator<T> original, ChainedPacketTranslator<T> chained) {
            this.original = original;
            this.chained = chained;
        }

        @Override
        public void translate(GeyserSession session, T packet) {
            try {
                if (this.chained.translate(session, packet) && this.original != null) {
                    this.original.translate(session, packet);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
