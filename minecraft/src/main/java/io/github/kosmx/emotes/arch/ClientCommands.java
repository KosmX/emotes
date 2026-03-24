package io.github.kosmx.emotes.arch;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.mc.EmoteArgumentProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

/**
 * Client-side commands, no permission verification, we're on the client
 */
public class ClientCommands {
    public static final Component FORCED = Component.translatable("emotecraft.cant.override.forced");

    @SuppressWarnings({"unchecked","unused"})
    public static <T> void register(CommandDispatcher<T> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register((LiteralArgumentBuilder<T>) literal("emotes-client")
                .then(literal("play")
                        .then(argument("emote", StringArgumentType.string()).suggests(new EmoteArgumentProvider(ClientCommands::getEmotes))
                                .executes(ctx -> {
                                    if (!ClientEmotePlay.clientStartLocalEmote(EmoteArgumentProvider.getEmote(getEmotes(ctx), ctx, "emote"))) {
                                        throw new SimpleCommandExceptionType(FORCED).create();
                                    }
                                    return 0;
                                })
                        )
                )
                .then(literal("stop")
                        .executes(ctx -> {
                                    if (ClientEmotePlay.isForcedEmote())
                                        throw new SimpleCommandExceptionType(FORCED).create();
                                    ClientEmotePlay.clientStopLocalEmote();
                                    return 0;
                                }
                        )
                )
        );
    }

    private static Map<UUID, Animation> getEmotes(CommandContext<CommandSourceStack> context) {
        return EmoteHolder.list.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, entry -> entry.getValue().emote
        ));
    }
}