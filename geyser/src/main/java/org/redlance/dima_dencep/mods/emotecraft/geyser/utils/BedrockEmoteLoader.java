package org.redlance.dima_dencep.mods.emotecraft.geyser.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonObject;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketTask;
import io.github.kosmx.emotes.common.network.objects.NetData;
import net.raphimc.minecraftauth.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BedrockEmoteLoader {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private static final LoadingCache<String, CompletableFuture<KeyframeAnimation>> BEDROCK_KEYFRAMES = CacheBuilder.newBuilder()
            .maximumSize(128)
            .expireAfterAccess(5, TimeUnit.HOURS)
            .build(new CacheLoader<>() {
                @Override
                public CompletableFuture<KeyframeAnimation> load(@NotNull String emoteId) {
                    HttpRequest request = HttpRequest.newBuilder()
                            .build();

                    return BedrockEmoteLoader.HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                            .thenCompose(this::parseAnimation)
                            .exceptionally(throwable -> {
                                BedrockEmoteLoader.BEDROCK_KEYFRAMES.invalidate(emoteId);
                                LoggerService.INSTANCE.log(Level.WARNING, "Failed to load emote!", throwable);
                                return null;
                            });
                }

                private CompletableFuture<KeyframeAnimation> parseAnimation(HttpResponse<InputStream> response) {
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

                        if (data == null || data.purpose != PacketTask.STREAM) {
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
            LoggerService.INSTANCE.log(Level.FINE, "Preloading emote " + emoteId + "...");
            try {
                BedrockEmoteLoader.BEDROCK_KEYFRAMES.get(emoteId.toString());
            } catch (Throwable th) {
                LoggerService.INSTANCE.log(Level.WARNING, "Failed to preload emote: " + emoteId, th);
            }
        }
    }

    public static CompletableFuture<KeyframeAnimation> loadEmote(String emoteId) {
        return BedrockEmoteLoader.BEDROCK_KEYFRAMES.getUnchecked(emoteId);
    }
}
