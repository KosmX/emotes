package io.github.kosmx.emotes.arch.mixin;

import static dev.kosmx.playerAnim.core.data.gson.AnimationJson.asJson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
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
    private boolean emotecraft$deserializeBages(JsonElement instance, Operation<Boolean> original, @Local(ordinal = 0) KeyframeAnimation.AnimationBuilder emote, @Local(ordinal = 0) String string) {
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

    @WrapMethod(
            method = "lambda$serialize$0"
    )
    private static void emotecraft$serializeBages(JsonObject node, String s, Object o, Operation<Void> original) {
        if (o instanceof List<?> list) {
            JsonArray array = new JsonArray(list.size());
            for (Object element : list) {
                if (element instanceof String s1) {
                    try {
                        array.add(asJson(s1));
                    } catch (Throwable th) {
                        array.add(s1);
                    }
                } else if (element instanceof Number number) {
                    array.add(number);
                } else if (element instanceof Boolean b) {
                    array.add(b);
                } else if (element instanceof JsonElement e) {
                    array.add(e);
                }
            }
            original.call(node, s, array);
        } else {
            original.call(node, s, o);
        }
    }
}
