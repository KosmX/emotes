package org.redlance.dima_dencep.mods.emotecraft.geyser.utils;

import org.geysermc.geyser.entity.type.player.AvatarEntity;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class GeyserEntityUtils {
    public static @Nullable AvatarEntity getAvatarByUUID(GeyserSession session, UUID uuid) {
        if (session.playerEntity() instanceof AvatarEntity player && uuid.equals(player.uuid())) {
            return player;
        }

        PlayerEntity player = session.getEntityCache().getPlayerEntity(uuid);
        if (player != null) return player; // Fast

        if (session.getEntityCache().getEntityByUuid(uuid) instanceof AvatarEntity avatar) {
            return avatar;
        }
        return null;
    }

    public static boolean unsubscribedFromEntity(AvatarEntity entity) {
        GeyserSession session = entity.getSession();
        if (session.isClosed()) return true;

        if (session.playerEntity() == entity) {
            return false;
        } else {
            return session.getEntityCache().getEntityByGeyserId(entity.geyserId()) == null;
        }
    }
}
