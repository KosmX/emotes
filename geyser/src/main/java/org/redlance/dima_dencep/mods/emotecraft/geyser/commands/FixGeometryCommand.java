package org.redlance.dima_dencep.mods.emotecraft.geyser.commands;

import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.command.CommandExecutor;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.geyser.entity.type.player.AvatarEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.jspecify.annotations.NonNull;
import org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt;
import org.redlance.dima_dencep.mods.emotecraft.geyser.animator.ControllerHolder;

public class FixGeometryCommand implements CommandExecutor<GeyserConnection> {
    @Override
    public void execute(@NonNull GeyserConnection source, @NonNull Command command, String @NonNull [] args) {
        EmotecraftExt.getNetworkInstance(source).appliedGeometries.clear();

        for (Entity entity : ((GeyserSession) source).getEntityCache().getEntitiesUnsafe().values()) {
            if (entity instanceof AvatarEntity avatar) ControllerHolder.INSTANCE.resubscribe(avatar);
        }
    }
}
