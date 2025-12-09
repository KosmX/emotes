package io.github.kosmx.emotes.mc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Avatar;

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
            "emotes.reload"
    );

    @SuppressWarnings("unused")
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
                                    UUID player = context.getSource().getPlayerOrException().getUUID();

                                    boolean admin = IPermissionService.INSTANCE.check(context.getSource(), "emotes.stop.forced", PermissionLevel.GAMEMASTERS);
                                    if (!admin && ServerEmoteAPI.isForcedEmote(player)) {
                                        throw new SimpleCommandExceptionType(Component.literal("Can't stop forced emote without admin rights")).create();
                                    }

                                    Animation emote = EmoteArgumentProvider.getEmote(getEmotes(context), context, "emote");
                                    ServerEmoteAPI.playEmote(player, emote, false);
                                    return 0;
                                })
                                .then(argument("avatar", EntityArgument.entities())
                                        .requires(IPermissionService.INSTANCE.require("emotes.play.player", PermissionLevel.MODERATORS))
                                        .executes(context -> {
                                            ServerEmoteAPI.playEmote(
                                                    ServerCommands.getAvatar(context, "avatar").getUUID(),
                                                    EmoteArgumentProvider.getEmote(getEmotes(context), context, "emote"),
                                                    false
                                            );
                                            return 0;
                                        })
                                        .then(argument("forced", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    ServerEmoteAPI.playEmote(
                                                            ServerCommands.getAvatar(context, "avatar").getUUID(),
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
                            boolean admin = IPermissionService.INSTANCE.check(context.getSource(), "emotes.stop.forced", PermissionLevel.GAMEMASTERS);
                            var player = context.getSource().getPlayerOrException().getUUID();
                            if (admin || !ServerEmoteAPI.isForcedEmote(player)) {
                                ServerEmoteAPI.playEmote(player, null, false);
                                return 0;
                            }
                            throw new SimpleCommandExceptionType(Component.literal("Can't stop forced emote without admin rights")).create();
                        })
                        .then(argument("avatar", EntityArgument.entity())
                                .requires(IPermissionService.INSTANCE.require("emotes.stop.player", PermissionLevel.MODERATORS))
                                .executes(context -> {
                                    ServerEmoteAPI.playEmote(
                                            ServerCommands.getAvatar(context, "avatar").getUUID(),
                                            null,
                                            false
                                    );
                                    return 0;
                                })
                        )
                )
                .then(literal("reload")
                        .requires(ctx -> IPermissionService.INSTANCE.check(ctx, "emotes.reload", PermissionLevel.ADMINS) && isDedicated)
                        .executes(
                        context -> {
                            UniversalEmoteSerializer.loadEmotes(); //Reload server-side emotes
                            return 0;
                        }
                ))

        );
    }

    private static Map<UUID, Animation> getEmotes(CommandContext<CommandSourceStack> context) {
        return IPermissionService.INSTANCE.check(context.getSource(), "emotes.play.showhidden", PermissionLevel.MODERATORS) ? UniversalEmoteSerializer.getLoadedEmotes() : UniversalEmoteSerializer.SERVER_EMOTES;
    }

    public static Avatar getAvatar(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        if (EntityArgument.getEntity(context, name) instanceof Avatar avatar) {
            return avatar;
        } else {
            throw new SimpleCommandExceptionType(Component.literal("The specified entity must be an avatar!")).create();
        }
    }
}
