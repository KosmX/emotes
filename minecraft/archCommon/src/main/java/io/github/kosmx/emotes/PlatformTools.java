package io.github.kosmx.emotes;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class PlatformTools {

    @ExpectPlatform
    public static boolean isPlayerAnimLoaded() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static INetworkInstance getClientNetworkController() {
        throw new AssertionError();
    }


    public static @Nullable IEmotePlayerEntity getPlayerFromUUID(UUID uuid) {
        if (Minecraft.getInstance().level == null) return null;
        return (IEmotePlayerEntity) Minecraft.getInstance().level.getPlayerByUUID(uuid);
    }

    public static void openExternalEmotesDir() {
        Util.getPlatform().openFile(EmoteInstance.instance.getExternalEmoteDir());
    }

    public static Component fromJson(String json){
        if(json == null){
            return Component.literal("");
        }
        try {
            return Component.Serializer.fromJson(JsonParser.parseString(json));
        }catch (JsonParseException e){
            return Component.literal(json);
        }
    }

    public static Component fromJson(Object obj) {
        if (obj == null || obj instanceof String) {
            return fromJson((String) obj);
        } else if (obj instanceof JsonElement) {
            return Component.Serializer.fromJson((JsonElement) obj);
        } else throw new IllegalArgumentException("Can not create Text from " + obj.getClass().getName());
    }

    public static ResourceLocation newIdentifier(String id){
        return new ResourceLocation(CommonData.MOD_ID, id);
    }
}
