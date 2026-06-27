package io.github.kosmx.emotes.fabric.services;

import io.github.kosmx.emotes.mc.services.IPermissionService;
import net.fabricmc.fabric.api.permission.v1.PermissionContextOwner;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class FabricPermissionService implements IPermissionService {
    @Override
    public Optional<Boolean> getPermissionValue(@NotNull CommandSourceStack source, @NotNull Identifier permission) {
        return ((PermissionContextOwner) source).getPermissionContext().checkPermission(permission).map(b -> b);
    }

    @Override
    public boolean isServiceActive() {
        return FabricLoader.getInstance().isModLoaded("fabric-permission-api-v1");
    }
}
