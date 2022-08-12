package io.github.kosmx.emotes.server.network;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.impl.event.EventResult;
import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.core.util.UUIDMap;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.api.events.server.ServerEmoteEvents;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.common.tools.BiMap;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.geyser.EmoteMappings;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * This will be used for modded servers
 *
 */
@SuppressWarnings({"ConstantConditions", "rawtypes", "unused"})
public abstract class AbstractServerEmotePlay<P> extends ServerEmoteAPI {
    protected EmoteMappings bedrockEmoteMap = new EmoteMappings(new BiMap<>());

    //private AbstractServerEmotePlay instance;


    public AbstractServerEmotePlay(){
        try {
            initMappings(EmoteInstance.instance.getConfigPath());
            ServerEmoteAPI.INSTANCE = this;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void initMappings(Path configPath) throws IOException{
        Path filePath = configPath.resolveSibling("emotecraft_emote_map.json");
        if(filePath.toFile().isFile()){
            BufferedReader reader = Files.newBufferedReader(filePath);
            try {
                this.bedrockEmoteMap = new EmoteMappings(Serializer.serializer.fromJson(reader, new TypeToken<BiMap<UUID, UUID>>() {}.getType()));
            }catch (JsonParseException e){
                e.printStackTrace();
            }
            reader.close();
        }
        else {
            BiMap<UUID, UUID> example = new BiMap<>();
            example.put(new UUID(0x0011223344556677L, 0x8899aabbccddeeffL), new UUID(0xffeeddccbbaa9988L, 0x7766554433221100L));
            BufferedWriter writer = Files.newBufferedWriter(filePath);
            Serializer.serializer.toJson(example, new TypeToken<BiMap<UUID, UUID>>() {}.getType(), writer);
            writer.close();
        }
    }

    protected boolean doValidate(){
        return EmoteInstance.config.validateEmote.get();
    }

    protected abstract UUID getUUIDFromPlayer(P player);

    protected abstract P getPlayerFromUUID(UUID player);

    protected abstract long getRuntimePlayerID(P player);

    protected abstract IServerNetworkInstance getPlayerNetworkInstance(P player);

    protected IServerNetworkInstance getPlayerNetworkInstance(UUID player) { //For potential optimization
        return getPlayerNetworkInstance(this.getPlayerFromUUID(player));
    }

    public void receiveMessage(byte[] bytes, P player, INetworkInstance instance) throws IOException{
        receiveMessage(new EmotePacket.Builder().setThreshold(EmoteInstance.config.validThreshold.get()).build().read(ByteBuffer.wrap(bytes)), player, instance);
    }

    public void receiveMessage(NetData data, P player, INetworkInstance instance) throws IOException {
        EmoteInstance.instance.getLogger().log(Level.FINEST, "[emotes server] Received data from: " + getUUIDFromPlayer(player) + " data: " + data);
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
     * Receive emote from GeyserMC
     * @param player player
     * @param emotePacket BE emote uuid
     */
    public void receiveBEEmote(P player, GeyserEmotePacket emotePacket) throws IOException {
        UUID javaEmote = bedrockEmoteMap.getJavaEmote(emotePacket.getEmoteID());
        if(javaEmote != null && UniversalEmoteSerializer.getEmote(javaEmote) != null){
            NetData data = new NetData();
            data.emoteData = UniversalEmoteSerializer.getEmote(javaEmote);
            data.purpose = PacketTask.STREAM;
            handleStreamEmote(data, player, null);
        }
        else sendForEveryoneElse(emotePacket, player);
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
                EmotePacket.Builder stopMSG = new EmotePacket.Builder().configureToSendStop(data.emoteData.getUuid()).configureTarget(getUUIDFromPlayer(player)).setSizeLimit(0x100000);
                if(instance != null)instance.sendMessage(stopMSG, null);
                return;
            }
        }
        if (data.player != null) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Player: " + player + " does not respect server-side emote tracking. Ignoring repeat", true);
            return;
        }
        if (getPlayerNetworkInstance(player).getEmoteTracker().isForced()) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Player: " + player + " is disobeying force play flag and tried to override it");
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
        ServerEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(data.emoteData, getUUIDFromPlayer(player));
        data.isForced = isForced;
        data.player = getUUIDFromPlayer(player);
        UUID bedrockEmoteID = bedrockEmoteMap.getBeEmote(data.emoteData.getUuid());
        GeyserEmotePacket geyserEmotePacket = null;
        if(bedrockEmoteID != null){
            geyserEmotePacket = new GeyserEmotePacket();
            geyserEmotePacket.setEmoteID(bedrockEmoteID);
            geyserEmotePacket.setRuntimeEntityID(getRuntimePlayerID(player));
        }
        sendForEveryoneElse(data, geyserEmotePacket, player);
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

            sendForEveryoneElse(data, null, player);
            if (originalMessage == null) { //If the stop is not from the player, server needs to notify the player too
                data.isForced = true;
                sendForPlayer(data, player, getUUIDFromPlayer(player));
            }
        }
    }

    public void playerEntersInvalidPose(P player) {
        if (!getPlayerNetworkInstance(player).getEmoteTracker().isForced()) {
            stopEmote(player, null);
        }
    }

    public void receiveGeyserMessage(P player, byte[] data){
        try {
            GeyserEmotePacket packet = new GeyserEmotePacket();
            packet.read(data);
            packet.setRuntimeEntityID(getRuntimePlayerID(player));
            receiveBEEmote(player, packet);
        }catch (Throwable t){
            t.printStackTrace();
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
    protected void setPlayerPlayingEmoteImpl(UUID player, @Nullable KeyframeAnimation emoteData, boolean isForced) {
        if (emoteData != null) {
            streamEmote(new EmotePacket.Builder().configureToStreamEmote(emoteData).build().data, getPlayerFromUUID(player), isForced, false);
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

    protected abstract void sendForEveryoneElse(GeyserEmotePacket packet, P player);

    /**
     * Send the message to everyone, except for the player
     * @param data message
     * @param emotePacket GeyserMC emote packet for Geyser users ;D
     * @param player send around this player
     */
    protected abstract void sendForEveryoneElse(NetData data, @Nullable GeyserEmotePacket emotePacket, P player);

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

    @Override
    protected HashMap<UUID, KeyframeAnimation> getLoadedEmotesImpl() {
        HashMap<UUID, KeyframeAnimation> map = new UUIDMap<>();
        map.putAll(UniversalEmoteSerializer.serverEmotes);
        map.putAll(UniversalEmoteSerializer.hiddenServerEmotes);
        return map;
    }

    @Override
    protected UUIDMap<KeyframeAnimation> getPublicEmotesImpl() {
        return UniversalEmoteSerializer.serverEmotes;
    }

    @Override
    protected UUIDMap<KeyframeAnimation> getHiddenEmotesImpl() {
        return UniversalEmoteSerializer.hiddenServerEmotes;
    }

    @Override
    protected List<KeyframeAnimation> deserializeEmoteImpl(InputStream inputStream, @Nullable String quarkName, String format) {
        return UniversalEmoteSerializer.readData(inputStream, quarkName, format);
    }

    @Override
    protected KeyframeAnimation getEmoteImpl(UUID emoteID) {
        return UniversalEmoteSerializer.getEmote(emoteID);
    }
}
