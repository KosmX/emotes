package io.github.kosmx.emotes.mc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.mc.services.IPermissionService;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

import java.util.*;

import static net.minecraft.commands.Commands.*;

/**
 * Server commands for Emotecraft (Fabric/Neoforge/Paper)
 * <p>
 * /emotes [play/stop]
 * - play [what ID/name] (Player) (forced:false)
 * - stop Player
 * status?
 */
public final class ServerCommands {
    public static final List<String> PERMISSIONS = List.of(
            "emotes.play.player",
            "emotes.stop.player",
            "emotes.stop.forced",
            "emotes.play.showhidden",
            "emotes.reload",
            "emotes.whitelist.toggle",
            "emotes.whitelist.reload"
    );

    public static <T> void register(CommandDispatcher<T> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        register(dispatcher, environment == CommandSelection.DEDICATED);
    }

    @SuppressWarnings("unchecked")
    public static <T> void register(CommandDispatcher<T> dispatcher, boolean isDedicated) {
        dispatcher.register((LiteralArgumentBuilder<T>) literal("emotes")
                .then(literal("play")
                        .then(argument("emote", StringArgumentType.string())
                                .suggests(new EmoteArgumentProvider(ServerCommands::getEmotes))
                                .executes(context -> {
                                    var player = context.getSource().getPlayerOrException().getUUID();
                                    boolean admin = IPermissionService.INSTANCE.check(context.getSource(), "emotes.stop.forced", 2);
                                    var emote = EmoteArgumentProvider.getEmote(getEmotes(context), context, "emote");
                                    if (!admin && ServerEmoteAPI.isForcedEmote(player))
                                        throw new SimpleCommandExceptionType(Component.literal("Can't stop forced emote without admin rights")).create();
                                    ServerEmoteAPI.playEmote(player, emote, false);
                                    return 0;
                                })
                                .then(argument("player", EntityArgument.players()).requires(IPermissionService.INSTANCE.require("emotes.play.player", 2))
                                        .executes(context -> {
                                            ServerEmoteAPI.playEmote(
                                                    EntityArgument.getPlayer(context, "player").getUUID(),
                                                    EmoteArgumentProvider.getEmote(getEmotes(context), context, "emote"),
                                                    false);
                                            return 0;
                                        })
                                        .then(argument("forced", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    ServerEmoteAPI.playEmote(
                                                            EntityArgument.getPlayer(context, "player").getUUID(),
                                                            EmoteArgumentProvider.getEmote(getEmotes(context), context, "emote"),
                                                            BoolArgumentType.getBool(context, "forced"));
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )
                .then(literal("stop")
                        .executes(context -> {
                            boolean admin = IPermissionService.INSTANCE.check(context.getSource(), "emotes.stop.forced", 2);
                            var player = context.getSource().getPlayerOrException().getUUID();
                            boolean canStop = admin || !ServerEmoteAPI.isForcedEmote(player);
                            if (canStop) {
                                ServerEmoteAPI.playEmote(player, null, false);
                                return 0;
                            }
                            throw new SimpleCommandExceptionType(Component.literal("Can't stop forced emote without admin rights")).create();
                        })
                        .then(argument("player", EntityArgument.players()).requires(IPermissionService.INSTANCE.require("emotes.stop.player", 2))
                                .executes(context -> {
                                    ServerEmoteAPI.playEmote(
                                            EntityArgument.getPlayer(context, "player").getUUID(),
                                            null,
                                            false
                                    );
                                    return 0;
                                })
                        )
                )
                .then(literal("reload").requires(ctx -> IPermissionService.INSTANCE.check(ctx, "emotes.reload", 4) && isDedicated).executes(
                        context -> {
                            UniversalEmoteSerializer.loadEmotes(); //Reload server-side emotes
                            return 0;
                        }
                ))
                .then(literal("whitelist").requires(ctx -> isDedicated)
                        .then(literal("toggle").requires(ctx -> IPermissionService.INSTANCE.check(ctx, "emotes.whitelist.toggle", 4))
                                .executes(context -> {
                                    try {
                                        Class<?> configClass = Class.forName("io.github.kosmx.emotes.common.SerializableConfig");
                                        Object config = io.github.kosmx.emotes.server.config.Serializer.getConfig();
                                        Object whitelistEntry = configClass.getField("enableEmoteWhitelist").get(config);
                                        boolean currentValue = (Boolean) whitelistEntry.getClass().getMethod("get").invoke(whitelistEntry);
                                        whitelistEntry.getClass().getMethod("set", Object.class).invoke(whitelistEntry, !currentValue);
                                        io.github.kosmx.emotes.server.config.Serializer.INSTANCE.saveConfig();
                                        context.getSource().sendSuccess(() -> Component.literal("Emote whitelist " + (!currentValue ? "enabled" : "disabled")), true);
                                        return 0;
                                    } catch (Exception e) {
                                        context.getSource().sendFailure(Component.literal("Failed to toggle whitelist: " + e.getMessage()));
                                        return -1;
                                    }
                                })
                        )
                        .then(literal("reload").requires(ctx -> IPermissionService.INSTANCE.check(ctx, "emotes.whitelist.reload", 4))
                                .executes(context -> {
                                    try {
                                        Class<?> managerClass = Class.forName("io.github.kosmx.emotes.server.moderation.EmoteWhitelistManager");
                                        Object manager = managerClass.getMethod("getInstance").invoke(null);
                                        managerClass.getMethod("loadWhitelist").invoke(manager);
                                        context.getSource().sendSuccess(() -> Component.literal("Whitelist reloaded"), true);
                                        return 0;
                                    } catch (Exception e) {
                                        context.getSource().sendFailure(Component.literal("Failed to reload whitelist: " + e.getMessage()));
                                        return -1;
                                    }
                                })
                        )
                        .then(literal("add").requires(ctx -> IPermissionService.INSTANCE.check(ctx, "emotes.whitelist.reload", 4))
                                .then(argument("hash", StringArgumentType.word())
                                    .executes(context -> {
                                        try {
                                            Class<?> managerClass = Class.forName("io.github.kosmx.emotes.server.moderation.EmoteWhitelistManager");
                                            Object manager = managerClass.getMethod("getInstance").invoke(null);
                                            String hash = StringArgumentType.getString(context, "hash");
                                            boolean result = (Boolean) managerClass.getMethod("addHashToWhitelist", String.class, String.class)
                                                .invoke(manager, hash, null);
                                            if (result) {
                                                context.getSource().sendSuccess(() -> Component.literal("Added hash " + hash + " to whitelist."), true);
                                                return 0;
                                            } else {
                                                context.getSource().sendFailure(Component.literal("Hash already in whitelist."));
                                                return -1;
                                            }
                                        } catch (Exception e) {
                                            context.getSource().sendFailure(Component.literal("Failed to add hash: " + e.getMessage()));
                                            return -1;
                                        }
                                    })
                                    .then(argument("label", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            try {
                                                Class<?> managerClass = Class.forName("io.github.kosmx.emotes.server.moderation.EmoteWhitelistManager");
                                                Object manager = managerClass.getMethod("getInstance").invoke(null);
                                                String hash = StringArgumentType.getString(context, "hash");
                                                String label = StringArgumentType.getString(context, "label");
                                                java.nio.file.Path whitelistPath = (java.nio.file.Path) managerClass.getDeclaredMethod("getWhitelistPath").invoke(manager);
                                                java.util.List<String> lines = java.nio.file.Files.readAllLines(whitelistPath);
                                                for (int i = 0; i < lines.size(); i++) {
                                                    String line = lines.get(i).trim();
                                                    if (line.isEmpty() || line.startsWith("#")) continue;
                                                    int hashEnd = line.indexOf(' ');
                                                    String fileHash = (hashEnd == -1) ? line : line.substring(0, hashEnd);
                                                    if (fileHash.equals(hash)) {
                                                        String currentLabel = null;
                                                        int labelIdx = line.indexOf("#");
                                                        if (labelIdx != -1) {
                                                            currentLabel = line.substring(labelIdx + 1).trim();
                                                        }
                                                        if (currentLabel != null && currentLabel.equals(label)) {
                                                            context.getSource().sendFailure(Component.literal("Hash already in whitelist."));
                                                            return -1;
                                                        } else {
                                                            // Overwrite label (preserve any leading whitespace before #)
                                                            String newLine = hash + " # " + label;
                                                            lines.set(i, newLine);
                                                            java.nio.file.Files.write(whitelistPath, lines);
                                                            context.getSource().sendSuccess(() -> Component.literal("Updated label for hash " + hash + " to: " + label), true);
                                                            return 0;
                                                        }
                                                    }
                                                }
                                                // If not found, add as new
                                                boolean result = (Boolean) managerClass.getMethod("addHashToWhitelist", String.class, String.class)
                                                    .invoke(manager, hash, label);
                                                if (result) {
                                                    context.getSource().sendSuccess(() -> Component.literal("Added hash " + hash + " to whitelist with label: " + label), true);
                                                    return 0;
                                                } else {
                                                    context.getSource().sendFailure(Component.literal("Hash already in whitelist."));
                                                    return -1;
                                                }
                                            } catch (Exception e) {
                                                context.getSource().sendFailure(Component.literal("Failed to add hash: " + e.getMessage()));
                                                return -1;
                                            }
                                        })
                                    )
                                )
                        )
                        .then(literal("remove").requires(ctx -> IPermissionService.INSTANCE.check(ctx, "emotes.whitelist.reload", 4))
                                .then(argument("hash", StringArgumentType.word())
                                    .executes(context -> {
                                        try {
                                            Class<?> managerClass = Class.forName("io.github.kosmx.emotes.server.moderation.EmoteWhitelistManager");
                                            Object manager = managerClass.getMethod("getInstance").invoke(null);
                                            String hash = StringArgumentType.getString(context, "hash");
                                            boolean result = (Boolean) managerClass.getMethod("removeHashFromWhitelist", String.class)
                                                .invoke(manager, hash);
                                            if (result) {
                                                context.getSource().sendSuccess(() -> Component.literal("Removed hash " + hash + " from whitelist."), true);
                                                return 0;
                                            } else {
                                                context.getSource().sendFailure(Component.literal("Hash not found in whitelist."));
                                                return -1;
                                            }
                                        } catch (Exception e) {
                                            context.getSource().sendFailure(Component.literal("Failed to remove hash: " + e.getMessage()));
                                            return -1;
                                        }
                                    })
                                    .then(argument("label", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            // Accepts but ignores the label argument
                                            try {
                                                Class<?> managerClass = Class.forName("io.github.kosmx.emotes.server.moderation.EmoteWhitelistManager");
                                                Object manager = managerClass.getMethod("getInstance").invoke(null);
                                                String hash = StringArgumentType.getString(context, "hash");
                                                boolean result = (Boolean) managerClass.getMethod("removeHashFromWhitelist", String.class)
                                                    .invoke(manager, hash);
                                                if (result) {
                                                    context.getSource().sendSuccess(() -> Component.literal("Removed hash " + hash + " from whitelist."), true);
                                                    return 0;
                                                } else {
                                                    context.getSource().sendFailure(Component.literal("Hash not found in whitelist."));
                                                    return -1;
                                                }
                                            } catch (Exception e) {
                                                context.getSource().sendFailure(Component.literal("Failed to remove hash: " + e.getMessage()));
                                                return -1;
                                            }
                                        })
                                    )
                                )
                        )
                        .then(literal("status")
                                .executes(context -> {
                                    try {
                                        Class<?> configClass = Class.forName("io.github.kosmx.emotes.common.SerializableConfig");
                                        Object config = io.github.kosmx.emotes.server.config.Serializer.getConfig();
                                        Object whitelistEntry = configClass.getField("enableEmoteWhitelist").get(config);
                                        boolean enabled = (Boolean) whitelistEntry.getClass().getMethod("get").invoke(whitelistEntry);
                                        context.getSource().sendSuccess(() -> Component.literal("Emote whitelist is " + (enabled ? "enabled" : "disabled")), false);
                                        return 0;
                                    } catch (Exception e) {
                                        context.getSource().sendFailure(Component.literal("Failed to check whitelist status: " + e.getMessage()));
                                        return -1;
                                    }
                                })
                        )
                )

        );
    }

    private static Map<UUID, Animation> getEmotes(CommandContext<CommandSourceStack> context) {
        return IPermissionService.INSTANCE.check(context.getSource(), "emotes.play.showhidden", 1) ? UniversalEmoteSerializer.getLoadedEmotes() : UniversalEmoteSerializer.SERVER_EMOTES;
    }
}
