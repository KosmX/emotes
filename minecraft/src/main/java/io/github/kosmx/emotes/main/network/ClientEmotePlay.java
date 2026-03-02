package io.github.kosmx.emotes.main.network;

import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.event.EventResult;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.events.client.ClientEmoteAPI;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.EmotecraftClientMod;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.main.EmoteHolder;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClientEmotePlay extends ClientEmoteAPI {
    /**
     * When the emotePacket arrives earlier than the player entity data
     * I put the emote into a queue.
     */
    private static final Map<UUID, QueueEntry> QUEUE = new ConcurrentHashMap<>();

    public static void clientStartLocalEmote(EmoteHolder emoteHolder) {
        clientStartLocalEmote(emoteHolder.getEmote());
    }

    public static boolean clientStartLocalEmote(Animation emote) {
        return clientStartLocalEmote(emote, 0);
    }

    public static boolean clientStartLocalEmote(Animation emote, float tick) {
        LocalPlayer player = ClientUtil.getClientPlayer();
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

    public static void clientRepeatLocalEmote(Animation emote, float tick, UUID target) {
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToStreamEmote(emote, ClientUtil.getClientPlayer().getUUID()).configureEmoteTick(tick);
        ClientPacketManager.send(packetBuilder, target);
    }

    public static boolean clientStopLocalEmote() {
        if (ClientUtil.getClientPlayer().isPlayingEmote()) {
            return clientStopLocalEmote(ClientUtil.getClientPlayer().emotecraft$getEmote().getCurrentAnimationInstance());
        }
        return false;
    }

    public static boolean isForcedEmote() {
        return ClientUtil.getClientPlayer().emotecraft$isForcedEmote();
    }

    public static boolean clientStopLocalEmote(@Nullable Animation emoteData) {
        if (emoteData != null && !ClientUtil.getClientPlayer().emotecraft$isForcedEmote()) {
            EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
            packetBuilder.configureToSendStop(emoteData.uuid(), ClientUtil.getClientPlayer().getUUID());
            ClientPacketManager.send(packetBuilder, null);
            ClientUtil.getClientPlayer().stopEmote();

            ClientEmoteEvents.LOCAL_EMOTE_STOP.invoker().onEmoteStop();
            return true;
        }
        return false;
    }

    static void executeMessage(NetData data, INetworkInstance networkInstance) throws NullPointerException {
        CommonData.LOGGER.trace("[emotes client] Received message: {}", data);
        if (data.purpose == null) {
            CommonData.LOGGER.error("Packet execution is not possible without a purpose");
            return;
        }

        switch (Objects.requireNonNull(data.purpose)) {
            case STREAM:
                assert data.emoteData != null;
                if (data.valid || !PlatformTools.getConfig().alwaysValidate.get()) {
                    receivePlayPacket(data.emoteData, data.player, data.tick, data.isForced);
                }
                break;
            case STOP:
                Avatar player = PlatformTools.getAvatarFromUUID(data.player);
                assert data.stopEmoteID != null;
                if (player != null) {
                    ClientEmoteEvents.EMOTE_STOP.invoker().onEmoteStop(data.stopEmoteID, player.getUUID());
                    player.stopEmote(data.stopEmoteID);
                    if (player.isMainAvatar() && !data.isForced) {
                        PlatformTools.addToast(Component.translatable("emotecraft.blockedEmote"));
                    }
                } else {
                    QUEUE.remove(data.player);
                }
                break;
            case CONFIG:
                networkInstance.setVersions(Objects.requireNonNull(data.versions));
                CommonData.LOGGER.warn("Legacy versions was received: {}", data.versions);
                break;
            case FILE:
                EmoteHolder.addEmoteToList(data.emoteData, networkInstance);
            case UNKNOWN:
                CommonData.LOGGER.error("Packet execution is not possible unknown purpose");
                break;
        }
    }

    static void receivePlayPacket(Animation emoteData, UUID player, float tick, boolean isForced) {
        Avatar playerEntity = PlatformTools.getAvatarFromUUID(player);
        if (isEmoteAllowed(emoteData, player)) {
            EventResult result = ClientEmoteEvents.EMOTE_VERIFICATION.invoker().verify(emoteData, player);
            if (result == EventResult.FAIL) return;
            if (playerEntity != null) {
                ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(emoteData, tick, player);
                playerEntity.emotecraft$playEmote(emoteData, tick, isForced);
            }
            else {
                QUEUE.put(player, new QueueEntry(emoteData, tick, EmotecraftClientMod.getTick()));
            }
        }
    }

    @SuppressWarnings("unused")
    public static boolean isEmoteAllowed(Animation emoteData, UUID player) {
        return !PlatformTools.getConfig().enablePlayerSafety.get() || !Minecraft.getInstance().isBlocked(player);
    }

    /**
     * @param uuid get emote for this player
     * @return KeyframeAnimation, current tick of the emote
     */
    public static @Nullable Pair<Animation, Float> getEmoteForUUID(UUID uuid) {
        if (QUEUE.containsKey(uuid)) {
            QueueEntry entry = QUEUE.get(uuid);
            Animation emoteData = entry.emoteData;
            float tick = entry.beginTick - entry.receivedTick + EmotecraftClientMod.getTick();
            QUEUE.remove(uuid);
            if (!emoteData.isPlayingAt(tick)) return null;
            return Pair.of(emoteData, tick);
        }
        return null;
    }

    /**
     * Call this periodically to keep the queue clean
     */
    public static void checkQueue(){
        int currentTick = EmotecraftClientMod.getTick();
        QUEUE.forEach((uuid, entry) -> {
            if(!entry.emoteData.isPlayingAt(entry.beginTick + currentTick)
                    && entry.beginTick + currentTick > 0
                    || EmotecraftClientMod.getTick() - entry.receivedTick > 24000){
                QUEUE.remove(uuid);
            }
        });
    }

    @Override
    protected boolean playEmoteImpl(Animation animation, float tick) {
        if (animation != null) {
            return clientStartLocalEmote(animation, tick);
        } else {
            return clientStopLocalEmote();
        }
    }

    @Override
    protected Collection<Animation> clientEmoteListImpl() {
        return EmoteHolder.list.values().stream().map(EmoteHolder::getEmote).collect(Collectors.toList());
    }

    static class QueueEntry {
        final Animation emoteData;
        final float beginTick;
        final int receivedTick;

        QueueEntry(Animation emoteData, float begin, int received) {
            this.emoteData = emoteData;
            this.beginTick = begin;
            this.receivedTick = received;
        }
    }
}
