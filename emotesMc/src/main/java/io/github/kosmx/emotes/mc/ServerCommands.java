package io.github.kosmx.emotes.mc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.UUID;

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
                                    boolean admin = context.getSource().hasPermission(2);
                                    var emote = EmoteArgumentProvider.getEmote(getEmotes(context), context, "emote");
                                    if (!admin && ServerEmoteAPI.isForcedEmote(player))
                                        throw new SimpleCommandExceptionType(Component.literal("Can't stop forced emote without admin rights")).create();
                                    ServerEmoteAPI.playEmote(player, emote, false);
                                    return 0;
                                })
                                .then(argument("player", EntityArgument.players()).requires(ctx -> ctx.hasPermission(2))
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
                            boolean admin = context.getSource().hasPermission(2);
                            var player = context.getSource().getPlayerOrException().getUUID();
                            boolean canStop = admin || !ServerEmoteAPI.isForcedEmote(player);
                            if (canStop) {
                                ServerEmoteAPI.playEmote(player, null, false);
                                return 0;
                            }
                            throw new SimpleCommandExceptionType(Component.literal("Can't stop forced emote without admin rights")).create();
                        })
                        .then(argument("player", EntityArgument.players()).requires(ctx -> ctx.hasPermission(2))
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
                .then(literal("reload").requires(ctx -> ctx.hasPermission(4) && isDedicated).executes(
                        context -> {
                            UniversalEmoteSerializer.loadEmotes(); //Reload server-side emotes
                            return 0;
                        }
                ))

        );
    }

    private static HashMap<UUID, KeyframeAnimation> getEmotes(CommandContext<CommandSourceStack> context) {
        return context.getSource().hasPermission(1) ? ServerEmoteAPI.getLoadedEmotes() : ServerEmoteAPI.getPublicEmotes();
    }
}
