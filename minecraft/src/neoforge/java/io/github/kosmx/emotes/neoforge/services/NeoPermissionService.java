package io.github.kosmx.emotes.neoforge.services;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.mc.ServerCommands;
import io.github.kosmx.emotes.mc.services.IPermissionService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@EventBusSubscriber(modid = CommonData.MOD_ID, value = Dist.DEDICATED_SERVER)
public class NeoPermissionService implements IPermissionService {
    private static final Map<Identifier, PermissionNode<Boolean>> NODES = new HashMap<>();

    @Override
    public Optional<Boolean> getPermissionValue(@NotNull CommandSourceStack source, @NotNull Identifier permission) {
        if (!NeoPermissionService.NODES.containsKey(permission) || !source.isPlayer()) {
            return Optional.empty();
        }
        return Optional.of(PermissionAPI.getPermission(
                Objects.requireNonNull(source.getPlayer()), NODES.get(permission)
        ));
    }

    @Override
    public boolean isServiceActive() {
        try {
            return FMLLoader.getCurrent().getDist().isDedicatedServer();
        } catch (Exception ex) {
            return false;
        }
    }

    @SubscribeEvent
    public static void onRegisterPermissionNodes(PermissionGatherEvent.Nodes event) {
        for (Identifier permission : ServerCommands.PERMISSIONS) {
            PermissionNode<Boolean> node = new PermissionNode<>(permission, PermissionTypes.BOOLEAN,
                    (_, _, _) -> false
            );

            event.addNodes(node);
            NeoPermissionService.NODES.put(permission, node);
        }
    }
}
