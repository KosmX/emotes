package io.github.kosmx.emotes.server.network;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.event.EventResult;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.api.events.server.ServerEmoteEvents;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.network.instance.ServerNetworkInstance;

import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * This will be used for modded servers
 */
public abstract class AbstractServerEmotePlay<P extends ServerNetworkInstance> extends ServerEmoteAPI {
    protected boolean doValidate() {
        return Serializer.getConfig().validateEmote.get();
    }

    protected abstract P getPlayerFromUUID(UUID player);

    public void receiveMessage(EmotePacket packet, P instance) throws IOException {
        CommonData.LOGGER.trace("[emotes server] Received data from: {} data: {}", instance, packet);
        switch (packet.data.purpose){
            case STOP:
                stopEmote(instance, packet.data);
                break;
            case CONFIG: // deprecated case
                CommonData.LOGGER.info("The {} does not support the new configuration!", instance);
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
            EventResult result = ServerEmoteEvents.EMOTE_VERIFICATION.invoker().verify(data.emoteData, instance.getUUID());
            if (result != EventResult.FAIL) {
                EmotePacket.Builder stopMSG = new EmotePacket.Builder()
                        .configureToSendStop(data.emoteData.uuid())
                        .configureTarget(instance.getUUID());
                if (instance != null) instance.sendMessage(stopMSG, true);
                return;
            }
        }
        if (data.player != null && instance.isTrackingPlayState()) {
            CommonData.LOGGER.warn("Player {} does not respect server-side emote tracking", instance);
        }
        if (instance.isForced()) {
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
        player.setPlayedEmote(data.emoteData, isForced);
        ServerEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(data.emoteData, data.tick, player.getUUID());
        data.isForced = isForced;
        data.player = player.getUUID();
        data.strictSizeLimit = false;
        sendForTrackedBy(data, player);
        if (!isFromPlayer) {
            player.sendMessage(data, true);
        }
    }

    protected void stopEmote(P player, @Nullable NetData originalMessage) {
        Pair<Animation, Float> emote = player.getPlayedEmote();
        player.setPlayedEmote(null, false);
        if (emote != null) {
            ServerEmoteEvents.EMOTE_STOP_BY_USER.invoker().onStopEmote(emote.left().uuid(), player.getUUID());
            NetData data = new EmotePacket.Builder().configureToSendStop(emote.left().uuid(), player.getUUID()).build().data;

            sendForTrackedBy(data, player);
            if (originalMessage == null) { //If the stop is not from the player, server needs to notify the player too
                data.isForced = true;
                player.sendMessage(data, true);
            }
        }
    }

    public void playerStartTracking(P tracked, P tracker) {
        if (tracked == null || tracker == null) return;
        Pair<Animation, Float> playedEmote = tracked.getPlayedEmote();
        if (playedEmote != null) {
            tracker.sendMessage(new EmotePacket.Builder()
                    .configureToStreamEmote(playedEmote.left())
                    .configureEmoteTick(playedEmote.right())
                    .configureTarget(tracked.getUUID()), true
            );
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
        return getPlayerFromUUID(player).getPlayedEmote();
    }

    @Override
    protected boolean isForcedEmoteImpl(UUID player) {
        return getPlayerFromUUID(player).isForced();
    }

    /**
     * Send the message to everyone, except for the player
     * @param data message
     * @param player send around this player
     */
    protected abstract void sendForTrackedBy(NetData data, P player);
    protected abstract void sendForEveryone(NetData data);

    public void updateClientEmotes(Set<UUID> removedIds) {
        NetData data = new EmotePacket.Builder()
                .configureToRemoveEmote(removedIds)
                .build().data;
        sendForEveryone(data);
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
