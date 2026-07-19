package io.github.kosmx.emotes.server.network.instance;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public record ConfigNetworkInstance(Map<Byte, Byte> versions) {
    public static final ConfigNetworkInstance IMMUTABLE = new ConfigNetworkInstance(Collections.emptyMap());

    public ConfigNetworkInstance() {
        this(new HashMap<>(EmotePacket.defaultVersions));
    }

    public void receiveConfigMessage(EmotePacket packet, Consumer<EmotePacket> consumer) throws IOException {
        NetData message = packet.data;
        if (message.purpose != PacketTask.CONFIG) throw new IOException("Wrong packet type for config task");
        setVersions(message.versions);

        UniversalEmoteSerializer.preparePackets()
                .map(builder -> builder.setVersion(versions()).build())
                .forEach(consumer);
    }

    /**
     * Default client-side version config,
     * Please call super if you override it.
     * @param map version/config map
     */
    public void setVersions(Map<Byte, Byte> map) {
        this.versions.clear();
        this.versions.putAll(map);
    }
}
