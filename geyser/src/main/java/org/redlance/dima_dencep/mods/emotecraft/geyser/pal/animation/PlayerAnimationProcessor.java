package org.redlance.dima_dencep.mods.emotecraft.geyser.pal.animation;

import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.animation.layered.AnimationStack;
import org.cloudburstmc.math.vector.Vector3f;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.redlance.dima_dencep.mods.emotecraft.geyser.pal.api.PlayerAnimationAccess;

public class PlayerAnimationProcessor extends AnimationProcessor {
    private final PlayerEntity player;

    /**
     * Each AnimationProcessor must be bound to a player
     *
     * @param player The player to whom this processor is bound
     */
    public PlayerAnimationProcessor(PlayerEntity player) {
        super();
        this.player = player;
    }

    @Override
    public void tickAnimation(AnimationStack stack, AnimationData state) {
        super.tickAnimation(stack, state);

        if (stack instanceof PlayerAnimManager playerAnimManager) {
            playerAnimManager.finishFirstTick();
        }
    }

    @Override
    public void handleAnimations(float partialTick, boolean fullTick) {
        Vector3f velocity = player.getMotion();

        PlayerAnimManager animatableManager = PlayerAnimationAccess.getPlayerAnimManager(player);

        AnimationData animationData = new PlayerAnimationData(player, (Math.abs(velocity.getX()) + Math.abs(velocity.getZ())) / 2f, partialTick);

        if (fullTick) animatableManager.tick(animationData.copy());

        this.tickAnimation(animatableManager, animationData);

        //TODO get all the bone values here and send them via the update properties method in the EntityData class you get from GeyserSession
    }

    public PlayerEntity getPlayer() {
        return this.player;
    }
}
