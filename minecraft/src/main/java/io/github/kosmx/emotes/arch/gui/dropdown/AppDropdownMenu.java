package io.github.kosmx.emotes.arch.gui.dropdown;

import com.mojang.blaze3d.platform.Window;
import io.github.kosmx.emotes.arch.gui.animation.Animatable;
import io.github.kosmx.emotes.arch.gui.animation.MotionScheme;
import io.github.kosmx.emotes.arch.screen.utils.WidgetOutliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Port of the library app's AppDropdownMenu, shown on its screen's {@link DropdownLayer}. */
public final class AppDropdownMenu extends AbstractContainerEventHandler implements Renderable {
    static final int ITEM_HEIGHT = 18;
    private static final int GROUP_PADDING = 1;
    private static final int GROUP_GAP = 3 + 2 * WidgetOutliner.REACH;
    private static final int ITEM_INSET = 1;
    private static final int MIN_ITEM_WIDTH = 64;
    private static final int MAX_ITEM_WIDTH = Button.BIG_WIDTH;
    private static final int MARGIN_X = 4; // Material's 8 dp from the window's sides, at a vanilla button's 20 px for its 44 dp item
    private static final int MARGIN_Y = 22; // and its 48 dp from the top and bottom

    private static final float CLOSED_SCALE = 0.8F;

    private final DropdownLayer layer;
    private final Supplier<ScreenRectangle> anchor;
    private final Runnable onDismissRequest;
    private final Consumer<MenuScope> content;

    private List<List<DropdownMenuItem>> groups = Collections.emptyList();
    private List<DropdownMenuItem> items = Collections.emptyList();
    private ScreenRectangle bounds = ScreenRectangle.empty();
    private int screenWidth;
    private int screenHeight;
    private float originX;
    private float originY;

    private boolean expanded;
    private final Animatable scale = new Animatable(CLOSED_SCALE, MotionScheme.FAST_SPATIAL);
    private final Animatable alpha = new Animatable(0.0F, MotionScheme.FAST_EFFECTS);

    /**
     * @param anchor           read again whenever the screen is laid out
     * @param onDismissRequest called on a click outside the menu or on escape; the menu stays open until {@link #setExpanded} closes it
     * @param content          collects the groups on every opening
     */
    public AppDropdownMenu(DropdownLayer layer, Supplier<ScreenRectangle> anchor, Runnable onDismissRequest, Consumer<MenuScope> content) {
        this.layer = layer;
        this.anchor = anchor;
        this.onDismissRequest = onDismissRequest;
        this.content = content;
    }

    public void setExpanded(boolean expanded) {
        boolean shown = this.layer.isShowing(this);
        if (expanded == (this.expanded && shown)) return;
        if (expanded) {
            if (!shown) {
                MenuScope scope = new MenuScope();
                this.content.accept(scope);
                this.groups = scope.groups;
                this.items = scope.groups.stream().flatMap(List::stream).toList();
                setFocused(null);
            }
            Window window = Minecraft.getInstance().getWindow();
            arrange(window.getGuiScaledWidth(), window.getGuiScaledHeight());
            this.layer.show(this);
        }
        this.expanded = expanded;
        this.scale.animateTo(expanded ? 1.0F : CLOSED_SCALE);
        this.alpha.animateTo(expanded ? 1.0F : 0.0F);
    }

    boolean isExpanded() {
        return this.expanded;
    }

    boolean isClosed() {
        return this.alpha.isAt(0.0F);
    }

    private void arrange(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        int itemWidth = MIN_ITEM_WIDTH;
        int height = GROUP_GAP * Math.max(0, this.groups.size() - 1);
        for (List<DropdownMenuItem> group : this.groups) {
            height += group.size() * ITEM_HEIGHT + 2 * (GROUP_PADDING + WidgetOutliner.REACH);
            for (DropdownMenuItem item : group) {
                itemWidth = Math.max(itemWidth, item.contentWidth(Minecraft.getInstance().font));
            }
        }
        itemWidth = Math.min(itemWidth, MAX_ITEM_WIDTH);
        int width = itemWidth + 2 * (ITEM_INSET + WidgetOutliner.REACH);

        // MenuAnchorPosition.Below: from the anchor's start, else its end; under it, else over it or centered on its top; else at the nearer window edge
        ScreenRectangle anchor = this.anchor.get();
        int anchorCenterX = (anchor.left() + anchor.right()) / 2;
        int anchorCenterY = (anchor.top() + anchor.bottom()) / 2;
        int x = position(width, screenWidth, MARGIN_X,
                anchor.left(), anchor.right() - width, anchorCenterX < screenWidth / 2 ? 0 : screenWidth - width
        );
        int y = position(height, screenHeight, MARGIN_Y,
                anchor.bottom(), anchor.top() - height, anchor.top() - height / 2, anchorCenterY < screenHeight / 2 ? 0 : screenHeight - height
        );
        this.bounds = new ScreenRectangle(x, y, width, height);
        this.originX = origin(x, x + width, anchor.left(), anchor.right());
        this.originY = origin(y, y + height, anchor.top(), anchor.bottom());

        int itemY = y;
        for (List<DropdownMenuItem> group : this.groups) {
            itemY += WidgetOutliner.REACH + GROUP_PADDING;
            for (DropdownMenuItem item : group) {
                item.setRectangle(itemWidth, ITEM_HEIGHT, x + WidgetOutliner.REACH + ITEM_INSET, itemY);
                itemY += ITEM_HEIGHT;
            }
            itemY += GROUP_PADDING + WidgetOutliner.REACH + GROUP_GAP;
        }
    }

