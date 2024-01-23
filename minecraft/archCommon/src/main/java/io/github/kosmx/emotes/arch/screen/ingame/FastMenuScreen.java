package io.github.kosmx.emotes.arch.screen.ingame;

import dev.kosmx.playerAnim.core.util.MathHelper;
import io.github.kosmx.emotes.arch.screen.EmoteConfigScreen;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.IChooseWheel;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.inline.TmpGetters;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.network.ClientPacketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Stuff to override
 * isPauseScreen return false
 * render
 */
public class FastMenuScreen extends EmoteConfigScreen {
    private FastMenuWidget widget;
    private static final Component warn_no_emotecraft = Component.translatable("emotecraft.no_server");
    private static final Component warn_only_proxy = Component.translatable("emotecraft.only_proxy");

    public FastMenuScreen(Screen screen) {
        super(Component.translatable("emotecraft.fastmenu"), screen);
    }


    @Override
    public void init(){
        int x = (int) Math.min(getWidth() * 0.8, getHeight() * 0.8);
        this.widget = newFastMenuWidget((getWidth() - x) / 2, (getHeight() - x) / 2, x);
        addToChildren(widget);
        //this.buttons.add(new ButtonWidget(this.width - 120, this.height - 30, 96, 20, new TranslatableText("emotecraft.config"), (button -> this.client.openScreen(new EmoteMenu(this)))));
        int x1 = getWidth() - 120;
        int y = getHeight() - 30;
        Component msg = Component.translatable("emotecraft.emotelist");
        addRenderableWidget(Button.builder(msg, (button -> getMinecraft().setScreen(newFullScreenMenu()))).pos(x1, y).size(96, 20).build());
    }

    private Screen newFullScreenMenu() {
        return new FullMenuScreenHelper(this);
    }


    @Override
    public void render(GuiGraphics matrices, int mouseX, int mouseY, float delta){
        renderBackground(matrices);
        widget.render(matrices, mouseX, mouseY, delta);
        if(!((ClientConfig)EmoteInstance.config).hideWarningMessage.get()) {
            int remoteVer = ClientPacketManager.isRemoteAvailable() ? 2 : ClientPacketManager.isAvailableProxy() ? 1 : 0;
            if (remoteVer != 2) {
                Component text = remoteVer == 0 ? warn_no_emotecraft : warn_only_proxy;
                int centerX = getWidth() / 2;
                int y = getHeight() / 24 - 1;
                matrices.drawCenteredString(Minecraft.getInstance().font, text, centerX, y, MathHelper.colorHelper(255, 255, 255, 255));
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    protected FastMenuWidget newFastMenuWidget(int x, int y, int size) {
        return new FastMenuWidget(x, y, size);
    }

    protected static class FastMenuWidget extends AbstractFastChooseWidget {

        public FastMenuWidget(int x, int y, int size){
            super(x, y, size);
        }

        @Override
        protected boolean doHoverPart(IChooseWheel.IChooseElement part){
            return part.hasEmote();
        }

        @Override
        protected boolean isValidClickButton(int button){
            return button == 0;
        }

        @Override
        protected boolean onClick(IChooseWheel.IChooseElement element, int button){
            if(element.getEmote() != null){
                boolean bl = element.getEmote().playEmote(TmpGetters.getClientMethods().getMainPlayer());
                Minecraft.getInstance().setScreen(null);
                return bl;
            }
            return false;
        }

        @Override
        protected boolean doesShowInvalid() {
            return false;
        }


        private boolean focused = false;


        @Override
        public void setFocused(boolean bl) {
            focused = bl;
        }

        @Override
        public boolean isFocused() {
            return focused;
        }
    }
}
