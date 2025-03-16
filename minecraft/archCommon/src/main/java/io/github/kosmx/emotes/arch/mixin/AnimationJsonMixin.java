package io.github.kosmx.emotes.arch.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationJson;
import io.github.kosmx.emotes.api.services.LoggerService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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
            JsonArray array = instance.getAsJsonArray();
            List<String> bages = new ArrayList<>(array.size());
            for (JsonElement element : array) {
                try {
                    bages.add(element.toString());
                } catch (Throwable th) {
                    LoggerService.INSTANCE.log(Level.WARNING, "Failed to serialize bage!", th);
                }
            }
            emote.extraData.put("bages", bages);
        }
        return false;
    }
}
