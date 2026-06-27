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
import io.netty.buffer.ByteBufAllocator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.entity.type.player.AvatarEntity;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;
import org.jetbrains.annotations.Nullable;
import org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt;
import org.redlance.dima_dencep.mods.emotecraft.geyser.animator.ControllerHolder;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.*;

import java.io.IOException;
import java.util.*;

public class GeyserNetworkInstance extends AbstractNetworkInstance {
    private final HashMap<Byte, Byte> versions = new HashMap<>();
    // private final Map<UUID, Object> queue = new ConcurrentHashMap<>();
    private final UUIDMap<Animation> animations = new UUIDMap<>();
    public final LongSet appliedGeometries = new LongOpenHashSet();
    private final GeyserConnection session;

    private UUID currentEmote;
    private ConnectionType connectionType = ConnectionType.NONE;

    public GeyserNetworkInstance(GeyserConnection session) {
        this.session = session;
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
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        try {
            packet.write(buf);
            ((GeyserSession) this.session).sendDownstreamPacket(new ServerboundCustomPayloadPacket(
                    EmotecraftExt.EMOTECRAFT_EMOTE_TYPE, MathHelper.readBytes(buf)
            ));
        } finally {
            buf.release();
        }
    }

    public void receiveMessage(EmotePacket packet) {
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
                AvatarEntity avatarEntity = getAvatarFromUUID(data.player);

                EventResult result = ClientEmoteEvents.EMOTE_VERIFICATION.invoker().verify(data.emoteData, data.player);
                if (result == EventResult.FAIL) break;

                if (avatarEntity != null) {
                    playEmote(avatarEntity, data.emoteData, data.tick, null);
                } /*else {
                    // this.queue.put(data.player, new QueueEntry(data.emoteData, data.tick, ClientMethods.getCurrentTick()));
                }*/
                break;

            case STOP:
                assert data.stopEmoteID != null;
                AvatarEntity avatar = getAvatarFromUUID(data.player);

                if (avatar != null) {
                    stopEmote(avatar, data.stopEmoteID);
                    if (isMainAvatar(avatar) && !data.isForced) {
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

            case REMOVE:
                for (UUID id : data.removeEmoteIds) this.animations.remove(id);
                break;

            case UNKNOWN:
                CommonData.LOGGER.warn("Packet execution is not possible unknown purpose");
                break;
        }
    }

    public void showEmoteList() {
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
            if (animation != null) playEmote(animation, 0, null);
        }).build();
        this.session.sendForm(simpleForm);
    }

    public void stopEmote(@Nullable UUID stopEmoteID) {
        stopEmote((AvatarEntity) this.session.entities().playerEntity(), stopEmoteID);
    }

    public void stopEmote(AvatarEntity avatarEntity, @Nullable UUID stopEmoteID) {
        if (stopEmoteID != null) { // TODO check
            ClientEmoteEvents.EMOTE_STOP.invoker().onEmoteStop(stopEmoteID, avatarEntity.uuid());
        }

        ControllerHolder.INSTANCE.get(avatarEntity).stop();
        BedrockPacketsUtils.sendBobAnimation(avatarEntity);

        if (isMainAvatar(avatarEntity) && this.currentEmote != null) {
            if (stopEmoteID == null) { // TODO check
                ClientEmoteEvents.EMOTE_STOP.invoker().onEmoteStop(this.currentEmote, avatarEntity.uuid());
            }

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

    public void playEmote(Animation animation, float tick, String bedrockId) {
        playEmote((AvatarEntity) this.session.entities().playerEntity(), animation, tick, bedrockId);
    }

    public void playEmote(AvatarEntity avatarEntity, Animation animation, float tick, String bedrockId) {
        ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(animation, tick, avatarEntity.uuid());

        if (isMainAvatar(avatarEntity)) {
            try {
                sendMessage(new EmotePacket.Builder().configureToStreamEmote(animation), null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.currentEmote = animation.get();
        }

        if (avatarEntity instanceof PlayerEntity player && bedrockId != null) {
            this.session.entities().showEmote(player, bedrockId); // TODO support tick?
        } else {
            ControllerHolder.INSTANCE.get(avatarEntity).triggerAnimation(animation, tick);
        }
    }

    public AvatarEntity getAvatarFromUUID(UUID uuid) {
        return GeyserEntityUtils.getAvatarByUUID((GeyserSession) this.session, uuid);
    }

    public boolean isMainAvatar(AvatarEntity avatarEntity) {
        return ((AvatarEntity) this.session.entities().playerEntity()).uuid().equals(avatarEntity.uuid());
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
            stopEmote(this.currentEmote);
            this.currentEmote = null;
        }
        this.connectionType = ConnectionType.NONE;
        this.animations.clear();
        this.versions.clear();
    }
}
