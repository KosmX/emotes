package org.redlance.dima_dencep.mods.emotecraft.geyser.handler;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.event.EventResult;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.common.tools.UUIDMap;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.entity.type.player.GeyserPlayerEntity;
import org.geysermc.geyser.entity.type.player.AvatarEntity;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;
import org.jetbrains.annotations.Nullable;
import org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt;
import org.redlance.dima_dencep.mods.emotecraft.geyser.animator.GeyserAnimationController;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.EmotecraftLocale;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.FormUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class GeyserNetworkInstance extends AbstractNetworkInstance {
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private final HashMap<Byte, Byte> versions = new HashMap<>();
    // private final Map<UUID, Object> queue = new ConcurrentHashMap<>();
    private final UUIDMap<Animation> animations = new UUIDMap<>();
    private final GeyserConnection session;
    private final Future<?> ticker;

    private final Map<AvatarEntity, GeyserAnimationController> controllers = new WeakHashMap<>();

    private UUID currentEmote;
    private ConnectionType connectionType = ConnectionType.NONE;

    public GeyserNetworkInstance(GeyserConnection session) {
        this.session = session;

        this.ticker = EXECUTOR.scheduleAtFixedRate(() -> this.controllers.values()
                .forEach(controller -> {
                    try {
                        controller.run();
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                }), 0L, 50L, TimeUnit.MILLISECONDS
        );
    }

    @Override
    public HashMap<Byte, Byte> getRemoteVersions() {
        return this.versions;
    }

    @Override
    public void setVersions(Map<Byte, Byte> map) {
        this.versions.clear();
        this.versions.putAll(map);
    }

    @Override
    public void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException {
        super.sendMessage(builder.setVersion(getRemoteVersions()), target);
    }

    @Override
    public void sendMessage(EmotePacket packet, @Nullable UUID target) {
        ByteBuf buf = Unpooled.buffer();
        packet.write(buf);
        ((GeyserSession) this.session).sendDownstreamPacket(new ServerboundCustomPayloadPacket(
                EmotecraftExt.EMOTECRAFT_EMOTE_TYPE, MathHelper.readBytes(buf)
        ));
        buf.release();
    }

    @Override
    public void receiveMessage(EmotePacket packet, UUID player) {
        try {
            NetData data = packet.data;
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
                    // this.session.entities().showEmote(playerEntity, "4c8ae710-df2e-47cd-814d-cc7bf21a3d67"); // TODO translate

                    this.controllers.computeIfAbsent(playerEntity, GeyserAnimationController::new)
                            .triggerAnimation(data.emoteData, data.tick);

                    if (isMainPlayer(playerEntity)) {
                        this.currentEmote = data.emoteData.get();
                    }
                } /*else {
                    // this.queue.put(data.player, new QueueEntry(data.emoteData, data.tick, ClientMethods.getCurrentTick()));
                }*/
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
                    // this.queue.remove(data.player);
                    CommonData.LOGGER.warn("Queue is not supported!");
                }
                break;
            case CONFIG:
                setVersions(Objects.requireNonNull(data.versions));
                break;

            case FILE:
                this.animations.add(data.emoteData);
                break;

            case UNKNOWN:
                CommonData.LOGGER.warn("Packet execution is not possible unknown purpose");
                break;
        }
    }

    public void showForm() {
        SimpleForm.Builder builder = SimpleForm.builder()
                .translator(EmotecraftLocale::getLocaleString, this.session.locale())
                .title(CommonData.MOD_NAME);
        if (this.connectionType.translation != null) builder.content(this.connectionType.translation);

        for (Animation animation : UniversalEmoteSerializer.getLoadedEmotes().values()) {
            builder.button(FormUtils.createButtonComponent(animation, this.session.locale()));
        }
        for (Animation animation : this.animations.values()) {
            builder.button(FormUtils.createButtonComponent(animation, this.session.locale()));
        }

        SimpleForm simpleForm = builder.validResultHandler((form, response) -> {
            UUID emoteId = FormUtils.extractAnimationFromButton(response.clickedButton());
            Animation animation = this.animations.getOrDefault(emoteId, UniversalEmoteSerializer.getEmote(emoteId));
            if (animation != null) playEmote(animation, true);
        }).build();
        this.session.sendForm(simpleForm);
    }

    public void stopEmote() {
        stopEmote(this.session.entities().playerEntity());
    }

    public void stopEmote(GeyserPlayerEntity player) {
        if (player instanceof AvatarEntity entity && this.controllers.containsKey(entity)) {
            this.controllers.get(entity).stop();
        }

        this.session.entities().showEmote(player, "");

        if (isMainPlayer(player) && this.currentEmote != null) {
            try {
                sendMessage(new EmotePacket.Builder().configureToSendStop(this.currentEmote), null);
            } catch (IOException e) {
                CommonData.LOGGER.warn("Failed to stop animation!", e);
            }

            this.currentEmote = null;
        }
    }

    public void sendChatMessage(String key) {
        this.session.sendMessage(EmotecraftLocale.getLocaleString(key, this.session.locale()));
    }

    public void playEmote(Animation animation, boolean local) {
        ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(animation, 0, this.session.javaUuid());
        try {
            sendMessage(new EmotePacket.Builder().configureToStreamEmote(animation), null);
            if (local) {
                this.session.entities().showEmote(this.session.entities().playerEntity(), "4c8ae710-df2e-47cd-814d-cc7bf21a3d67"); // TODO translate
            }
            this.currentEmote = animation.get();
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    public PlayerEntity getPlayerFromUUID(UUID uuid) {
        if (this.session.javaUuid().equals(uuid)) {
            return (PlayerEntity) this.session.entities().playerEntity();
        }
        return ((GeyserSession) this.session).getEntityCache().getPlayerEntity(uuid);
    }

    public boolean isMainPlayer(GeyserPlayerEntity geyserPlayer) {
        return geyserPlayer instanceof PlayerEntity player && this.session.javaUuid().equals(player.getUuid());
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

    public void setConnectionType(ConnectionType type) {
        this.connectionType = type;
    }

    public ConnectionType getConnectionType() {
        return this.connectionType;
    }

    public boolean isPlaying() {
        return this.currentEmote != null;
    }

    @Override
    public void disconnect() {
        if (this.currentEmote != null) {
            stopEmote();
            this.currentEmote = null;
        }
        this.connectionType = ConnectionType.NONE;
        this.animations.clear();
        this.versions.clear();
        this.ticker.cancel(true);
    }
}
