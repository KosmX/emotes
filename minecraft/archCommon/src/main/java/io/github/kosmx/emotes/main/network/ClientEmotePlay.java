package io.github.kosmx.emotes.main.network;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.impl.event.EventResult;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.PlayingAnimationData;
import io.github.kosmx.emotes.api.events.client.ClientEmoteAPI;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ClientEmotePlay extends ClientEmoteAPI {
    /**
     * When the emotePacket arrives earlier than the player entity data
     * I put the emote into a queue.
     */
    private static final Map<UUID, PlayingAnimationData> QUEUE = new ConcurrentHashMap<>();

    public static boolean clientStartLocalEmote(PlayingAnimationData data) {
        LocalPlayer player = PlatformTools.getMainPlayer();
        if (player.emotecraft$isForcedEmote()) {
            return false;
        }

        ClientPacketManager.send(data.preparePacket(), null);
        ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(data, player.getUUID());
        if (!data.canBeSynced() || !ClientPacketManager.isRemoteSupportSync()) player.emotecraft$playEmote(data);
        return true;
    }

    public static void clientRepeatLocalEmote(PlayingAnimationData data, UUID target) {
        EmotePacket.Builder packetBuilder = data.preparePacket().configureTarget(PlatformTools.getMainPlayer().getUUID());
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
        LoggerService.INSTANCE.log(Level.FINE, "[emotes client] Received message: " + data);
        if (data.purpose == null) {
            LoggerService.INSTANCE.log(Level.INFO, "Packet execution is not possible without a purpose");
            return;
        }

        switch (Objects.requireNonNull(data.purpose)) {
            case STREAM:
                assert data.emoteData != null;
                if (data.valid || !PlatformTools.getConfig().alwaysValidate.get()) {
                    receivePlayPacket(data.player, new PlayingAnimationData(data));
                }
                break;
            case STOP:
                AbstractClientPlayer player = PlatformTools.getPlayerFromUUID(data.player);
                assert data.stopEmoteID != null;
                if (player != null) {
                    ClientEmoteEvents.EMOTE_STOP.invoker().onEmoteStop(data.stopEmoteID, player.getUUID());
                    player.stopEmote(data.stopEmoteID);
                    if (player.isMainPlayer() && !data.isForced) {
                        PlatformTools.sendChatMessage(Component.translatable("emotecraft.blockedEmote"));
                    }
                } else {
                    QUEUE.remove(data.player);
                }
                break;
            case CONFIG:
                networkInstance.setVersions(Objects.requireNonNull(data.versions));
                LoggerService.INSTANCE.log(Level.INFO, "Legacy versions was received: " + data.versions);
                break;
            case FILE:
                EmoteHolder.addEmoteToList(data.emoteData).fromInstance = networkInstance;
            case UNKNOWN:
                LoggerService.INSTANCE.log(Level.WARNING, "Packet execution is not possible unknown purpose");
                break;
        }
    }

    static void receivePlayPacket(UUID player, PlayingAnimationData data) {
        AbstractClientPlayer playerEntity = PlatformTools.getPlayerFromUUID(player);
        if(isEmoteAllowed(data.currentEmote(), player)) {
            EventResult result = ClientEmoteEvents.EMOTE_VERIFICATION.invoker().verify(data, player);
            if (result == EventResult.FAIL) return;
            if (playerEntity != null) {
                ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(data, player);
                playerEntity.emotecraft$playEmote(data);
            }
            else {
                QUEUE.put(player, data);
            }
        }
    }

    public static boolean isEmoteAllowed(KeyframeAnimation emoteData, UUID player) {
        return (PlatformTools.getConfig().enablePlayerSafety.get() || !PlatformTools.isPlayerBlocked(player))
                && (!emoteData.nsfw || PlatformTools.getConfig().enableNSFW.get());
    }


    /**
     * @param uuid get emote for this player
     * @return KeyframeAnimation, current tick of the emote
     */
    public static @Nullable PlayingAnimationData getEmoteForUUID(UUID uuid) {
        if (QUEUE.containsKey(uuid)) {
            PlayingAnimationData entry = QUEUE.remove(uuid);
            if (!entry.isPlayingAt(Instant.now()))
                return null;
            return entry;
        }
        return null;
    }

    /**
     * Call this periodically to keep the queue clean
     */
    public static void checkQueue(){
        for (var entry : QUEUE.entrySet()) {
            if (!entry.getValue().isPlayingAt(Instant.now())) {
                QUEUE.remove(entry.getKey());
            }
        }
    }

    @Override
    protected boolean playEmoteImpl(PlayingAnimationData animation) {
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
}
