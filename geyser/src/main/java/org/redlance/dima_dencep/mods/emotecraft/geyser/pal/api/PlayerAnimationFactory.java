package org.redlance.dima_dencep.mods.emotecraft.geyser.pal.api;

import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redlance.dima_dencep.mods.emotecraft.geyser.pal.animation.PlayerAnimManager;

import java.util.*;
import java.util.function.Function;

/**
 * Animation factory, the factory will be invoked whenever a client-player is constructed.
 * The returned animation will be automatically registered and added to playerAssociated data.
 * <p>
 * {@link PlayerAnimationAccess#REGISTER_ANIMATION_EVENT} is invoked <strong>after</strong> factories are done.
 */
public interface PlayerAnimationFactory {
    FactoryHolder ANIMATION_DATA_FACTORY = new FactoryHolder();

    @Nullable IAnimation invoke(@NotNull PlayerEntity player);

    class FactoryHolder {
        private FactoryHolder() {}

        private static final List<Function<PlayerEntity, DataHolder>> factories = new ArrayList<>();

        /**
         * Animation factory
         * @param id       animation id or <code>null</code> if you don't want to add to playerAssociated data
         * @param priority animation priority
         * @param factory  animation factory
         */
        public void registerFactory(@Nullable String id, int priority, @NotNull PlayerAnimationFactory factory) {
            factories.add(player -> Optional.ofNullable(factory.invoke(player)).map(animation -> new DataHolder(id, priority, animation)).orElse(null));
        }

        @ApiStatus.Internal
        private record DataHolder(@Nullable String id, int priority, @NotNull IAnimation animation) {}

        @ApiStatus.Internal
        public void prepareAnimations(PlayerEntity player, PlayerAnimManager playerStack) {
            for (Function<PlayerEntity, DataHolder> factory: factories) {
                DataHolder dataHolder = factory.apply(player);
                if (dataHolder != null) {
                    playerStack.addAnimLayer(dataHolder.priority(), dataHolder.animation());
                    if (dataHolder.id() != null) {
                        playerStack.putAnimation(dataHolder.id(), dataHolder.animation());
                    }
                }
            }
        }
    }

}
