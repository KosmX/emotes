package org.redlance.dima_dencep.mods.emotecraft.geyser.utils;

import com.zigythebird.playeranimcore.animation.Animation;
import net.kyori.adventure.text.Component;
import org.geysermc.cumulus.component.ButtonComponent;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.translator.text.MessageTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.UUID;

public class FormUtils {
    private static final Component AUTHOR = Component.translatable("emotecraft.emote.author");

    public static ButtonComponent createButtonComponent(Animation animation, String locale) {
        return ButtonComponent.of(formatAnimationName(animation, locale), FormImage.of(FormImage.Type.URL,
                String.format("https://bot.redlance.org/api/emotes/icon/%s.png#%s", animation.boneAnimations().hashCode(), animation.uuid())
        ));
    }

    private static String formatAnimationName(Animation animation, String locale) {
        if (animation.data().getRaw("name") instanceof String rawName) {
            String name = EmotecraftLocale.getLocaleString(MessageTranslator.convertMessageLenient(rawName, locale), locale); // TODO fallbacks

            /*if (animation.data().getRaw("description") instanceof String description) { // Doesn't fit
                name = String.format("%s\n%s", name, EmotecraftLocale.getLocaleString(
                        MessageTranslator.convertMessageLenient(description, locale), locale
                ));
            }*/

            if (animation.data().getRaw("author") instanceof String rawAuthor) {
                String author = EmotecraftLocale.getLocaleString(MessageTranslator.convertMessage(AUTHOR, locale), locale);
                name = String.format("%s\n(%s %s)", name, author, MessageTranslator.convertMessageLenient(rawAuthor, locale));
            }

            return name;
        }
        return String.format("INVALID: %s", animation.uuid());
    }

    @Nullable
    public static UUID extractAnimationFromButton(@NotNull ButtonComponent button) {
        FormImage image = button.image();
        if (image == null) return null;
        return extractAnimationFromImage(image);
    }

    @Nullable
    public static UUID extractAnimationFromImage(@NotNull FormImage button) {
        return switch (button.type()) {
            case URL -> UUID.fromString(URI.create(button.data()).getFragment());
            case PATH -> null; // TODO
        };
    }
}
