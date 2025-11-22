package io.github.kosmx.emotes.server.network;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.event.EventResult;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.api.events.server.ServerEmoteEvents;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.UUID;

/**
 * This will be used for modded servers
 */
public abstract class AbstractServerEmotePlay<P extends IServerNetworkInstance> extends ServerEmoteAPI {
    protected boolean doValidate() {
        return Serializer.getConfig().validateEmote.get();
    }

    protected abstract UUID getUUIDFromPlayer(P player);

    protected abstract P getPlayerFromUUID(UUID player);

    public void receiveMessage(ByteBuf bytes, P instance) throws IOException {
        receiveMessage(new EmotePacket(bytes), instance);
    }

    @SuppressWarnings("deprecation")
    public void receiveMessage(EmotePacket packet, P instance) throws IOException {
        CommonData.LOGGER.trace("[emotes server] Received data from: {} data: {}", instance, packet);
        switch (packet.data.purpose){
            case STOP:
                stopEmote(instance, packet.data);
                break;
            case CONFIG:
                instance.setVersions(packet.data.versions);
                instance.presenceResponse();
                break;
            case STREAM:
                handleStreamEmote(packet.data, instance);
                break;
            case UNKNOWN:
            default:
                throw new IOException("Unknown packet task");
        }
    }

    /**
     * Handle received stream message
     * @param data received data
     * @param instance sender player
     * @throws IOException probably not
     */
    @SuppressWarnings("ConstantConditions")
    protected void handleStreamEmote(NetData data, P instance) throws IOException {
        if (!data.valid && doValidate()) {
            EventResult result = ServerEmoteEvents.EMOTE_VERIFICATION.invoker().verify(data.emoteData, getUUIDFromPlayer(instance));
            if (result != EventResult.FAIL) {
                EmotePacket.Builder stopMSG = new EmotePacket.Builder().configureToSendStop(data.emoteData.uuid()).configureTarget(getUUIDFromPlayer(instance)).setSizeLimit(0x100000, true);
                if(instance != null)instance.sendMessage(stopMSG, null);
                return;
            }
        }
        if (data.player != null && instance.trackPlayState()) {
            CommonData.LOGGER.warn("Player {} does not respect server-side emote tracking", instance);
        }
        if (instance.getEmoteTracker().isForced()) {
            CommonData.LOGGER.warn("Player {} is disobeying force play flag and tried to override it", instance);
            return;
        }
        streamEmote(data, instance, false, true);
    }

    /**
     * Stream emote
     * @param data   data
     * @param player source player
     */
    protected void streamEmote(NetData data, P player, boolean isForced, boolean isFromPlayer) {
        player.getEmoteTracker().setPlayedEmote(data.emoteData, isForced);
        ServerEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(data.emoteData, data.tick, getUUIDFromPlayer(player));
        data.isForced = isForced;
        data.player = getUUIDFromPlayer(player);
        data.strictSizeLimit = false;
        sendForEveryoneElse(data, player);
        if (!isFromPlayer) {
            sendForPlayer(data, player, player);
        }
    }

    protected void stopEmote(P player, @Nullable NetData originalMessage) {
        Pair<Animation, Float> emote = player.getEmoteTracker().getPlayedEmote();
        player.getEmoteTracker().setPlayedEmote(null, false);
        if (emote != null) {
            ServerEmoteEvents.EMOTE_STOP_BY_USER.invoker().onStopEmote(emote.left().uuid(), getUUIDFromPlayer(player));
            NetData data = new EmotePacket.Builder().configureToSendStop(emote.left().uuid(), getUUIDFromPlayer(player)).build().data;

            sendForEveryoneElse(data, player);
            if (originalMessage == null) { //If the stop is not from the player, server needs to notify the player too
                data.isForced = true;
                sendForPlayer(data, player, player);
            }
        }
    }

    public void playerStartTracking(P tracked, P tracker) {
        if (tracked == null || tracker == null) return;
        Pair<Animation, Float> playedEmote = tracked.getEmoteTracker().getPlayedEmote();
        if (playedEmote != null) {
            sendForPlayer(new EmotePacket.Builder().configureToStreamEmote(playedEmote.left()).configureEmoteTick(playedEmote.right()).configureTarget(getUUIDFromPlayer(tracked)).build().data, tracked, tracker);
        }
    }

    @Override
    protected void setPlayerPlayingEmoteImpl(UUID player, @Nullable Animation emoteData, float tick, boolean isForced) {
        if (emoteData != null) {
            EmotePacket packet = new EmotePacket.Builder()
                    .configureToStreamEmote(emoteData)
                    .configureEmoteTick(tick)
                    .build();

            streamEmote(packet.data, getPlayerFromUUID(player), isForced, false);
        } else {
            stopEmote(getPlayerFromUUID(player), null);
        }
    }

    @Override
    protected Pair<Animation, Float> getPlayedEmoteImpl(UUID player) {
        return getPlayerFromUUID(player).getEmoteTracker().getPlayedEmote();
    }

    @Override
    protected boolean isForcedEmoteImpl(UUID player) {
        return getPlayerFromUUID(player).getEmoteTracker().isForced();
    }

    @Deprecated
    public void presenceResponse(AbstractNetworkInstance instance, boolean trackPlayState) {
        try {
            instance.sendMessage(getS2CConfigPacket(trackPlayState), null);
        } catch(IOException e) {
            CommonData.LOGGER.error("Failed to send config to client!", e);
        }
        if (instance.getRemoteVersions().getOrDefault(PacketConfig.HEADER_PACKET, (byte)0) >= 0) {
            UniversalEmoteSerializer.preparePackets(instance.getRemoteVersions()).forEach(buffer ->
                    instance.sendMessage(buffer, null)
            );
        }
    }

    public EmotePacket.Builder getS2CConfigPacket(boolean trackPlayState) {
        NetData configData = new EmotePacket.Builder().configureToConfigExchange().build().data;
        if (trackPlayState) {
            configData.versions.put(PacketConfig.SERVER_TRACK_EMOTE_PLAY, (byte)0x01);
        }
        return new EmotePacket.Builder(configData);
    }

    /**
     * Send the message to everyone, except for the player
     * @param data message
     * @param player send around this player
     */
    protected abstract void sendForEveryoneElse(NetData data, P player);

    /**
     * Send a message to target. This will send a message even if target doesn't see player
     * @param data message
     * @param player player for the ServerWorld information
     * @param target target entity
     */
    protected void sendForPlayer(NetData data, P player, P target) {
        if (!target.isActive()) return;
        try {
            EmotePacket.Builder packetBuilder = new EmotePacket.Builder(data.copy());
            packetBuilder.setVersion(target.getRemoteVersions());
            target.sendMessage(packetBuilder, null);
        } catch (Exception e) {
            CommonData.LOGGER.warn("Failed to send packet!", e);
        }
    }

    /**
     * This is **NOT** for API usage,
     * internal purpose only
     * @return this
     */
    @SuppressWarnings("rawtypes")
    public static AbstractServerEmotePlay getInstance() {
        return (AbstractServerEmotePlay) ServerEmoteAPI.INSTANCE;
    }
}
