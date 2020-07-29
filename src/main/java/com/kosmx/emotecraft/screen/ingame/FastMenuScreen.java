package com.kosmx.emotecraft.screen.ingame;

import com.kosmx.emotecraft.screen.widget.AbstractFastChooseWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class FastMenuScreen extends Screen {
    private FastMenuWidget widget;

    public FastMenuScreen(Text title) {
        super(title);
    }

    public void init(){
        int x = (int) Math.min(this.width*0.8, this.height*0.8);
        this.widget = new FastMenuWidget((this.width - x)/2, (this.height - x)/2, x);
        this.children.add(widget);
        this.buttons.add(new ButtonWidget(this.width - 120, this.height - 30, 96, 20, new TranslatableText("emotecraft.emotelist"), (button -> this.client.openScreen(new FullMenuScreen(new TranslatableText("emotecraft.emotelist"))))));
        this.children.addAll(this.buttons);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        widget.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }


    private class FastMenuWidget extends AbstractFastChooseWidget{

        public FastMenuWidget(int x, int y, int size) {
            super(x, y, size);
        }

        @Override
        protected boolean doHoverPart(FastChooseElement part) {
            return part.hasEmote();
        }

        @Override
        protected boolean isValidClickButton(int button) {
            return button == 0;
        }

        @Override
        protected boolean onClick(FastChooseElement element, int button) {
            if(element.getEmote() != null) {
                boolean bl = element.getEmote().playEmote((PlayerEntity) MinecraftClient.getInstance().getCameraEntity());
                client.openScreen(null);
                return bl;
            }
            return false;
        }
    }
}
