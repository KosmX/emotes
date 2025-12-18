package io.github.kosmx.emotes.mc.services;

import io.github.kosmx.emotes.api.services.IEmotecraftService;
import io.github.kosmx.emotes.common.tools.ServiceLoaderUtil;
import io.github.kosmx.emotes.mc.services.impl.VanillaPermissionService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public interface IPermissionService extends IEmotecraftService {
    IPermissionService INSTANCE = ServiceLoaderUtil.loadService(IPermissionService.class, VanillaPermissionService::new);

    default Predicate<CommandSourceStack> require(@NotNull String permission, PermissionLevel defaultValue) {
        Objects.requireNonNull(permission, "permission");
        return player -> check(player, permission, defaultValue);
    }

    default boolean check(@NotNull CommandSourceStack source, @NotNull String permission, PermissionLevel defaultValue) {
        return getPermissionValue(source, permission).orElseGet(() -> source.permissions().hasPermission(new Permission.HasCommandLevel(defaultValue)));
    }

    Optional<Boolean> getPermissionValue(@NotNull CommandSourceStack source, @NotNull String permission);
}
