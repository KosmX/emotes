package io.github.kosmx.emotes.server.network;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.api.events.impl.EventResult;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.api.events.server.ServerEmoteEvents;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.tools.BiMap;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.geyser.EmoteMappings;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;

/**
 * This will be used for modded servers
 *
 */
public abstract class AbstractServerEmotePlay<P> extends ServerEmoteAPI {
    protected EmoteMappings bedrockEmoteMap = new EmoteMappings(new BiMap<>());

    //private AbstractServerEmotePlay instance;


    public AbstractServerEmotePlay(){
        try {
            initMappings(EmoteInstance.instance.getGameDirectory().resolve("config"));
            ServerEmoteAPI.INSTANCE = this;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void initMappings(Path configPath) throws IOException{
        Path filePath = configPath.resolve("emotecraft_emote_map.json");
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
        switch (data.purpose){
            case STOP:
                data.player = getUUIDFromPlayer(player);
                sendForEveryoneElse(data, null, player);
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
        streamEmote(data, player);
    }

    /**
     * Stream emote
     * @param data   data
     * @param player source player
     */
    protected void streamEmote(NetData data, P player) {
        getPlayerNetworkInstance(player).getEmoteTracker().setPlayedEmote(data.emoteData);
        data.player = getUUIDFromPlayer(player);
        UUID bedrockEmoteID = bedrockEmoteMap.getBeEmote(data.emoteData.getUuid());
        GeyserEmotePacket geyserEmotePacket = null;
        if(bedrockEmoteID != null){
            geyserEmotePacket = new GeyserEmotePacket();
            geyserEmotePacket.setEmoteID(bedrockEmoteID);
            geyserEmotePacket.setRuntimeEntityID(getRuntimePlayerID(player));
        }
        sendForEveryoneElse(data, geyserEmotePacket, player);
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
        Pair<EmoteData, Integer> playedEmote = getPlayerNetworkInstance(tracked).getEmoteTracker().getPlayedEmote();
        if (playedEmote != null) {
            sendForPlayer(new EmotePacket.Builder().configureToStreamEmote(playedEmote.getLeft()).configureEmoteTick(playedEmote.getRight()).build().data, tracked, getUUIDFromPlayer(tracker));
        }
    }

    @Override
    protected void setPlayerPlayingEmoteImpl(UUID player, @Nullable EmoteData emoteData) {
        streamEmote(new EmotePacket.Builder().configureToStreamEmote(emoteData).build().data, getPlayerFromUUID(player));
    }

    @Override
    protected Pair<EmoteData, Integer> getPlayedEmoteImpl(UUID player) {
        return getPlayerNetworkInstance(getPlayerFromUUID(player)).getEmoteTracker().getPlayedEmote();
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
}
