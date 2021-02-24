package com.kosmx.emotes.main.screen.ingame;

import com.kosmx.emotes.common.tools.MathHelper;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.dataTypes.Text;
import com.kosmx.emotes.main.config.ClientConfig;
import com.kosmx.emotes.main.screen.IScreenLogic;
import com.kosmx.emotes.main.screen.widget.AbstractFastChooseWidget;

/**
 * Stuff to override
 * isPauseScreen -> false
 * render
 */
public abstract class FastMenuScreen<MATRIX> implements IScreenLogic<MATRIX> {
    private FastMenuWidget widget;
    private static final Text warn_no_emotecraft = EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.no_server");
    private static final Text warn_diff_emotecraft = EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.different_server");


    @Override
    public void initScreen(){
        int x = (int) Math.min(this.getWidth() * 0.8, this.getHeight() * 0.8);
        this.widget = newFastMenuWidget((this.getWidth() - x) / 2, (this.getHeight() - x) / 2, x);
        addToChildren(widget);
        //this.buttons.add(new ButtonWidget(this.width - 120, this.height - 30, 96, 20, new TranslatableText("emotecraft.config"), (button -> this.client.openScreen(new EmoteMenu(this)))));
        addToButtons(newButton(this.getWidth() - 120, this.getHeight() - 30, 96, 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.emotelist"), (button->openScreen(newFullScreenMenu()))));
        addButtonsToChildren();
    }

    protected abstract FullMenuScreen<MATRIX> newFullScreenMenu();


    @Override
    public void renderScreen(MATRIX matrices, int mouseX, int mouseY, float delta){
        this.renderBackground(matrices);
        widget.render(matrices, mouseX, mouseY, delta);
        int remoteVer = ((ClientConfig)EmoteInstance.config).modAvailableAtServer ? ((ClientConfig)EmoteInstance.config).correctServerVersion ? 2 : 1 : 0;
        if(remoteVer != 2){
            drawCenteredText(matrices, remoteVer == 0 ? warn_no_emotecraft : warn_diff_emotecraft, this.getWidth()/2, this.getHeight()/24 - 1, MathHelper.colorHelper(255, 255, 255, 255));
        }
    }

    @Override
    public boolean isThisPauseScreen() {
        return false;
    }

    abstract protected FastMenuWidget newFastMenuWidget(int width, int height, int size);

    private abstract class FastMenuWidget extends AbstractFastChooseWidget<MATRIX> {

        public FastMenuWidget(int x, int y, int size){
            super(x, y, size);
        }

        @Override
        protected boolean doHoverPart(FastChooseElement part){
            return part.hasEmote();
        }

        @Override
        protected boolean isValidClickButton(int button){
            return button == 0;
        }

        @Override
        protected boolean onClick(FastChooseElement element, int button){
            if(element.getEmote() != null){
                boolean bl = element.getEmote().playEmote(EmoteInstance.instance.getClientMethods().getMainPlayer());
                openScreen(null);
                return bl;
            }
            return false;
        }
    }
}
