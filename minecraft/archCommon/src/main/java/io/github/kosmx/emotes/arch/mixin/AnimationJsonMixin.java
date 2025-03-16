package io.github.kosmx.emotes.arch.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationJson;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AnimationJson.class, remap = false)
public class AnimationJsonMixin {
    @WrapOperation(
            method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Ljava/util/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/gson/JsonElement;isJsonPrimitive()Z"
            )
    )
    private boolean emotecraft$serializeBages(JsonElement instance, Operation<Boolean> original, @Local(ordinal = 0) KeyframeAnimation.AnimationBuilder emote, @Local(ordinal = 0) String string) {
        if (original.call(instance)) return true;
        if ("bages".equals(string) && instance.isJsonArray()) {
            emote.extraData.put("bages", instance.getAsJsonArray());
        }
        return false;
    }
}
