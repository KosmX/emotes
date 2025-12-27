package org.redlance.dima_dencep.mods.emotecraft.geyser.animator;

import io.github.kosmx.emotes.common.CommonData;
import org.geysermc.geyser.entity.type.player.AvatarEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public final class ControllerHolder {
    private static final ThreadFactory THREAD_FACTORY = Thread.ofVirtual()
            .name("emotecraft-animating-")
            .uncaughtExceptionHandler((t, e) -> CommonData.LOGGER.warn("Failed to animate!", e))
            .factory();

    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(THREAD_FACTORY);
    private static final ExecutorService CHILD_EXECUTOR = Executors.newCachedThreadPool(THREAD_FACTORY);

    public static final ControllerHolder INSTANCE = new ControllerHolder();

    private final Map<UUID, GeyserAnimationController> controllers = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<Boolean>> inFlight = new ConcurrentHashMap<>();

    private ControllerHolder() {
        EXECUTOR.scheduleAtFixedRate(this::run, 0L, 50L, TimeUnit.MILLISECONDS);
    }

    private void run() {
        if (this.controllers.isEmpty()) return;
        for (Map.Entry<UUID, GeyserAnimationController> e : this.controllers.entrySet()) {
            UUID uuid = e.getKey();

            CompletableFuture<Boolean> current = this.inFlight.get(uuid);
            if (current != null && !current.isDone()) continue;

            CompletableFuture<Boolean> cf = CompletableFuture.supplyAsync(e.getValue()::handleFrame, CHILD_EXECUTOR);
            inFlight.put(uuid, cf);

            cf.whenComplete((remove, ex) -> {
                inFlight.remove(uuid, cf);
                if (remove) controllers.remove(uuid);
            });
        }
    }

    public GeyserAnimationController get(AvatarEntity entity) {
        GeyserAnimationController controller = this.controllers.computeIfAbsent(entity.getUuid(), GeyserAnimationController::new);
        controller.subscribe(entity);
        return controller;
    }
}
