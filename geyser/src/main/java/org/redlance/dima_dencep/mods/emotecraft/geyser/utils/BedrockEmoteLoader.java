package org.redlance.dima_dencep.mods.emotecraft.geyser.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonObject;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import com.zigythebird.playeranimcore.util.JsonUtil;
import io.github.kosmx.emotes.common.CommonData;
import org.jetbrains.annotations.NotNull;
import org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.resourcepack.EmoteResourcePack;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BedrockEmoteLoader extends CacheLoader<String, CompletableFuture<Animation>> {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            // .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private static final LoadingCache<String, CompletableFuture<Animation>> BEDROCK_KEYFRAMES = CacheBuilder.newBuilder()
            .maximumSize(128)
            .expireAfterAccess(5, TimeUnit.HOURS)
            .build(new BedrockEmoteLoader());

    @Override
    public @NotNull CompletableFuture<Animation> load(@NotNull String emoteId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.redlance.org/bugrock/v1/marketplace/get-emote-by-uuid"))
                .header("user-agent", EmotecraftExt.getInstance().description().toString())
                .POST(HttpRequest.BodyPublishers.ofString(emoteId))
                .build();

        CommonData.LOGGER.debug("Sending request: {}", request);
        return BedrockEmoteLoader.HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(this::parseAnimation)
                .exceptionally(throwable -> {
                    BedrockEmoteLoader.BEDROCK_KEYFRAMES.invalidate(emoteId);
                    CommonData.LOGGER.error("Failed to load emote!", throwable);
                    return null;
                });
    }

    private Animation parseAnimation(HttpResponse<InputStream> response) {
        try (Reader reader = new InputStreamReader(response.body())) {
            JsonObject obj = EmoteResourcePack.GSON.fromJson(reader, JsonObject.class);

            if (!obj.has("present") || !obj.get("present").getAsBoolean()) {
                throw new NullPointerException(JsonUtil.getAsString(obj, "message", "Animation is not present!"));

            } else if (obj.has("message")) {
                CommonData.LOGGER.warn(obj.get("message").getAsString());
            }

            for (Map.Entry<String, Animation> animation : UniversalAnimLoader.loadAnimations(obj.getAsJsonObject("emotes")).entrySet()) {
                return animation.getValue();
            }

            throw new NullPointerException();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void preloadEmotes(List<UUID> emotes) {
        for (UUID emoteId : emotes) {
            CommonData.LOGGER.debug("Preloading emote {}...", emoteId);
            try {
                BedrockEmoteLoader.BEDROCK_KEYFRAMES.get(emoteId.toString());
            } catch (Throwable th) {
                CommonData.LOGGER.error("Failed to preload emote: {}", emoteId, th);
            }
        }
    }

    public static CompletableFuture<Animation> loadEmote(String emoteId) {
        try {
            return BedrockEmoteLoader.BEDROCK_KEYFRAMES.get(emoteId);
        } catch (Throwable th) {
            return CompletableFuture.failedFuture(th);
        }
    }
}
