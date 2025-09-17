package org.redlance.dima_dencep.mods.emotecraft.geyser.pal.animation;

import com.zigythebird.playeranimcore.animation.layered.AnimationStack;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The animation data collection for a given player instance
 * <p>
 * Generally speaking, a single working-instance of a player will have a single instance of {@code PlayerAnimManager} associated with it
 */
public class PlayerAnimManager extends AnimationStack {
	private final Map<String, IAnimation> animations = new HashMap<>();
	private final PlayerEntity player;

	private boolean isFirstTick = true;
	private float tickDelta;

	public PlayerAnimManager(PlayerEntity player) {
		this.player = player;
	}

	public boolean isFirstTick() {
		return this.isFirstTick;
	}

	protected void finishFirstTick() {
		this.isFirstTick = false;
	}

	public float getTickDelta() {
		return this.tickDelta;
	}

	/**
	 * If you touch this, you're a horrible person.
	 */
	@ApiStatus.Internal
	public void setTickDelta(float tickDelta) {
		this.tickDelta = tickDelta;
	}

	public PlayerEntity getPlayer() {
		return player;
	}

	public @Nullable IAnimation getAnimation(String name) {
		return animations.getOrDefault(name, null);
	}

	public void putAnimation(String name, IAnimation animation) {
		animations.put(name, animation);
	}
}
