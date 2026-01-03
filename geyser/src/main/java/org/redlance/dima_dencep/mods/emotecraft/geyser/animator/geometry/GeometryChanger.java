package org.redlance.dima_dencep.mods.emotecraft.geyser.animator.geometry;

import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerSkinPacket;
import org.geysermc.geyser.api.skin.Cape;
import org.geysermc.geyser.api.skin.Skin;
import org.geysermc.geyser.api.skin.SkinData;
import org.geysermc.geyser.api.skin.SkinGeometry;
import org.geysermc.geyser.entity.type.player.AvatarEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.skin.SkinManager;
import org.geysermc.geyser.skin.SkinProvider;
import org.redlance.common.utils.ReflectUtils;

import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.concurrent.CompletableFuture;

public class GeometryChanger {
    private static final MethodHandle REQUEST_SKIN_DATA = ReflectUtils.uncheck(() -> ReflectUtils.TRUSTED_LOOKUP.findStatic(
            SkinProvider.class, "requestSkinData", MethodType.methodType(CompletableFuture.class, AvatarEntity.class, GeyserSession.class)
    ));
    private static final MethodHandle GET_SKIN = ReflectUtils.uncheck(() -> ReflectUtils.TRUSTED_LOOKUP.findStatic(
            SkinManager.class, "getSkin", MethodType.methodType(SerializedSkin.class, GeyserSession.class, String.class, Skin.class, Cape.class, SkinGeometry.class)
    ));

    public static CompletableFuture<SkinData> changeGeometryToBending(AvatarEntity entity) {
        return requestSkinData(entity, entity.getSession())
                .thenApply(skinData -> {
                    SkinData bendable = new SkinData(skinData.skin(), skinData.cape(),
                            BendingGeometry.addBoneBends(skinData.geometry())
                    );
                    sendSkinPacket(entity.getSession(), entity, bendable);
                    return bendable;
                });
    }

    public static void sendSkinPacket(GeyserSession session, AvatarEntity entity, SkinData skinData) {
        Skin skin = skinData.skin();
        Cape cape = skinData.cape();
        SkinGeometry geometry = skinData.geometry();
        Color color = session.getWaypointCache().getWaypointColor(entity.getUuid()).orElse(Color.WHITE);

        if (entity.getUuid().equals(session.getPlayerEntity().getUuid())) {
            PlayerListPacket.Entry updatedEntry = SkinManager.buildEntryManually(
                    session,
                    entity.getUuid(),
                    entity.getUsername(),
                    entity.getGeyserId(),
                    skin,
                    cape,
                    geometry,
                    color
            );

            PlayerListPacket playerAddPacket = new PlayerListPacket();
            playerAddPacket.setAction(PlayerListPacket.Action.ADD);
            playerAddPacket.getEntries().add(updatedEntry);
            session.sendUpstreamPacketImmediately(playerAddPacket);
        } else {
            PlayerSkinPacket packet = new PlayerSkinPacket();
            packet.setUuid(entity.getUuid());
            packet.setOldSkinName("");
            packet.setNewSkinName(skin.textureUrl());
            try {
                packet.setSkin((SerializedSkin) GET_SKIN.invoke(session, skin.textureUrl(), skin, cape, geometry));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            packet.setTrustedSkin(true);
            session.sendUpstreamPacketImmediately(packet);
        }
    }

    @SuppressWarnings("unchecked")
    public static CompletableFuture<SkinData> requestSkinData(AvatarEntity entity, GeyserSession session) {
        try {
            return (CompletableFuture<SkinData>) REQUEST_SKIN_DATA.invoke(entity, session);
        } catch (Throwable th) {
            return CompletableFuture.failedFuture(th);
        }
    }
}
