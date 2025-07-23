package org.redlance.dima_dencep.mods.emotecraft.geyser.handler;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.event.EventResult;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.objects.NetData;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.text.MinecraftLocale;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;
import org.jetbrains.annotations.Nullable;
import org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GeyserNetworkInstance extends AbstractNetworkInstance {
    private final HashMap<Byte, Byte> versions = new HashMap<>();
    private final Map<UUID, Object> queue = new ConcurrentHashMap<>();
    private final GeyserSession session;

    private UUID currentEmote;
    private boolean isHandShaked;

    public GeyserNetworkInstance(GeyserSession session) {
        this.session = session;
    }

    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        return this.versions;
    }

    @Override
    public void setVersions(HashMap<Byte, Byte> map) {
        this.versions.clear();
        this.versions.putAll(map);
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        super.sendMessage(builder.setVersion(getRemoteVersions()), target);
    }

    @Override
    protected void sendMessage(byte[] bytes, @Nullable UUID target) {
        this.session.sendDownstreamPacket(new ServerboundCustomPayloadPacket(
                EmotecraftExt.EMOTECAFT_EMOTE_TYPE, bytes
        ));
    }

    @Override
    public void receiveMessage(ByteBuffer byteBuffer, UUID player) {
        try {
            NetData data = new EmotePacket.Builder().build().read(byteBuffer);
            if (!trustReceivedPlayer()) {
                data.player = null;
            }
            if (data.player == null && data.purpose.playerBound) {
                throw new IOException("Didn't received any player information");
            }

            CommonData.LOGGER.debug("[emotes client] Received message: {}", data);
            if (data.purpose == null) {
                CommonData.LOGGER.warn("Packet execution is not possible without a purpose");
                return;
            }

            handleNetData(data);
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    private void handleNetData(NetData data) {
        switch (Objects.requireNonNull(data.purpose)) {
            case STREAM:
                assert data.emoteData != null;
                PlayerEntity playerEntity = getPlayerFromUUID(data.player);

                EventResult result = ClientEmoteEvents.EMOTE_VERIFICATION.invoker().verify(data.emoteData, data.player);
                if (result == EventResult.FAIL) break;

                if (playerEntity != null) {
                    ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(data.emoteData, data.tick, data.player);

                    //playerEntity.emotecraft$playEmote(data.emoteData, data.tick, data.isForced);
                    this.session.showEmote(playerEntity, "4c8ae710-df2e-47cd-814d-cc7bf21a3d67"); // TODO translate

                    if (isMainPlayer(playerEntity)) {
                        this.currentEmote = data.emoteData.get();
                    }
                } else {
                    // this.queue.put(data.player, new QueueEntry(data.emoteData, data.tick, ClientMethods.getCurrentTick()));
                }
                break;

            case STOP:
                assert data.stopEmoteID != null;
                PlayerEntity player = getPlayerFromUUID(data.player);

                if (player != null) {
                    ClientEmoteEvents.EMOTE_STOP.invoker().onEmoteStop(data.stopEmoteID, player.getUuid());
                    stopEmote(player);

                    if (isMainPlayer(player) && !data.isForced) {
                        sendChatMessage("emotecraft.blockedEmote");
                    }
                } else {
                    this.queue.remove(data.player);
                }
                break;
            case CONFIG:
                setVersions(Objects.requireNonNull(data.versions));
                break;

            case FILE:
                // TODO add bedrock form
                break;

            case UNKNOWN:
                CommonData.LOGGER.warn("Packet execution is not possible unknown purpose");
                break;
        }
    }

    public void stopEmote() {
        stopEmote(this.session.getPlayerEntity());
    }

    public void stopEmote(PlayerEntity player) {
        this.session.showEmote(player, "idk");

        if (isMainPlayer(player) && this.currentEmote != null) {
            try {
                sendMessage(new EmotePacket.Builder().configureToSendStop(this.currentEmote), null);
            } catch (IOException e) {
                CommonData.LOGGER.warn("Failed to stop animation!", e);
            }
        }

        this.currentEmote = null;
    }

    public void sendChatMessage(String key) {
        this.session.sendChat(MinecraftLocale.getLocaleString(key, this.session.locale()));
    }

    public void playEmote(Animation animation, boolean local) {
        ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(animation, 0, this.session.javaUuid());
        try {
            sendMessage(new EmotePacket.Builder().configureToStreamEmote(animation), null);
            if (local) {
                this.session.showEmote(this.session.getPlayerEntity(), animation.get().toString());
            }
            this.currentEmote = animation.get();
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    public PlayerEntity getPlayerFromUUID(UUID uuid) {
        if (this.session.javaUuid().equals(uuid)) {
            return this.session.getPlayerEntity();
        }
        return this.session.getEntityCache().getPlayerEntity(uuid);
    }

    public boolean isMainPlayer(PlayerEntity player) {
        return player != null && this.session.javaUuid().equals(player.getUuid());
    }

    @Override
    public boolean isActive() {
        return this.session != null;
    }

    @Override
    public boolean isServerTrackingPlayState() {
        return this.versions.get(PacketConfig.SERVER_TRACK_EMOTE_PLAY) != 0;
    }

    @Override
    public boolean sendPlayerID() {
        return !isServerTrackingPlayState();
    }

    public void sendC2SConfig() {
        sendC2SConfig(payload -> {
            try {
                sendMessage(payload, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void setHandShaked(boolean is) {
        this.isHandShaked = is;
    }

    public boolean isHandShaked() {
        return this.isHandShaked;
    }

    public boolean isPlaying() {
        return this.currentEmote != null;
    }
}
