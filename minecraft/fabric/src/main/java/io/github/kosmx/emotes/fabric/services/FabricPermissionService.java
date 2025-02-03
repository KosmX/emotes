package io.github.kosmx.emotes.fabric.services;

import io.github.kosmx.emotes.mc.services.IPermissionService;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FabricPermissionService implements IPermissionService {
    @Override
    public Optional<Boolean> getPermissionValue(@NotNull CommandSourceStack source, @NotNull String permission) {
        return Permissions.getPermissionValue(source, permission).map(b -> b);
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
