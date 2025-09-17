package org.redlance.dima_dencep.mods.emotecraft.geyser.pal.animation;

import com.zigythebird.playeranimcore.animation.Animation;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cache class for holding loaded {@link Animation Animations}
 */
public class PlayerAnimResources {
	private static final Map<String, Animation> ANIMATIONS = new Object2ObjectOpenHashMap<>();

	/**
	 * Get an animation from the registry, using Identifier(mod_id, animation_name) as the key.
	 * @return animation, <code>null</code> if no animation
	 */
	public static @Nullable Animation getAnimation(String id) {
		if (!ANIMATIONS.containsKey(id)) return null;
		return ANIMATIONS.get(id);
	}

	/**
	 * Get Optional animation from registry
	 * @param identifier identifier
	 * @return Optional animation
	 */
	@NotNull
	public static Optional<Animation> getAnimationOptional(@NotNull String identifier) {
		return Optional.ofNullable(getAnimation(identifier));
	}

	/**
	 * @return an unmodifiable map of all the animations
	 */
	public static Map<String, Animation> getAnimations() {
		return Collections.unmodifiableMap(ANIMATIONS);
	}

	/**
	 * Returns the animations of a specific mod/namespace
	 * @param modid namespace (assets/modid)
	 * @return map of path and animations
	 */
	@NotNull
	public static Map<String, Animation> getModAnimations(@NotNull String modid) {
		HashMap<String, Animation> map = new HashMap<>();
		for (Map.Entry<String, Animation> entry: ANIMATIONS.entrySet()) {
			if (entry.getKey().split(":")[0].equals(modid)) {
				map.put(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}

	/**
	 * @param id ID of the desired animation.
	 * @return Returns true if that animation is available.
	 */
	public static boolean hasAnimation(String id) {
		return ANIMATIONS.containsKey(id);
	}
}
