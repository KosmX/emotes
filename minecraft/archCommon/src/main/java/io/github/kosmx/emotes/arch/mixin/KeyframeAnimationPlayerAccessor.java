package io.github.kosmx.emotes.arch.mixin;

import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = KeyframeAnimationPlayer.class, remap = false)
public interface KeyframeAnimationPlayerAccessor {
    @Accessor
    void setCurrentTick(int currentTick);
}
