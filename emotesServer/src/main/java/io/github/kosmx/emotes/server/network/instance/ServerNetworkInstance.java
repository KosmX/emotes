package io.github.kosmx.emotes.server.network.instance;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public abstract class ServerNetworkInstance implements INetworkInstance {
    private final ConfigNetworkInstance configInstance;

    // Read from other connections' threads, not only from the one playing the emote
    private volatile Animation currentEmote = null;
    private volatile Instant startTime = null;
    private volatile boolean isForced = false;

    protected ServerNetworkInstance(ConfigNetworkInstance configInstance) {
        this.configInstance = configInstance;
    }

    /**
     * Set the currently played emote.
     * @param data Emote, null if stop playing
     */
    public void setPlayedEmote(@Nullable Animation data, boolean isForced) {
        if (data == null) {
            this.currentEmote = null;
            this.startTime = null;
            this.isForced = false;
        } else {
            // startTime first: a reader that sees currentEmote must see the time it started at
            this.startTime = Instant.now();
            this.isForced = isForced;
            this.currentEmote = data;
        }
    }

    /**
     * Is the currently played emote forced
     * Returns false if not playing emote
     * a.k.a. disallow the user play a different emote
     * @return true if forced, false if not playing any emote.
     */
    public boolean isForced() {
        if (getPlayedEmote() != null) {
            return isForced;
        } else return false;
    }

    /**
     * Get the currently played emote and the tick time
     * @return null if not playing emote
     */
    @Nullable
    public Pair<Animation, Float> getPlayedEmote() {
        // Read both fields once: another thread may stop the emote between the two reads
        Animation emote = this.currentEmote;
        Instant started = this.startTime;
        if (emote == null || started == null) return null;

        float tick = Duration.between(started, Instant.now()).toMillis() / 50F;
        if (!emote.isPlayingAt(tick)) {
            setPlayedEmote(null, false);
            return null;
        }
        return Pair.of(emote, tick);
    }

    public abstract UUID getUUID();

    @Deprecated
    public void presenceResponse() {
        sendMessage(createConfigurationPacket(isTrackingPlayState()), false);
        if (getVersions().getOrDefault(PacketConfig.HEADER_PACKET, (byte)0) >= 0) {
            UniversalEmoteSerializer.preparePackets().forEach(buffer ->
                    sendMessage(buffer, true)
            );
        }
    }

    @Override
    public Map<Byte, Byte> getVersions() {
        return this.configInstance.versions();
    }

    @Override
    public void setVersions(Map<Byte, Byte> map) {
        this.configInstance.setVersions(map); // routed through ConfigNetworkInstance to stay safe on the immutable avatar config
    }

    @Override
    public boolean isTrackingPlayState() {
        return true;
    }
}
