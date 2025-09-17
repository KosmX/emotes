package org.redlance.dima_dencep.mods.emotecraft.geyser.pal.api;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.event.Event;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redlance.dima_dencep.mods.emotecraft.geyser.pal.animation.PlayerAnimManager;
import org.redlance.dima_dencep.mods.emotecraft.geyser.pal.animation.PlayerAnimationProcessor;

import java.util.Map;
import java.util.WeakHashMap;

public final class PlayerAnimationAccess {
    private static final Map<PlayerEntity, PlayerAnimationProcessor> PLAYER_PROCESSORS = new WeakHashMap<>();
    private static final Map<PlayerEntity, PlayerAnimManager> PLAYER_MANAGERS = new WeakHashMap<>();

    /**
     * Get the animation manager for a geyser player.
     *
     * @param player The geyser player instance
     * @return The animation manager associated with the player
     */
    public static PlayerAnimManager getPlayerAnimManager(PlayerEntity player) throws IllegalArgumentException {
        return PLAYER_MANAGERS.computeIfAbsent(player, playerEntity -> {
            PlayerAnimManager manager = new PlayerAnimManager(playerEntity);
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.prepareAnimations(player, manager);
            PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.invoker().registerAnimation(player, manager);
            return manager;
        });
    }

    /**
     * Get the animation processor for a geyser player.
     *
     * @param player The geyser player instance
     * @return The animation processor associated with the player
     */
    public static PlayerAnimationProcessor getPlayerAnimProcessor(PlayerEntity player) throws IllegalArgumentException {
        return PLAYER_PROCESSORS.computeIfAbsent(player, PlayerAnimationProcessor::new);
    }


    /**
     * Get the player animator (usually a {@link AnimationController}) associated with an id.
     * @throws IllegalArgumentException if the given argument is not a player, or api mixins have failed (normally never)
     * @implNote data is stored in the player object (using mixins), using it is more efficient than any objectMap as objectMap solution does not know when to delete the data.
     */
    public static @Nullable IAnimation getPlayerAnimationLayer(@NotNull PlayerEntity player, @NotNull String id) {
        return getPlayerAnimManager(player).getAnimation(id);
    }

    /**
     * If you don't want to create your own mixin, you can use this event to add animation to players<br>
     * <b>The event will fire for every player</b> and if the player reloads, it will fire again.<br>
     * <hr>
     * NOTE: You have to use the given stack. Every other method of getting the anim manager will return null.
     */
    public static final Event<AnimationRegister> REGISTER_ANIMATION_EVENT = new Event<>(listeners -> (player, animationStack) -> {
        for (AnimationRegister listener : listeners) {
            listener.registerAnimation(player, animationStack);
        }
    });

    @FunctionalInterface
    public interface AnimationRegister {
        /**
         * Player object is in construction, it will be invoked when you can register animation
         * It will be invoked for every player only ONCE
         */
        void registerAnimation(@NotNull PlayerEntity player, @NotNull PlayerAnimManager manager);
    }
}
