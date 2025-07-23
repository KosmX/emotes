package org.redlance.dima_dencep.mods.emotecraft.geyser.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonObject;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import net.raphimc.minecraftauth.util.JsonUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BedrockEmoteLoader {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private static final LoadingCache<String, CompletableFuture<Animation>> BEDROCK_KEYFRAMES = CacheBuilder.newBuilder()
            .maximumSize(128)
            .expireAfterAccess(5, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull CompletableFuture<Animation> load(@NotNull String emoteId) {
                    HttpRequest request = HttpRequest.newBuilder()
                            .build();

                    return BedrockEmoteLoader.HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                            .thenCompose(this::parseAnimation)
                            .exceptionally(throwable -> {
                                BedrockEmoteLoader.BEDROCK_KEYFRAMES.invalidate(emoteId);
                                CommonData.LOGGER.error("Failed to load emote!", throwable);
                                return null;
                            });
                }

                private CompletableFuture<Animation> parseAnimation(HttpResponse<InputStream> response) {
                    try (Reader reader = new InputStreamReader(response.body())) {
                        JsonObject obj = JsonUtil.GSON.fromJson(reader, JsonObject.class);

                        if (!JsonUtil.getBooleanOr(obj, "present", false)) {
                            return CompletableFuture.failedFuture(new NullPointerException());
                        }

                        NetData data = new EmotePacket.Builder()
                                .setSizeLimit(Integer.MAX_VALUE, false)
                                .build()
                                .read(ByteBuffer.wrap(
                                        JsonUtil.GSON.fromJson(obj.get("bytes"), byte[].class)
                                ));

                        if (data.purpose != PacketTask.STREAM) {
                            return CompletableFuture.failedFuture(new IllegalStateException("Binary emote is invalid!"));
                        }

                        return CompletableFuture.completedFuture(data.emoteData);
                    } catch (IOException e) {
                        return CompletableFuture.failedFuture(e);
                    }
                }
            });

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
        return BedrockEmoteLoader.BEDROCK_KEYFRAMES.getUnchecked(emoteId);
    }
}
