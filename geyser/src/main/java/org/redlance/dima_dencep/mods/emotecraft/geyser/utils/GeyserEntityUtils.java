package org.redlance.dima_dencep.mods.emotecraft.geyser.utils;

import org.cloudburstmc.protocol.bedrock.packet.EmotePacket;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.geyser.entity.type.player.AvatarEntity;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.jspecify.annotations.NonNull;
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

    public static void showEmote(@NonNull AvatarEntity emoter, @NonNull String emoteId) {
        EmotePacket packet = new EmotePacket();
        packet.setRuntimeEntityId(emoter.getGeyserId());
        packet.setXuid("");
        packet.setPlatformId(""); // BDS sends empty
        packet.setEmoteId(emoteId);
        emoter.getSession().sendUpstreamPacket(packet);
    }

    public static boolean unsubscribedFromEntity(AvatarEntity entity) {
        return entity.getSession().getEntityCache().getEntityByGeyserId(entity.getGeyserId()) == null;
    }
}
