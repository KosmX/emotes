package org.redlance.dima_dencep.mods.emotecraft.geyser.handler;

import dev.kosmx.playerAnim.core.impl.event.EventResult;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.objects.NetData;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.text.MinecraftLocale;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class GeyserNetworkInstance implements INetworkInstance {
    private final HashMap<Byte, Byte> versions = new HashMap<>();
    private final Map<UUID, Object> queue = new ConcurrentHashMap<>();
    private final GeyserSession session;

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
        NetData data = builder.getData();

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
                } else {
                    // this.queue.put(data.player, new QueueEntry(data.emoteData, data.tick, ClientMethods.getCurrentTick()));
                }
                break;

            case STOP:
                assert data.stopEmoteID != null;
                PlayerEntity player = getPlayerFromUUID(data.player);

                if (player != null) {
                    ClientEmoteEvents.EMOTE_STOP.invoker().onEmoteStop(data.stopEmoteID, player.getUuid());

                    this.session.showEmote(player, "idk");

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
                LoggerService.INSTANCE.log(Level.WARNING, "Packet execution is not possible unknown purpose");
                break;
        }
    }

    public void sendChatMessage(String key) {
        this.session.sendChat(MinecraftLocale.getLocaleString(key, this.session.locale()));
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
    public int getRemoteVersion() {
        return CommonData.networkingVersion;
    }

    @Override
    public boolean isServerTrackingPlayState() {
        return this.versions.get(PacketConfig.SERVER_TRACK_EMOTE_PLAY) != 0;
    }

    @Override
    public int maxDataSize() {
        return CommonData.MAX_PACKET_SIZE;
    }

    @Override
    public boolean sendPlayerID() {
        return !isServerTrackingPlayState();
    }
}
