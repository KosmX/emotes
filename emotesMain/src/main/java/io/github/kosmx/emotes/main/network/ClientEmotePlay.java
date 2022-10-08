package io.github.kosmx.emotes.main.network;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.impl.event.EventResult;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.api.events.client.ClientEmoteAPI;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ClientEmotePlay extends ClientEmoteAPI {

    /**
     * When the emotePacket arrives earlier than the player entity data
     * I put the emote into a queue.
     */
    //private static final int maxQueueLength = 256;
    private static final HashMap<UUID, QueueEntry> queue = new HashMap<>();

    public static void clientStartLocalEmote(EmoteHolder emoteHolder) {
        clientStartLocalEmote(emoteHolder.getEmote());
    }

    public static boolean clientStartLocalEmote(KeyframeAnimation emote) {
        IEmotePlayerEntity player = EmoteInstance.instance.getClientMethods().getMainPlayer();
        if (player.isForcedEmote()) {
            return false;
        }

        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToStreamEmote(emote, player.emotes_getUUID());
        ClientPacketManager.send(packetBuilder, null);
        ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(emote, player.emotes_getUUID());
        EmoteInstance.instance.getClientMethods().getMainPlayer().playEmote(emote, 0, false);
        return true;
    }

    public static void clientRepeatLocalEmote(KeyframeAnimation emote, int tick, UUID target){
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToStreamEmote(emote, EmoteInstance.instance.getClientMethods().getMainPlayer().emotes_getUUID()).configureEmoteTick(tick);
        ClientPacketManager.send(packetBuilder, target);
    }

    public static boolean clientStopLocalEmote() {
        if (EmoteInstance.instance.getClientMethods().getMainPlayer().isPlayingEmote()) {
            return clientStopLocalEmote(EmoteInstance.instance.getClientMethods().getMainPlayer().getEmote().getData());
        }
        return false;
    }

    public static boolean isForcedEmote() {
        IEmotePlayerEntity player = EmoteInstance.instance.getClientMethods().getMainPlayer();
        return player.isForcedEmote();
    }

    public static boolean clientStopLocalEmote(KeyframeAnimation emoteData) {
        if (emoteData != null && !EmoteInstance.instance.getClientMethods().getMainPlayer().isForcedEmote()) {
            EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
            packetBuilder.configureToSendStop(emoteData.getUuid(), EmoteInstance.instance.getClientMethods().getMainPlayer().emotes_getUUID());
            ClientPacketManager.send(packetBuilder, null);
            EmoteInstance.instance.getClientMethods().getMainPlayer().stopEmote();

            ClientEmoteEvents.LOCAL_EMOTE_STOP.invoker().onEmoteStop();
            return true;
        }
        return false;
    }

    static void executeMessage(NetData data, INetworkInstance networkInstance) throws NullPointerException {
        EmoteInstance.instance.getLogger().log(Level.FINEST, "[emotes client] Received message: " + data);

        if (data.purpose == null) {
            if (EmoteInstance.config.showDebug.get()) {
                EmoteInstance.instance.getLogger().log(Level.INFO, "Packet execution is not possible without a purpose");
            }
        }
        switch (Objects.requireNonNull(data.purpose)) {
            case STREAM:
                assert data.emoteData != null;
                if(data.valid || !(((ClientConfig)EmoteInstance.config).alwaysValidate.get() || !networkInstance.safeProxy())) {
                    receivePlayPacket(data.emoteData, data.player, data.tick, data.isForced);
                }
                break;
            case STOP:
                IEmotePlayerEntity player = EmoteInstance.instance.getGetters().getPlayerFromUUID(data.player);
                assert data.stopEmoteID != null;
                if(player != null) {
                    ClientEmoteEvents.EMOTE_STOP.invoker().onEmoteStop(data.stopEmoteID, player.emotes_getUUID());
                    player.stopEmote(data.stopEmoteID);
                    if(player.isMainPlayer() && !data.isForced){
                        EmoteInstance.instance.getClientMethods().sendChatMessage(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.blockedEmote"));
                    }
                }
                else {
                    queue.remove(data.player);
                }
                break;
            case CONFIG:
                networkInstance.setVersions(Objects.requireNonNull(data.versions));
                break;
            case FILE:
                EmoteHolder.addEmoteToList(data.emoteData).fromInstance = networkInstance;
            case UNKNOWN:
                if (EmoteInstance.config.showDebug.get()) {
                    EmoteInstance.instance.getLogger().log(Level.INFO, "Packet execution is not possible unknown purpose");
                }
                break;
        }
    }

    static void receivePlayPacket(KeyframeAnimation emoteData, UUID player, int tick, boolean isForced) {
        IEmotePlayerEntity playerEntity = EmoteInstance.instance.getGetters().getPlayerFromUUID(player);
        if(isEmoteAllowed(emoteData, player)) {
            EventResult result = ClientEmoteEvents.EMOTE_VERIFICATION.invoker().verify(emoteData, player);
            if (result == EventResult.FAIL) return;
            if (playerEntity != null) {
                ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(emoteData, player);
                playerEntity.playEmote(emoteData, tick, isForced);
            }
            else {
                addToQueue(new QueueEntry(emoteData, tick, EmoteInstance.instance.getClientMethods().getCurrentTick()), player);
            }
        }
    }

    public static boolean isEmoteAllowed(KeyframeAnimation emoteData, UUID player) {
        return (((ClientConfig)EmoteInstance.config).enablePlayerSafety.get() || !EmoteInstance.instance.getClientMethods().isPlayerBlocked(player))
                && (!emoteData.nsfw || ((ClientConfig)EmoteInstance.config).enableNSFW.get());
    }

    static void addToQueue(QueueEntry entry, UUID player) {
        queue.put(player, entry);
    }


    /**
     * @param uuid get emote for this player
     * @return KeyframeAnimation, current tick of the emote
     */
    public static @Nullable
    Pair<KeyframeAnimation, Integer> getEmoteForUUID(UUID uuid) {
        if (queue.containsKey(uuid)) {
            QueueEntry entry = queue.get(uuid);
            KeyframeAnimation emoteData = entry.emoteData;
            int tick = entry.beginTick - entry.receivedTick + EmoteInstance.instance.getClientMethods().getCurrentTick();
            queue.remove(uuid);
            if (!emoteData.isPlayingAt(tick)) return null;
            return new Pair<>(emoteData, tick);
        }
        return null;
    }

    /**
     * Call this periodically to keep the queue clean
     */
    public static void checkQueue(){
        int currentTick = EmoteInstance.instance.getClientMethods().getCurrentTick();
        queue.forEach((uuid, entry) -> {
            if(!entry.emoteData.isPlayingAt(entry.beginTick + currentTick)
                    && entry.beginTick + currentTick > 0
                    || EmoteInstance.instance.getClientMethods().getCurrentTick() - entry.receivedTick > 24000){
                queue.remove(uuid);
            }
        });
    }

    public static void init() {
        ClientEmoteAPI.INSTANCE = new ClientEmotePlay();
    }

    @Override
    protected boolean playEmoteImpl(KeyframeAnimation animation) {
        if (animation != null) {
            return clientStartLocalEmote(animation);
        } else {
            return clientStopLocalEmote();
        }
    }

    @Override
    protected Collection<KeyframeAnimation> clientEmoteListImpl() {
        return EmoteHolder.list.values().stream().map(EmoteHolder::getEmote).collect(Collectors.toList());
    }

    static class QueueEntry{
        final KeyframeAnimation emoteData;
        final int beginTick;
        final int receivedTick;

        QueueEntry(KeyframeAnimation emoteData, int begin, int received) {
            this.emoteData = emoteData;
            this.beginTick = begin;
            this.receivedTick = received;
        }
    }
}
