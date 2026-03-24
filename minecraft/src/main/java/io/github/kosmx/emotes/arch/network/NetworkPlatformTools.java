package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.redlance.common.services.AdvancedService;
import org.redlance.common.services.ServiceUtils;

import java.util.Collection;

public interface NetworkPlatformTools extends AdvancedService {
    NetworkPlatformTools INSTANCE = ServiceUtils.loadService(NetworkPlatformTools.class);

    CustomPacketPayload.Type<EmotePacketPayload> EMOTE_CHANNEL_ID = new CustomPacketPayload.Type<>(McUtils.newIdentifier(CommonData.playEmoteID));
    CustomPacketPayload.Type<EmotePacketPayload> STREAM_CHANNEL_ID = new CustomPacketPayload.Type<>(McUtils.newIdentifier(CommonData.emoteStreamID));

    boolean canSendPlay(ServerPlayer player, Identifier channel);
    boolean canSendConfig(ServerConfigurationPacketListenerImpl player, Identifier channel);
    Collection<ServerPlayer> getTrackedBy(Entity entity);

    static @NotNull Packet<?> createClientboundPacket(@NotNull CustomPacketPayload.Type<?> id, @NotNull EmotePacket packet) {
        return new ClientboundCustomPayloadPacket(new EmotePacketPayload(id, packet));
    }

    MinecraftServer getServer();

    static @NotNull Packet<?> playPacket(@NotNull EmotePacket packet) {
        return createClientboundPacket(EMOTE_CHANNEL_ID, packet);
    }

    static @NotNull Packet<?> streamPacket(@NotNull EmotePacket packet) {
        return createClientboundPacket(STREAM_CHANNEL_ID, packet);
    }
}
