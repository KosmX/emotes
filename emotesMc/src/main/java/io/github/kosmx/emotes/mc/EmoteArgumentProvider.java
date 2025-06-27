package io.github.kosmx.emotes.mc;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.zigythebird.playeranimcore.animation.Animation;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class EmoteArgumentProvider implements SuggestionProvider<CommandSourceStack> {
    public final Function<CommandContext<CommandSourceStack>, Map<UUID, Animation>> emotes;
    public final HolderLookup.Provider registries;

    public EmoteArgumentProvider(Function<CommandContext<CommandSourceStack>, Map<UUID, Animation>> emotes) {
        this(RegistryAccess.EMPTY, emotes);
    }

    public EmoteArgumentProvider(HolderLookup.Provider registries, Function<CommandContext<CommandSourceStack>, Map<UUID, Animation>> emotes) {
        this.emotes = emotes;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> suggestions = new LinkedList<>();
        for (var emote : emotes.apply(context).values()) {
            if (emote.data().has("name")) {
                String name = McUtils.fromJson(emote.data().getRaw("name"), this.registries).getString();
                if (name.contains(" ")) {
                    name = "\"" + name + "\"";
                }
                suggestions.add(StringUtil.filterText(name));
            } else {
                suggestions.add(emote.uuid().toString());
            }
        }

        return SharedSuggestionProvider.suggest(suggestions.toArray(String[]::new), builder);
    }

    public static Animation getEmote(Map<UUID, Animation> emotes, CommandContext<CommandSourceStack> context, String argumentName) throws CommandSyntaxException {
        String id = StringArgumentType.getString(context, argumentName);
        try {
            UUID emoteID = UUID.fromString(id);
            Animation emote = emotes.get(emoteID);
            if (emote == null) throw new SimpleCommandExceptionType(Component.literal("No emote with ID: " + emoteID)).create();
            return emote;
        } catch(IllegalArgumentException ignore) {} //Not a UUID

        for (var emote : emotes.values()) {
            if (emote.data().has("name")) {
                String name = StringUtil.filterText(McUtils.fromJson(emote.data().getRaw("name"), RegistryAccess.EMPTY).getString());
                if (name.equals(id)) return emote;
            }
        }
        throw new SimpleCommandExceptionType(Component.literal("Not emote with name: " + id)).create();
    }
}
