package io.github.kosmx.emotes.arch.gui.toast;

import io.github.kosmx.emotes.arch.screen.utils.EmotecraftTexture;
import io.github.kosmx.emotes.arch.screen.utils.WidgetOutliner;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A toast on the mod's own panel — a tiled {@link EmotecraftTexture#MENU_LIST_BACKGROUND} behind
 * {@link WidgetOutliner}'s separators — with an icon on the left and the text beside it.
 */
public class EmotecraftToast implements Toast {
    private static final Identifier MOD_ICON = McUtils.newIdentifier("textures/emotecraft_mod_logo.png");
    private static final long DEFAULT_DISPLAY_TIME = 5000L;

    private static final int GAP = 4;     // between stacked panels
    private static final int PADDING = 2; // around the icon, and from the text to the panel's right edge
    /** Keeps a plain two-line toast one slot tall; a taller panel grows its icon to match. */
    private static final int MIN_ICON_SIZE = Toast.SLOT_HEIGHT - GAP - PADDING * 2;

    /** The tallest panel the manager could place: every slot it has, less the gap the last one reserves. */
    private static final int MAX_HEIGHT = ToastManager.SLOT_COUNT * Toast.SLOT_HEIGHT - GAP;
    /** And so the most text one can hold — beyond it a toast would be queued and never shown. */
    private static final int MAX_LINES = (MAX_HEIGHT - PADDING * 2) / SystemToast.LINE_SPACING;
    private static final FormattedCharSequence ELLIPSIS = CommonComponents.ELLIPSIS.getVisualOrderText();

    private final long displayTime;
    private final Identifier icon;
    private final List<FormattedCharSequence> titleLines;
    private final List<FormattedCharSequence> messageLines;
    private final int width;
    private final int height;
    private final int iconSize;

    private Toast.Visibility wantedVisibility = Toast.Visibility.HIDE;

    /** Raises a toast under the mod's own icon. */
    public static void add(ToastManager manager, Component title, @Nullable Component message) {
        manager.addToast(new EmotecraftToast(manager, MOD_ICON, DEFAULT_DISPLAY_TIME, title, message));
    }

    public EmotecraftToast(ToastManager manager, Identifier icon, long displayTime, Component title, @Nullable Component message) {
        this.icon = icon;
        this.displayTime = displayTime;

        Font font = manager.getMinecraft().font;
        // A notification's text comes from the library, so it has to be bounded: the manager can never place a
        // panel needing more slots than it owns, and would keep it queued forever. The title is a headline, so
        // it gets half the room at most and can't crowd the message out.
        this.titleLines = clampLines(font.split(title, SystemToast.MAX_LINE_SIZE), message == null ? MAX_LINES : MAX_LINES / 2);
        this.messageLines = message == null ? List.of()
                : clampLines(font.split(message, SystemToast.MAX_LINE_SIZE), MAX_LINES - this.titleLines.size());

        int textWidth = 0;
        for (FormattedCharSequence line : this.titleLines) textWidth = Math.max(textWidth, font.width(line));
        for (FormattedCharSequence line : this.messageLines) textWidth = Math.max(textWidth, font.width(line));

        this.height = Math.max(MIN_ICON_SIZE, textHeight()) + PADDING * 2;
        this.iconSize = this.height - PADDING * 2; // square, PADDING from the panel on every side
        this.width = Math.max(Toast.DEFAULT_WIDTH, textX() + textWidth + PADDING);
    }

    /** @return the lines, cut to {@code max} with the last one ending in an ellipsis to show what was dropped. */
    private static List<FormattedCharSequence> clampLines(List<FormattedCharSequence> lines, int max) {
        if (lines.size() <= max) {
            return lines;
        }

        List<FormattedCharSequence> kept = new ArrayList<>(lines.subList(0, max));
        kept.set(max - 1, FormattedCharSequence.composite(kept.get(max - 1), ELLIPSIS));
        return kept;
    }

    private int textHeight() {
        return (this.titleLines.size() + this.messageLines.size()) * SystemToast.LINE_SPACING;
    }

    private int textX() {
        return PADDING + this.iconSize + PADDING;
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    /** The panel reserves the slots it covers plus the {@link #GAP} that separates it from the toast below. */
    @Override
    public int occcupiedSlotCount() {
        return Mth.positiveCeilDiv(this.height + GAP, Toast.SLOT_HEIGHT);
    }

    /** {@link Toast#yPos} spaces by the toast's own height, taking the panel off the slot grid every other
     * toast — vanilla's included — is placed on. */
    @Override
    public float yPos(int firstSlotIndex) {
        return firstSlotIndex * Toast.SLOT_HEIGHT;
    }

    @Override
    public Toast.@NonNull Visibility getWantedVisibility() {
        return this.wantedVisibility;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        this.wantedVisibility = fullyVisibleForMs < this.displayTime * manager.getNotificationDisplayTimeMultiplier()
                ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, @NonNull Font font, long fullyVisibleForMs) {
        Identifier background = EmotecraftTexture.MENU_LIST_BACKGROUND.identifier(Minecraft.getInstance().level != null);
        graphics.blit(RenderPipelines.GUI_TEXTURED, background, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
        WidgetOutliner.extractOutline(graphics, 0, 0, this.width, this.height, -1);

        // Source and texture size are equal, so the icon maps whole onto the square whatever its resolution.
        graphics.blit(RenderPipelines.GUI_TEXTURED, this.icon, PADDING, PADDING, 0.0F, 0.0F,
                this.iconSize, this.iconSize, this.iconSize, this.iconSize, this.iconSize, this.iconSize
        );

        int y = (this.height - textHeight()) / 2 + 1;
        for (FormattedCharSequence line : this.titleLines) {
            graphics.text(font, line, textX(), y, CommonColors.WHITE, false);
            y += SystemToast.LINE_SPACING;
        }
        for (FormattedCharSequence line : this.messageLines) {
            graphics.text(font, line, textX(), y, CommonColors.LIGHT_GRAY, false);
            y += SystemToast.LINE_SPACING;
        }
    }
}
