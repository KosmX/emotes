package org.redlance.dima_dencep.mods.emotecraft.geyser.animator.geometry;

import org.geysermc.geyser.api.skin.SkinData;
import org.geysermc.geyser.entity.type.player.AvatarEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.skin.SkinManager;
import org.geysermc.geyser.skin.SkinProvider;
import org.redlance.common.utils.ReflectUtils;
import org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt;

import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.concurrent.CompletableFuture;

public class GeometryChanger {
    private static final MethodHandle REQUEST_SKIN_DATA = ReflectUtils.uncheck(() -> ReflectUtils.TRUSTED_LOOKUP.findStatic(
            SkinProvider.class, "requestSkinData", MethodType.methodType(CompletableFuture.class, AvatarEntity.class, GeyserSession.class)
    ));

    public static CompletableFuture<SkinData> changeGeometryToBending(AvatarEntity entity) {
        return requestSkinData(entity, entity.getSession())
                .thenApply(skinData -> {
                    SkinData bendable = new SkinData(skinData.skin(), skinData.cape(),
                            BendingGeometry.addBoneBends(skinData.geometry())
                    );
                    SkinManager.sendSkinPacket(entity.getSession(), entity, bendable);
                    return bendable;
                });
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
