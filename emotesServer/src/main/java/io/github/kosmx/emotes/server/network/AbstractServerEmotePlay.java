package io.github.kosmx.emotes.server.network;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.impl.event.EventResult;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.api.events.server.ServerEmoteEvents;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.logging.Level;

/**
 * This will be used for modded servers
 *
 */
@SuppressWarnings({"ConstantConditions", "rawtypes", "unused"})
public abstract class AbstractServerEmotePlay<P> extends ServerEmoteAPI {
    public AbstractServerEmotePlay(){
        ServerEmoteAPI.INSTANCE = this;
    }

    protected boolean doValidate(){
        return Serializer.getConfig().validateEmote.get();
    }

    protected abstract UUID getUUIDFromPlayer(P player);

    protected abstract P getPlayerFromUUID(UUID player);

    protected abstract long getRuntimePlayerID(P player);

    protected abstract IServerNetworkInstance getPlayerNetworkInstance(P player);

    protected IServerNetworkInstance getPlayerNetworkInstance(UUID player) { //For potential optimization
        return getPlayerNetworkInstance(this.getPlayerFromUUID(player));
    }

    public void receiveMessage(byte[] bytes, P player, INetworkInstance instance) throws IOException{
        receiveMessage(new EmotePacket.Builder().setThreshold(Serializer.getConfig().validThreshold.get()).build().read(ByteBuffer.wrap(bytes)), player, instance);
    }

    public void receiveMessage(NetData data, P player, INetworkInstance instance) throws IOException {
        LoggerService.INSTANCE.log(Level.FINEST, "[emotes server] Received data from: " + getUUIDFromPlayer(player) + " data: " + data);
        switch (data.purpose){
            case STOP:
                stopEmote(player, data);
                break;
            case CONFIG:
                instance.setVersions(data.versions);
                instance.presenceResponse();
                break;
            case STREAM:
                handleStreamEmote(data, player, instance);
                break;
            case UNKNOWN:
            default:
                throw new IOException("Unknown packet task");
        }
    }

    /**
     * Handle received stream message
     * @param data received data
     * @param player sender player
     * @param instance senders network handler
     * @throws IOException probably not
     */
    protected void handleStreamEmote(NetData data, P player, INetworkInstance instance) throws IOException {
        if (!data.valid && doValidate()) {
            EventResult result = ServerEmoteEvents.EMOTE_VERIFICATION.invoker().verify(data.emoteData, getUUIDFromPlayer(player));
            if (result != EventResult.FAIL) {
                EmotePacket.Builder stopMSG = new EmotePacket.Builder().configureToSendStop(data.emoteData.getUuid()).configureTarget(getUUIDFromPlayer(player)).setSizeLimit(0x100000, true);
                if(instance != null)instance.sendMessage(stopMSG, null);
                return;
            }
        }
        IServerNetworkInstance playerInstance = getPlayerNetworkInstance(player);
        if (data.player != null && playerInstance.trackPlayState()) {
            LoggerService.INSTANCE.log(Level.WARNING, "Player: " + player + " does not respect server-side emote tracking. Ignoring repeat");
            return;
        }
        if (playerInstance.getEmoteTracker().isForced()) {
            LoggerService.INSTANCE.log(Level.WARNING, "Player: " + player + " is disobeying force play flag and tried to override it");
        }
        streamEmote(data, player, false, true);
    }

    /**
     * Stream emote
     * @param data   data
     * @param player source player
     */
    protected void streamEmote(NetData data, P player, boolean isForced, boolean isFromPlayer) {
        getPlayerNetworkInstance(player).getEmoteTracker().setPlayedEmote(data.emoteData, isForced);
        ServerEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(data.emoteData, data.tick, getUUIDFromPlayer(player));
        data.isForced = isForced;
        data.player = getUUIDFromPlayer(player);
        data.strictSizeLimit = false;
        sendForEveryoneElse(data, player);
        if (!isFromPlayer) {
            sendForPlayer(data, player, this.getUUIDFromPlayer(player));
        }
    }

