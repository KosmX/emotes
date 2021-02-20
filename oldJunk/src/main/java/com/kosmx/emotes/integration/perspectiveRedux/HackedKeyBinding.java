package com.kosmx.emotes.integration.perspectiveRedux;

import com.kosmx.emotes.mixinInterface.EmotePlayerInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * I need to redirect the toggleKey.isPressed() in the lambda, but that is not really possible
 * so... I redirect the Constructor of the KeyBinding to another Class (this)
 * then here I Override the isPressed method, and I'm done :D
 */
public class HackedKeyBinding extends KeyBinding {
    public HackedKeyBinding(String translationKey, int code, String category) {
        super(translationKey, code, category);
    }

    public HackedKeyBinding(String translationKey, InputUtil.Type type, int code, String category) {
        super(translationKey, type, code, category);
    }

    /**
     * There is it. If I can't redirect from mixin, I'll do it somehow else ;)
     * @return some interesting value...
     */
    @Override
    public boolean isPressed() {
        boolean s = super.isPressed();
        if(MinecraftClient.getInstance().getCameraEntity() != null && ((EmotePlayerInterface)MinecraftClient.getInstance().getCameraEntity()).isPlayingEmote()){
            EmotePlayerInterface player = ((EmotePlayerInterface)MinecraftClient.getInstance().getCameraEntity());
            if(player.getEmote().perspectiveRedux){
                if(s)player.getEmote().perspectiveRedux = false;
                else return true;
            }
        }
        return s;
    }
}
