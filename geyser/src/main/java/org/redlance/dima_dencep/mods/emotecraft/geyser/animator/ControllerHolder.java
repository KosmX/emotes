package org.redlance.dima_dencep.mods.emotecraft.geyser.animator;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.geysermc.geyser.entity.type.player.AvatarEntity;

import java.util.Map;
import java.util.UUID;

public class ControllerHolder {
    private static final Map<UUID, GeyserAnimationController> CONTROLLERS = new Object2ObjectOpenHashMap<>();

    public static GeyserAnimationController get(AvatarEntity entity) {
        GeyserAnimationController controller = CONTROLLERS.computeIfAbsent(entity.getUuid(), GeyserAnimationController::new);
        controller.subscribe(entity);
        return controller;
    }
}
