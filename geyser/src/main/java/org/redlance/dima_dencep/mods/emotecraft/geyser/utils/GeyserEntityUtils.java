package org.redlance.dima_dencep.mods.emotecraft.geyser.utils;

import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.geyser.entity.type.player.AvatarEntity;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class GeyserEntityUtils {
    public static @Nullable AvatarEntity getAvatarByUUID(GeyserSession session, UUID uuid) {
        if (session.entities().playerEntity() instanceof AvatarEntity player && player.getUuid().equals(uuid)) {
            return player;
        }

        PlayerEntity player = session.getEntityCache().getPlayerEntity(uuid);
        if (player != null) return player; // Fast

        for (Entity entity : session.getEntityCache().getEntities().values()) {
            if (entity instanceof AvatarEntity avatar && avatar.getUuid().equals(uuid)) {
                return avatar;
            }
        }
        return null;
    }

    public static boolean unsubscribedFromEntity(AvatarEntity entity) {
        GeyserSession session = entity.getSession();
        if (session.isClosed()) return true;

        if (session.entities().playerEntity() == entity) {
            return false;
        } else {
            return session.getEntityCache().getEntityByGeyserId(entity.getGeyserId()) == null;
        }
    }
}