    private static int position(int size, int window, int margin, int... candidates) {
        for (int candidate : candidates) {
            if (candidate >= margin && candidate + size <= window - margin) return candidate;
        }
        if (size >= window - 2 * margin) return (window - size) / 2;
        return Mth.clamp(candidates[candidates.length - 1], margin, window - margin - size);
    }

    private static float origin(int start, int end, int anchorStart, int anchorEnd) {
        if (start >= anchorEnd) return start;
        if (end <= anchorStart) return end;
        return (Math.max(start, anchorStart) + Math.min(end, anchorEnd)) / 2.0F;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (graphics.guiWidth() != this.screenWidth || graphics.guiHeight() != this.screenHeight) {
            arrange(graphics.guiWidth(), graphics.guiHeight());
        }
        float alpha = this.alpha.get();
        graphics.pose().pushMatrix();
        graphics.pose().scaleAround(this.scale.get(), this.originX, this.originY);
        for (List<DropdownMenuItem> group : this.groups) {
            extractGroup(graphics, group, alpha);
            for (DropdownMenuItem item : group) {
                item.setAlpha(alpha);
                item.extractRenderState(graphics, mouseX, mouseY, a);
            }
        }
        graphics.pose().popMatrix();
    }

    private void extractGroup(GuiGraphicsExtractor graphics, List<DropdownMenuItem> group, float alpha) {
        int x = this.bounds.left();
        int y = group.getFirst().getY() - GROUP_PADDING - WidgetOutliner.REACH;
        int width = this.bounds.width();
        int height = group.size() * ITEM_HEIGHT + 2 * (GROUP_PADDING + WidgetOutliner.REACH);
        WidgetOutliner.extractPanel(graphics, x, y, width, height, alpha);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (!this.expanded) return false;
        if (isMouseOver(event.x(), event.y())) {
            super.mouseClicked(event, doubleClick);
        } else {
            this.onDismissRequest.run();
        }
        return true;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (!this.expanded) return false;
        if (event.isEscape()) {
            this.onDismissRequest.run();
        } else if (event.isCycleFocus()) {
            moveFocus(new FocusNavigationEvent.TabNavigation(!event.hasShiftDown()));
        } else if (event.isUp() || event.isDown()) {
            moveFocus(new FocusNavigationEvent.ArrowNavigation(event.isUp() ? ScreenDirection.UP : ScreenDirection.DOWN));
        } else {
            super.keyPressed(event);
        }
        return true; // While the menu is shown, no key press reaches the screen under it
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.bounds.containsPoint((int) mouseX, (int) mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.expanded; // While the menu is shown, nothing under it scrolls
    }

    private void moveFocus(FocusNavigationEvent navigation) {
        ComponentPath path = nextFocusPath(navigation);
        if (path != null) path.applyFocus(true);
    }

    void updateNarration(NarrationElementOutput output) {
        Screen.NarratableSearchResult narratable = Screen.findNarratableWidget(this.items, null);
        if (narratable != null) narratable.entry().updateNarration(output.nest());
    }

    @Override
    public @NonNull List<? extends GuiEventListener> children() {
        return this.items;
    }

    @Override
    public @NonNull ScreenRectangle getRectangle() {
        return this.bounds; // The first arrow press starts from the menu's edge
    }

    public static final class MenuScope {
        private final List<List<DropdownMenuItem>> groups = new ArrayList<>();

        private MenuScope() {}

        public void group(Consumer<GroupScope> content) {
            GroupScope group = new GroupScope();
            content.accept(group);
            if (!group.items.isEmpty()) this.groups.add(group.items.stream().map(item -> item.apply(group.hasChoice)).toList());
        }
    }

    public static final class GroupScope {
        private final List<Function<Boolean, DropdownMenuItem>> items = new ArrayList<>();
        private boolean hasChoice;

        private GroupScope() {}

        public void item(Component text, Runnable onClick) {
            this.items.add(checkColumn -> new DropdownMenuItem(text, false, checkColumn, onClick));
        }

        public void selectable(boolean selected, Component text, Runnable onClick) {
            this.hasChoice = true;
            this.items.add(checkColumn -> new DropdownMenuItem(text, selected, checkColumn, onClick));
        }
    }
}
