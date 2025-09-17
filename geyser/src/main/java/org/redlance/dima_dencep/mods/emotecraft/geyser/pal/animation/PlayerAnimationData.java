package org.redlance.dima_dencep.mods.emotecraft.geyser.pal.animation;

import com.zigythebird.geyser_pal.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.AnimationData;
import org.geysermc.geyser.entity.type.player.PlayerEntity;

public class PlayerAnimationData extends AnimationData {
    private final PlayerEntity player;

    public PlayerAnimationData(PlayerEntity player, float velocity, float partialTick) {
        super(velocity, partialTick);
        this.player = player;
    }

    /**
     * Gets the current player being animated
     */
    public PlayerEntity getPlayer() {
        return this.player;
    }

    /**
     * Gets the current player animation manager
     */
    public PlayerAnimManager getPlayerAnimManager() {
        return PlayerAnimationAccess.getPlayerAnimManager(player);
    }

    @Override
    public PlayerAnimationData copy() {
        return new PlayerAnimationData(getPlayer(), getVelocity(), getPartialTick());
    }
}
