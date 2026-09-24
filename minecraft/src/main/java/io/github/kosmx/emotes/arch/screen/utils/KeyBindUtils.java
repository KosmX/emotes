package io.github.kosmx.emotes.arch.screen.utils;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.UUID;

/**
 * Emote key binds: one key per emote and one emote per key. The binding stores the emote itself, not its UUID.
 */
public final class KeyBindUtils {
    private static final Component SURE = Component.translatable("emotecraft.sure");
    private static final Component SURE2 = Component.translatable("emotecraft.sure2");

    private KeyBindUtils() {}

    /** @return the key {@code emoteId} is bound to, matching by emote id against the stored holders, or UNKNOWN. */
    public static InputConstants.@NonNull Key getKey(UUID emoteId) {
        for (Map.Entry<InputConstants.Key, EmoteHolder> entry : PlatformTools.getConfig().keyBinds.entrySet()) {
            if (entry.getValue().getUuid().equals(emoteId)) return entry.getKey();
        }
        return InputConstants.UNKNOWN;
    }

    /** @return the name of {@code key}, highlighted like vanilla while the key is being waited for. */
    public static Component getKeyMessage(InputConstants.Key key, boolean waiting) {
        if (!waiting) return key.getDisplayName();
        return Component.literal("> ")
                .append(key.getDisplayName().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                .append(" <")
                .withStyle(ChatFormatting.YELLOW);
    }

    public static void unbind(UUID emoteId) {
        PlatformTools.getConfig().keyBinds.values().removeIf(holder -> holder.getUuid().equals(emoteId));
    }

    /**
     * Binds {@code emote} to {@code key}, UNKNOWN unbinds it. If the key already plays another emote,
     * asks to overwrite that bind first and returns to {@code screen} afterwards.
     *
     * @param onBound runs once the binds have changed
     */
    public static void bind(Screen screen, EmoteHolder emote, InputConstants.Key key, Runnable onBound) {
        EmoteHolder current = key.equals(InputConstants.UNKNOWN) ? null : PlatformTools.getConfig().keyBinds.get(key);
        if (current == null || current.getUuid().equals(emote.getUuid())) {
            forceBind(emote, key);
            onBound.run();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(new ConfirmScreen(choice -> {
            if (choice) {
                forceBind(emote, key);
                onBound.run();
            }
            minecraft.gui.setScreen(screen);
        }, SURE, SURE2));
    }

    private static void forceBind(EmoteHolder emote, InputConstants.Key key) {
        unbind(emote.getUuid());                                // one key per emote: drop its previous bind
        if (!key.equals(InputConstants.UNKNOWN)) {
            PlatformTools.getConfig().keyBinds.put(key, emote); // one emote per key: overwrites any conflicting bind
        }
    }
}
