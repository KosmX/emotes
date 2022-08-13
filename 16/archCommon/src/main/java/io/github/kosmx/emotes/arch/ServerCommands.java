package io.github.kosmx.emotes.arch;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TextComponent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Server commands for Emotecraft
 * Fabric+Forge, should be identical to bukkit
 * <p>
 * /emotes [play/stop]
 * - play [what ID/name] (Player) (forced:false)
 * - stop Player
 * status?
 */
public final class ServerCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean bl) {
        dispatcher.register(literal("emotes")
                .then(literal("play")
                        .then(argument("emote", StringArgumentType.string()).suggests(new EmoteArgumentProvider())
                                .executes(context -> {
                                    UUID player = context.getSource().getPlayerOrException().getUUID();
                                    boolean admin = context.getSource().hasPermission(2);
                                    KeyframeAnimation emote = EmoteArgumentProvider.getEmote(context, "emote");
                                    if (!admin && ServerEmoteAPI.isForcedEmote(player))
                                        throw new SimpleCommandExceptionType(new TextComponent("Can't stop forced emote without admin rights")).create();
                                    ServerEmoteAPI.playEmote(player, emote, false);
                                    return 0;
                                })
                                .then(argument("player", EntityArgument.players()).requires(ctx -> ctx.hasPermission(2))
                                        .executes(context -> {
                                            ServerEmoteAPI.playEmote(
                                                    EntityArgument.getPlayer(context, "player").getUUID(),
                                                    EmoteArgumentProvider.getEmote(context, "emote"),
                                                    false);
                                            return 0;
                                        })
                                        .then(argument("forced", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    ServerEmoteAPI.playEmote(
                                                            EntityArgument.getPlayer(context, "player").getUUID(),
                                                            EmoteArgumentProvider.getEmote(context, "emote"),
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
                            UUID player = context.getSource().getPlayerOrException().getUUID();
                            boolean canStop = admin || !ServerEmoteAPI.isForcedEmote(player);
                            if (canStop) {
                                ServerEmoteAPI.playEmote(player, null, false);
                                return 0;
                            }
                            throw new SimpleCommandExceptionType(new TextComponent("Can't stop forced emote without admin rights")).create();
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
                .then(literal("reload").requires(ctx -> bl).executes(
                        context -> {
                            UniversalEmoteSerializer.loadEmotes(); //Reload server-side emotes
                            return 0;
                        }
                ))

        );
    }

    private static class EmoteArgumentProvider implements SuggestionProvider<CommandSourceStack> {

        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
            HashMap<UUID, KeyframeAnimation> emotes = getEmotes(context.getSource().hasPermission(1));

            List<String> suggestions = new LinkedList<>();
            for (KeyframeAnimation emote : emotes.values()) {
                if (emote.extraData.containsKey("name")) {
                    String name = EmoteInstance.instance.getDefaults().fromJson(emote.extraData.get("name")).getString();
                    if (name.contains(" ")) {
                        name = "\"" + name + "\"";
                    }
                    suggestions.add(name);
                } else {
                    suggestions.add(emote.getUuid().toString());
                }
            }

            return SharedSuggestionProvider.suggest(suggestions.toArray(new String[0]), builder);
        }

        private static HashMap<UUID, KeyframeAnimation> getEmotes(boolean allowHidden) {
            return allowHidden ? ServerEmoteAPI.getLoadedEmotes() : ServerEmoteAPI.getPublicEmotes();
        }

        public static KeyframeAnimation getEmote(CommandContext<CommandSourceStack> context, String argumentName) throws CommandSyntaxException {
            String id = StringArgumentType.getString(context, argumentName);
            HashMap<UUID, KeyframeAnimation> emotes = getEmotes(context.getSource().hasPermission(1));
            try {
                UUID emoteID = UUID.fromString(id);
                KeyframeAnimation emote = emotes.get(emoteID);
                if (emote == null) throw new SimpleCommandExceptionType(new TextComponent("No emote with ID: " + emoteID)).create();
                return emote;
            } catch(IllegalArgumentException ignore) {} //Not a UUID

            for (KeyframeAnimation emote : emotes.values()) {
                if (emote.extraData.containsKey("name")) {
                    String name = EmoteInstance.instance.getDefaults().fromJson(emote.extraData.get("name")).getString();
                    if (name.equals(id)) return emote;
                }
            }
            throw new SimpleCommandExceptionType(new TextComponent("Not emote with name: " + id)).create();
        }
    }
}
