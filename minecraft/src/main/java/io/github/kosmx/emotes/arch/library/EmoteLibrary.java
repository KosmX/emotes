package io.github.kosmx.emotes.arch.library;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.EmotecraftModPlatform;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import org.jspecify.annotations.NonNull;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryClient;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryException;
import org.redlance.emotecraftlibrary.sdk.JoinServer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

final class EmoteLibrary implements JoinServer {
    private static final Executor EXECUTOR = Executors.newCachedThreadPool(Thread.ofVirtual()
            .name("emotecraft-library-", 0)
            .factory()
    );

    public static final LoadingCache<UUID, CompletableFuture<Animation>> ANIMATION_CACHE = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .build(new CacheLoader<>() {
                @Override
                public @NonNull CompletableFuture<Animation> load(@NonNull UUID key) {
                    return EmoteLibrary.executeAuthorized(client -> client.getBinary(key, EmotePacket.defaultVersions))
                            .thenApply(is ->
                                    UniversalEmoteSerializer.readData(is, "emote.emotecraft").values().iterator().next()
                            )
                            .whenComplete((_, throwable) -> {
                                // Don't let a transient failure stay cached forever — evict so the next request retries.
                                if (throwable != null) ANIMATION_CACHE.invalidate(key);
                            });
                }
            });

    private static final EmoteLibraryClient EMOTE_LIBRARY_CLIENT = new EmoteLibraryClient("https://emotes.redlance.org/", String.format("%s/%s (%s; mc%s)",
            CommonData.MOD_NAME, EmotecraftModPlatform.INSTANCE.getModVersion(CommonData.MOD_ID), EmotecraftModPlatform.INSTANCE.getPlatformName(), SharedConstants.getCurrentVersion().name()
    ));

    private static final EmoteLibrary JOIN_SERVER = new EmoteLibrary();

    public static <R> CompletableFuture<R> executeAuthorized(Function<EmoteLibraryClient, R> request) {
        return CompletableFuture.supplyAsync(() -> request.apply(EMOTE_LIBRARY_CLIENT), EXECUTOR)
                .exceptionallyCompose(throwable -> {
                    throwable = unwrap(throwable);
                    if (throwable instanceof EmoteLibraryException.SessionExpired || throwable instanceof EmoteLibraryException.NotAuthorized || throwable instanceof EmoteLibraryException.AuthFailed) {
                        return CompletableFuture.runAsync(() -> {
                            if (PlatformTools.getConfig().cloudLibraryStatus.get() != LibraryStatus.ENABLED) {
                                throw new EmoteLibraryException("EmoteLibrary not enabled!");
                            }
                            EMOTE_LIBRARY_CLIENT.authorizeJava(JOIN_SERVER);
                        }, EXECUTOR).thenApply((_) -> request.apply(EMOTE_LIBRARY_CLIENT)).whenComplete((_, th) -> {
                            if (th != null) CommonData.LOGGER.warn("Failed to send emotecraft library request!", th);
                        });
                    }
                    CommonData.LOGGER.warn("Failed to send emotecraft library request!", throwable);
                    throw new CompletionException(throwable);
                });
    }

    /** Peels the {@link CompletionException} wrappers off a future's failure to reach the real cause (e.g. an SDK error). */
    static Throwable unwrap(Throwable throwable) {
        while (throwable instanceof CompletionException && throwable.getCause() != null) {
            throwable = throwable.getCause();
        }
        return throwable;
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(EMOTE_LIBRARY_CLIENT::close, "emote-library-shoutdown-hook"));
    }

    @Override
    public String join(String serverId) {
        try {
            Minecraft mc = Minecraft.getInstance();
            User user = mc.getUser();
            mc.services().sessionService().joinServer(user.getProfileId(), user.getAccessToken(), serverId);
            return user.getName();
        } catch (AuthenticationException e) {
            throw new EmoteLibraryException.AuthFailed(EmoteLibraryException.reason(e), e);
        }
    }
}
