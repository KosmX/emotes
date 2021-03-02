package com.kosmx.emotes.fabric.mixin;

import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.fabric.executor.types.Key;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.main.config.ClientConfig;
import com.kosmx.emotes.main.network.ClientEmotePlay;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public class KeyEventMixin {
    @Inject(method = "onKeyPressed", at = @At(value = "HEAD"))
    private static void keyPressCallback(InputUtil.Key key, CallbackInfo ci){
        if(EmoteHolder.canRunEmote(EmoteInstance.instance.getClientMethods().getMainPlayer())){
            for(EmoteHolder emoteHolder: ((ClientConfig)EmoteInstance.config).emotesWithKey){
                if(emoteHolder.keyBinding.equals(new Key(key))){
                    ClientEmotePlay.clientStartLocalEmote(emoteHolder);
                    return;
                }
            }
        }
    }
}
