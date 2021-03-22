package com.kosmx.emotes.main.network;

import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.common.network.EmotePacket;
import com.kosmx.emotes.common.network.objects.NetData;
import com.kosmx.emotes.common.tools.Pair;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.main.mixinFunctions.IPlayerEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
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
        packetBuilder.configureToSendEmote(emote, EmoteInstance.instance.getClientMethods().getMainPlayer().getUUID());
        ClientPacketManager.send(packetBuilder, null);
        EmoteInstance.instance.getClientMethods().getMainPlayer().playEmote(emote, 0);
        return true;
    }

    public static void clientRepeateLocalEmote(EmoteData emote, int tick, IPlayerEntity target){
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToSendEmote(emote, EmoteInstance.instance.getClientMethods().getMainPlayer().getUUID()).configureEmoteTick(tick);
        ClientPacketManager.send(packetBuilder, target);
    }

    public static void clientStopLocalEmote() {
        if (EmoteInstance.instance.getClientMethods().getMainPlayer().isPlayingEmote()) {
            clientStopLocalEmote(EmoteInstance.instance.getClientMethods().getMainPlayer().getEmote().getData());
            EmoteInstance.instance.getClientMethods().getMainPlayer().stopEmote();
        }
    }

    public static void clientStopLocalEmote(EmoteData emoteData) {
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToSendStop(emoteData.hashCode(), EmoteInstance.instance.getClientMethods().getMainPlayer().getUUID());
        ClientPacketManager.send(packetBuilder, null);
        EmoteInstance.instance.getClientMethods().getMainPlayer().stopEmote();
    }

    static void executeMessage(NetData data, IClientNetwork networkInstance) throws NullPointerException {
        if (data.purpose == null) {
            if (EmoteInstance.config.showDebug) {
                EmoteInstance.instance.getLogger().log(Level.INFO, "Packet execution is not possible without a purpose");
            }
        }
        switch (Objects.requireNonNull(data.purpose)) {
            case STREAM:
                assert data.emoteData != null;
                receivePlayPacket(data.emoteData, data.player, data.tick);
                break;
            case STOP:
                IEmotePlayerEntity player = EmoteInstance.instance.getGetters().getPlayerFromUUID(data.player);
                assert data.stopEmoteID != null;
                if(player != null) {
                    player.stopEmote(data.stopEmoteID.get());
                }
                else {
                    queue.remove(data.player);
                }
                break;
            case CONFIG:
                networkInstance.setVersions(Objects.requireNonNull(data.versions));
                break;
            case UNKNOWN:
                if (EmoteInstance.config.showDebug) {
                    EmoteInstance.instance.getLogger().log(Level.INFO, "Packet execution is not possible unknown purpose");
                }
                break;
        }
    }

    static void receivePlayPacket(EmoteData emoteData, UUID player, int tick) {
        IEmotePlayerEntity playerEntity = EmoteInstance.instance.getGetters().getPlayerFromUUID(player);
        if (playerEntity != null) {
            playerEntity.playEmote(emoteData, tick);
        }
        else {
            addToQueue(new QueueEntry(emoteData, tick, EmoteInstance.instance.getClientMethods().getCurrentTick()), player);
        }
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
            if (emoteData.isPlayingAt(tick)) return null;
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
