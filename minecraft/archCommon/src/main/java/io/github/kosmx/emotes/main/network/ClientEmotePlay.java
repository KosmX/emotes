package io.github.kosmx.emotes.main.network;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.impl.event.EventResult;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.events.client.ClientEmoteAPI;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.arch.executor.ClientMethods;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;
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
        return clientStartLocalEmote(emote, 0);
    }

    public static boolean clientStartLocalEmote(KeyframeAnimation emote, int tick) {
        LocalPlayer player = PlatformTools.getMainPlayer();
        if (player.emotecraft$isForcedEmote()) {
            return false;
        }

        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToStreamEmote(emote, player.getUUID());
        packetBuilder.configureEmoteTick(tick);
        ClientPacketManager.send(packetBuilder, null);
        ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(emote, tick, player.getUUID());
        player.emotecraft$playEmote(emote, tick, false);
        return true;
    }

    public static void clientRepeatLocalEmote(KeyframeAnimation emote, int tick, UUID target){
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToStreamEmote(emote, PlatformTools.getMainPlayer().getUUID()).configureEmoteTick(tick);
        ClientPacketManager.send(packetBuilder, target);
    }

    public static boolean clientStopLocalEmote() {
        if (PlatformTools.getMainPlayer().isPlayingEmote()) {
            return clientStopLocalEmote(PlatformTools.getMainPlayer().emotecraft$getEmote().getData());
        }
        return false;
    }

    public static boolean isForcedEmote() {
        LocalPlayer player = PlatformTools.getMainPlayer();
        return player.emotecraft$isForcedEmote();
    }

    public static boolean clientStopLocalEmote(KeyframeAnimation emoteData) {
        if (emoteData != null && !PlatformTools.getMainPlayer().emotecraft$isForcedEmote()) {
            EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
            packetBuilder.configureToSendStop(emoteData.getUuid(), PlatformTools.getMainPlayer().getUUID());
            ClientPacketManager.send(packetBuilder, null);
            PlatformTools.getMainPlayer().stopEmote();

            ClientEmoteEvents.LOCAL_EMOTE_STOP.invoker().onEmoteStop();
            return true;
        }
        return false;
    }

    static void executeMessage(NetData data, INetworkInstance networkInstance) throws NullPointerException {
        LoggerService.LOADED_SERVICE.log(Level.FINE, "[emotes client] Received message: " + data);

        if (data.purpose == null) {
            LoggerService.LOADED_SERVICE.log(Level.INFO, "Packet execution is not possible without a purpose");
            return;
        }
        switch (Objects.requireNonNull(data.purpose)) {
            case STREAM:
                assert data.emoteData != null;
                if(data.valid || !(PlatformTools.getConfig().alwaysValidate.get() || !networkInstance.safeProxy())) {
                    receivePlayPacket(data.emoteData, data.player, data.tick, data.isForced);
                }
                break;
            case STOP:
                AbstractClientPlayer player = PlatformTools.getPlayerFromUUID(data.player);
                assert data.stopEmoteID != null;
                if(player != null) {
                    ClientEmoteEvents.EMOTE_STOP.invoker().onEmoteStop(data.stopEmoteID, player.getUUID());
                    player.stopEmote(data.stopEmoteID);
                    if(player.isMainPlayer() && !data.isForced){
                        PlatformTools.sendChatMessage(Component.translatable("emotecraft.blockedEmote"));
                    }
                }
                else {
                    queue.remove(data.player);
                }
                break;
            case CONFIG:
                networkInstance.setVersions(Objects.requireNonNull(data.versions));
                LoggerService.LOADED_SERVICE.log(Level.INFO, "Legacy versions was received: " + data.versions);
                break;
            case FILE:
                EmoteHolder.addEmoteToList(data.emoteData).fromInstance = networkInstance;
            case UNKNOWN:
                LoggerService.LOADED_SERVICE.log(Level.WARNING, "Packet execution is not possible unknown purpose");
                break;
        }
    }

    static void receivePlayPacket(KeyframeAnimation emoteData, UUID player, int tick, boolean isForced) {
        AbstractClientPlayer playerEntity = PlatformTools.getPlayerFromUUID(player);
        if(isEmoteAllowed(emoteData, player)) {
            EventResult result = ClientEmoteEvents.EMOTE_VERIFICATION.invoker().verify(emoteData, player);
            if (result == EventResult.FAIL) return;
            if (playerEntity != null) {
                ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(emoteData, tick, player);
                playerEntity.emotecraft$playEmote(emoteData, tick, isForced);
            }
            else {
                addToQueue(new QueueEntry(emoteData, tick, ClientMethods.getCurrentTick()), player);
            }
        }
    }

    public static boolean isEmoteAllowed(KeyframeAnimation emoteData, UUID player) {
        return (PlatformTools.getConfig().enablePlayerSafety.get() || !PlatformTools.isPlayerBlocked(player))
                && (!emoteData.nsfw || PlatformTools.getConfig().enableNSFW.get());
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
            int tick = entry.beginTick - entry.receivedTick + ClientMethods.getCurrentTick();
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
        int currentTick = ClientMethods.getCurrentTick();
        queue.forEach((uuid, entry) -> {
            if(!entry.emoteData.isPlayingAt(entry.beginTick + currentTick)
                    && entry.beginTick + currentTick > 0
                    || ClientMethods.getCurrentTick() - entry.receivedTick > 24000){
                queue.remove(uuid);
            }
        });
    }

    public static void init() {
        ClientEmoteAPI.INSTANCE = new ClientEmotePlay();
    }

    @Override
    protected boolean playEmoteImpl(KeyframeAnimation animation, int tick) {
        if (animation != null) {
            return clientStartLocalEmote(animation, tick);
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
