package io.github.kosmx.emotes.mc.services;

import io.github.kosmx.emotes.mc.services.impl.VanillaPermissionService;
import io.github.kosmx.emotes.services.IEmotecraftService;
import io.github.kosmx.emotes.services.ServiceLoaderUtil;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public interface IPermissionService extends IEmotecraftService {
    IPermissionService LOADED_SERVICE = ServiceLoaderUtil.loadService(IPermissionService.class, VanillaPermissionService::new);

    @Override
    default String getName() {
        return "Permission" + IEmotecraftService.super.getName();
    }

    default Predicate<CommandSourceStack> require(@NotNull String permission, int defaultValue) {
        Objects.requireNonNull(permission, "permission");
        return player -> check(player, permission, defaultValue);
    }

    default boolean check(@NotNull CommandSourceStack source, @NotNull String permission, int defaultValue) {
        return getPermissionValue(source, permission).orElseGet(() -> source.hasPermission(defaultValue));
    }

    Optional<Boolean> getPermissionValue(@NotNull CommandSourceStack source, @NotNull String permission);
}
