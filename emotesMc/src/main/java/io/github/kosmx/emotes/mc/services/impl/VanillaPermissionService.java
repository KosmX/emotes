package io.github.kosmx.emotes.mc.services.impl;

import io.github.kosmx.emotes.mc.services.IPermissionService;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import org.redlance.common.services.ServiceUtils;

import java.util.Optional;

public class VanillaPermissionService implements IPermissionService {
    @Override
    public Optional<Boolean> getPermissionValue(@NotNull CommandSourceStack source, @NotNull String permission) {
        return Optional.empty();
    }

    @Override
    public boolean isServiceActive() {
        return true;
    }

    @Override
    public int getPriority() {
        return ServiceUtils.LOWEST_PRIORITY;
    }
}
