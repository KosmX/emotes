package io.github.kosmx.emotes.hytale.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.server.core.Message;

import java.util.Locale;
import java.util.Map;

/**
 * Converts Minecraft text components into Hytale {@link Message}s.
 * <p>
 * The emote library speaks Minecraft's component JSON — the Minecraft client feeds {@code GameEmoteInfo}'s name,
 * description and author straight through {@code Component.Serializer}. Handing those strings to a Hytale label would
 * show raw JSON, so the tree is walked here instead. The two models line up closely: text and translation nodes,
 * children, and the colour/bold/italic styling all have direct counterparts.
 */
public final class McComponents {
    private McComponents() {
    }

    /** Minecraft's sixteen legacy colour names; anything else is already a {@code #rrggbb} literal. */
    private static final Map<String, String> COLORS = Map.ofEntries(
            Map.entry("black", "#000000"), Map.entry("dark_blue", "#0000aa"),
            Map.entry("dark_green", "#00aa00"), Map.entry("dark_aqua", "#00aaaa"),
            Map.entry("dark_red", "#aa0000"), Map.entry("dark_purple", "#aa00aa"),
            Map.entry("gold", "#ffaa00"), Map.entry("gray", "#aaaaaa"),
            Map.entry("dark_gray", "#555555"), Map.entry("blue", "#5555ff"),
            Map.entry("green", "#55ff55"), Map.entry("aqua", "#55ffff"),
            Map.entry("red", "#ff5555"), Map.entry("light_purple", "#ff55ff"),
            Map.entry("yellow", "#ffff55"), Map.entry("white", "#ffffff")
    );

    /**
     * @param json a serialized Minecraft component, which may also be a bare string
     * @param language the viewing player's language, as {@code PlayerRef#getLanguage} reports it
     * @return the equivalent Hytale message, or a raw message holding the input if it does not parse
     */
    public static Message toMessage(String json, String language) {
        if (json == null || json.isEmpty()) {
            return Message.raw("");
        }

        try {
            return convert(JsonParser.parseString(json), normalize(language));
        } catch (Exception e) {
            return Message.raw(json); // not a component after all - show it as the plain text it presumably is
        }
    }

    /** Locale tags are compared as Minecraft writes them: lower case with an underscore, e.g. {@code en_us}. */
    private static String normalize(String language) {
        return language == null ? "" : language.toLowerCase(Locale.ROOT).replace('-', '_');
    }

    private static Message convert(JsonElement element, String language) {
        if (element.isJsonPrimitive()) {
            return Message.raw(element.getAsString());
        }

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.isEmpty()) {
                return Message.raw("");
            }

            // A component array is its first element with the rest appended, which is exactly what insert does.
            Message message = convert(array.get(0), language);
            for (int i = 1; i < array.size(); i++) {
                message.insert(convert(array.get(i), language));
            }
            return message;
        }

        JsonObject object = element.getAsJsonObject();
        Message message = base(object, language);

        if (object.has("color")) {
            String color = object.get("color").getAsString();
            message.color(COLORS.getOrDefault(color.toLowerCase(Locale.ROOT), color));
        }
        if (object.has("bold")) {
            message.bold(object.get("bold").getAsBoolean());
        }
        if (object.has("italic")) {
            message.italic(object.get("italic").getAsBoolean());
        }

        if (object.has("extra")) {
            for (JsonElement child : object.getAsJsonArray("extra")) {
                message.insert(convert(child, language));
            }
        }
        return message;
    }

    private static Message base(JsonObject object, String language) {
        if (object.has("text")) {
            return Message.raw(object.get("text").getAsString());
        }

        if (object.has("translate")) {
            String fallback = fallback(object, language);
            if (fallback != null) {
                // Prefer the fallback outright. A Minecraft translation key means nothing to a Hytale client, so the
                // localized literal the library ships alongside it is the only thing that can actually be displayed.
                return Message.raw(fallback);
            }

            String key = object.get("translate").getAsString();
            Message message = Message.translation(key);

            // Minecraft substitutes positionally, Hytale by name. Numbered names keep the arguments addressable, so a
            // Hytale-side .lang can render them; an untranslated key at least still carries its arguments.
            if (object.has("with")) {
                JsonArray with = object.getAsJsonArray("with");
                for (int i = 0; i < with.size(); i++) {
                    message.param(String.valueOf(i), convert(with.get(i), language));
                }
            }
            return message;
        }

        return Message.raw("");
    }

    /**
     * Picks the localized literal for a translation node.
     * <p>
     * Vanilla Minecraft allows a single {@code fallback}; the emote library instead ships a {@code fallbacks} map of
     * locale to text, which on Minecraft needs the TranslationFallbacks mod because components resolve client-side.
     * Here no such mod is needed: pages are built per player on the server, so the viewer's own language picks the
     * entry directly, with the vanilla single fallback as the last resort.
     */
    private static String fallback(JsonObject object, String language) {
        if (object.get("fallbacks") instanceof JsonObject fallbacks) {
            JsonElement localized = fallbacks.get(language);
            if (localized != null && localized.isJsonPrimitive()) {
                return localized.getAsString();
            }
        }

        return object.has("fallback") ? object.get("fallback").getAsString() : null;
    }
}
