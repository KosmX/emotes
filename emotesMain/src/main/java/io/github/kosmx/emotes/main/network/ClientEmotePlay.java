package io.github.kosmx.emotes.main.network;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;

public class ClientEmotePlay {

    /**
     * When the emotePacket arrives earlier than the player entity data
     * I put the emote into a queue.
     */
    //private static final int maxQueueLength = 256;
    private static final HashMap<UUID, QueueEntry> queue = new HashMap<>();

    public static void clientStartLocalEmote(EmoteHolder emoteHolder) {
        clientStartLocalEmote(emoteHolder.getEmote());
    }

    public static boolean clientStartLocalEmote(EmoteData emote) {
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToStreamEmote(emote, EmoteInstance.instance.getClientMethods().getMainPlayer().emotes_getUUID());
        ClientPacketManager.send(packetBuilder, null);
        EmoteInstance.instance.getClientMethods().getMainPlayer().playEmote(emote, 0);
        return true;
    }

    public static void clientRepeateLocalEmote(EmoteData emote, int tick, UUID target){
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToStreamEmote(emote, EmoteInstance.instance.getClientMethods().getMainPlayer().emotes_getUUID()).configureEmoteTick(tick);
        ClientPacketManager.send(packetBuilder, target);
    }

    public static void clientStopLocalEmote() {
        if (EmoteInstance.instance.getClientMethods().getMainPlayer().isPlayingEmote()) {
            clientStopLocalEmote(EmoteInstance.instance.getClientMethods().getMainPlayer().getEmote().getData());
        }
    }

    public static void clientStopLocalEmote(EmoteData emoteData) {
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToSendStop(emoteData.getUuid(), EmoteInstance.instance.getClientMethods().getMainPlayer().emotes_getUUID());
        ClientPacketManager.send(packetBuilder, null);
        EmoteInstance.instance.getClientMethods().getMainPlayer().stopEmote();
    }

    static void executeMessage(NetData data, INetworkInstance networkInstance) throws NullPointerException {
        if (data.purpose == null) {
            if (EmoteInstance.config.showDebug.get()) {
                EmoteInstance.instance.getLogger().log(Level.INFO, "Packet execution is not possible without a purpose");
            }
        }
        switch (Objects.requireNonNull(data.purpose)) {
            case STREAM:
                assert data.emoteData != null;
                if(data.valid || !(((ClientConfig)EmoteInstance.config).alwaysValidate.get() || !networkInstance.safeProxy())) {
                    receivePlayPacket(data.emoteData, data.player, data.tick);
                }
                break;
            case STOP:
                IEmotePlayerEntity player = EmoteInstance.instance.getGetters().getPlayerFromUUID(data.player);
                assert data.stopEmoteID != null;
                if(player != null) {
                    player.stopEmote(data.stopEmoteID);
                    if(player.isMainPlayer()){
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

    static void receivePlayPacket(EmoteData emoteData, UUID player, int tick) {
        IEmotePlayerEntity playerEntity = EmoteInstance.instance.getGetters().getPlayerFromUUID(player);
        if(isEmoteAllowed(emoteData, player)) {
            if (playerEntity != null) {
                playerEntity.playEmote(emoteData, tick);
            }
            else {
                addToQueue(new QueueEntry(emoteData, tick, EmoteInstance.instance.getClientMethods().getCurrentTick()), player);
            }
        }
    }

    public static boolean isEmoteAllowed(EmoteData emoteData, UUID player) {
        return (((ClientConfig)EmoteInstance.config).enablePlayerSafety.get() || !EmoteInstance.instance.getClientMethods().isPlayerBlocked(player))
                && (!emoteData.nsfw || ((ClientConfig)EmoteInstance.config).enableNSFW.get());
    }

    static void addToQueue(QueueEntry entry, UUID player) {
        queue.put(player, entry);
    }


    /**
     * @param uuid get emote for this player
     * @return EmoteData, current tick of the emote
     */
    public static @Nullable
    Pair<EmoteData, Integer> getEmoteForUUID(UUID uuid) {
        if (queue.containsKey(uuid)) {
            QueueEntry entry = queue.get(uuid);
            EmoteData emoteData = entry.emoteData;
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

    static class QueueEntry{
        final EmoteData emoteData;
        final int beginTick;
        final int receivedTick;

        QueueEntry(EmoteData emoteData, int begin, int received) {
            this.emoteData = emoteData;
            this.beginTick = begin;
            this.receivedTick = received;
        }
    }
}
