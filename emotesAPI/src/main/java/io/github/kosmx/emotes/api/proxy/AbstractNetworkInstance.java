package io.github.kosmx.emotes.api.proxy;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Implement this if you want to act as a proxy for EmoteX
 * This has most of the functions implemented as you might want, but you can override any.
 */
public abstract class AbstractNetworkInstance implements INetworkInstance {
    private final HashMap<Byte, Byte> versions = new HashMap<>(EmotePacket.defaultVersions);

    /**
     * Default client-side version config,
     * Please call super if you override it.
     * @param map version/config map
     */
    @Override
    public void setVersions(Map<Byte, Byte> map) {
        this.versions.clear();
        this.versions.putAll(map);
    }

    /**
     * see {@link INetworkInstance#getVersions()}
     * it is just a default implementation
     */
    @Override
    public Map<Byte, Byte> getVersions() {
        return this.versions;
    }

    @Override
    public boolean isTrackingPlayState() {
        return this.versions.containsKey(PacketConfig.SERVER_TRACK_EMOTE_PLAY) &&
                this.versions.get(PacketConfig.SERVER_TRACK_EMOTE_PLAY) != 0;
    }
}
