package io.github.kosmx.emotes.main.network;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.event.EventResult;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.EmotecraftClientMod;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.main.EmoteHolder;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseClientNetwork implements INetworkInstance {
    private final HashMap<Byte, Byte> versions = new HashMap<>(EmotePacket.defaultVersions);

    /**
     * When the emotePacket arrives earlier than the player entity data
     * I put the emote into a queue.
     */
    private static final Map<UUID, QueueEntry> QUEUE = new ConcurrentHashMap<>();

    /**
     * Network instance has received a message, it will send it to EmoteX to execute
     *
     * @param packet received buffer
     */
    public void receiveMessage(EmotePacket packet) {
        if (packet.data.purpose == null) {
            CommonData.LOGGER.error("Packet execution is not possible without a purpose!");
            return;
        }
        CommonData.LOGGER.trace("[emotes client] Received message: {}", packet.data);

        if (packet.data.player == null && packet.data.purpose.playerBound) {
            CommonData.LOGGER.error("Didn't received any player information!");
            return;
        }

        try {
            BaseClientNetwork.executeMessage(packet.data, this);
        } catch (Exception e) { // I don't want to break the whole game with a bad message, but I'll warn with the highest level
            CommonData.LOGGER.error("Critical error has occurred while receiving emote!", e);
        }
    }

    protected static void executeMessage(NetData data, INetworkInstance networkInstance) throws NullPointerException {
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
                if (networkInstance instanceof BaseClientNetwork base) base.onConfigurationDone();
                break;
            case FILE:
                EmoteHolder.addEmoteToList(data.emoteData, networkInstance);
                break;
            case REMOVE:
                EmoteHolder.removeEmotesFromList(data.removeEmoteIds, networkInstance);
                break;
            case UNKNOWN:
            default:
                CommonData.LOGGER.error("Packet execution is not possible: {} purpose!", data.purpose);
                break;
        }
    }

    protected static void receivePlayPacket(Animation emoteData, UUID player, float tick, boolean isForced) {
        Avatar playerEntity = PlatformTools.getAvatarFromUUID(player);
        if (PlatformTools.isEmoteAllowed(emoteData, player)) {
            EventResult result = ClientEmoteEvents.EMOTE_VERIFICATION.invoker().verify(emoteData, player);
            if (result == EventResult.FAIL) return;
            if (playerEntity != null) {
                ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(emoteData, tick, player);
                playerEntity.emotecraft$playEmote(emoteData, tick, isForced);
            } else {
                QUEUE.put(player, new QueueEntry(emoteData, tick, EmotecraftClientMod.getTick()));
            }
        }
    }

    @Override
    public void disconnect() {
        EmoteHolder.clearEmotes(this);
    }

    protected abstract void onConfigurationDone();

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
    public static void checkQueue() {
        int currentTick = EmotecraftClientMod.getTick();
        QUEUE.forEach((uuid, entry) -> {
            if(!entry.emoteData.isPlayingAt(entry.beginTick + currentTick)
                    && entry.beginTick + currentTick > 0
                    || EmotecraftClientMod.getTick() - entry.receivedTick > 24000){
                QUEUE.remove(uuid);
            }
        });
    }

    record QueueEntry(Animation emoteData, float beginTick, int receivedTick) {}

    @Override
    public HashMap<Byte, Byte> getVersions() {
        return this.versions;
    }
}
