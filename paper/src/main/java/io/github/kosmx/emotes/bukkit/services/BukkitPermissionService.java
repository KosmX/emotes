package io.github.kosmx.emotes.bukkit.services;

import io.github.kosmx.emotes.mc.services.IPermissionService;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BukkitPermissionService implements IPermissionService {
    @Override
    public Optional<Boolean> getPermissionValue(@NotNull CommandSourceStack source, @NotNull String permission) {
        if (!source.isPlayer()) return Optional.empty();
        return Optional.of(source.getBukkitSender().hasPermission(permission));
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