    protected void stopEmote(P player, @Nullable NetData originalMessage) {
        Pair<KeyframeAnimation, Integer> emote = getPlayerNetworkInstance(player).getEmoteTracker().getPlayedEmote();
        getPlayerNetworkInstance(player).getEmoteTracker().setPlayedEmote(null, false);
        if (emote != null) {
            ServerEmoteEvents.EMOTE_STOP_BY_USER.invoker().onStopEmote(emote.getLeft().getUuid(), getUUIDFromPlayer(player));
            NetData data = new EmotePacket.Builder().configureToSendStop(emote.getLeft().getUuid(), getUUIDFromPlayer(player)).build().data;

            sendForEveryoneElse(data, player);
            if (originalMessage == null) { //If the stop is not from the player, server needs to notify the player too
                data.isForced = true;
                sendForPlayer(data, player, getUUIDFromPlayer(player));
            }
        }
    }

    public void playerStartTracking(P tracked, P tracker) {
        if (tracked == null || tracker == null) return;
        Pair<KeyframeAnimation, Integer> playedEmote = getPlayerNetworkInstance(tracked).getEmoteTracker().getPlayedEmote();
        if (playedEmote != null) {
            sendForPlayer(new EmotePacket.Builder().configureToStreamEmote(playedEmote.getLeft()).configureEmoteTick(playedEmote.getRight()).configureTarget(getUUIDFromPlayer(tracked)).build().data, tracked, getUUIDFromPlayer(tracker));
        }
    }

    @Override
    protected void setPlayerPlayingEmoteImpl(UUID player, @Nullable KeyframeAnimation emoteData, int tick, boolean isForced) {
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
    protected Pair<KeyframeAnimation, Integer> getPlayedEmoteImpl(UUID player) {
        return getPlayerNetworkInstance(getPlayerFromUUID(player)).getEmoteTracker().getPlayedEmote();
    }

    @Override
    protected boolean isForcedEmoteImpl(UUID player) {
        return getPlayerNetworkInstance(player).getEmoteTracker().isForced();
    }

    @Deprecated
    public void presenceResponse(AbstractNetworkInstance instance, boolean trackPlayState) {
        try {
            instance.sendMessage(getS2CConfigPacket(trackPlayState), null);
        } catch(IOException e) {
            LoggerService.INSTANCE.log(Level.SEVERE, "Failed to send config to client!", e);
        }
        if(instance.getRemoteVersions().getOrDefault((byte)11, (byte)0) >= 0) {
            for (ByteBuffer emote : UniversalEmoteSerializer.preparePackets(instance.getRemoteVersions()).toList()) {
                try{
                    instance.sendMessage(emote, null);
                } catch (Throwable e){
                    LoggerService.INSTANCE.log(Level.WARNING, "Failed to send save emote message", e);
                }
            }
        }
    }

    public EmotePacket.Builder getS2CConfigPacket(boolean trackPlayState) {
        NetData configData = new EmotePacket.Builder().configureToConfigExchange(true).build().data;
        if (trackPlayState) {
            configData.versions.put((byte)0x80, (byte)0x01);
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
     * Send message to target. If target see player the message will be sent
     * @param data message
     * @param player around player
     * @param target target player
     */
    protected abstract void sendForPlayerInRange(NetData data, P player, UUID target);

    /**
     * Send a message to target. This will send a message even if target doesn't see player
     * @param data message
     * @param player player for the ServerWorld information
     * @param target target entity
     */
    protected abstract void sendForPlayer(NetData data, P player, UUID target);

    /**
     * This is **NOT** for API usage,
     * internal purpose only
     * @return this
     */
    public static AbstractServerEmotePlay getInstance() {
        return (AbstractServerEmotePlay) ServerEmoteAPI.INSTANCE;
    }
}
