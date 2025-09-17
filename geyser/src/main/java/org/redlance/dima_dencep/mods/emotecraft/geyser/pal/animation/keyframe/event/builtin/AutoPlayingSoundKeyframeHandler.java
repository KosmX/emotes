package org.redlance.dima_dencep.mods.emotecraft.geyser.pal.animation.keyframe.event.builtin;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.event.CustomKeyFrameEvents;
import com.zigythebird.playeranimcore.animation.keyframe.event.data.SoundKeyframeData;
import com.zigythebird.playeranimcore.event.EventResult;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.SoundEvent;
import org.redlance.dima_dencep.mods.emotecraft.geyser.pal.animation.PlayerAnimationController;

/**
 * Built-in helper for a {@link CustomKeyFrameEvents.CustomKeyFrameHandler CustomKeyFrameHandler} that automatically plays the sound defined in the keyframe data
 * <p>
 * The expected keyframe data format is one of the below:
 * <pre>{@code
 * namespace:soundid
 * namespace:soundid|volume|pitch
 * }</pre>
 */
public class AutoPlayingSoundKeyframeHandler implements CustomKeyFrameEvents.CustomKeyFrameHandler<SoundKeyframeData> {
    @Override
    public EventResult handle(float animationTick, AnimationController controller, SoundKeyframeData keyFrameData, AnimationData animationData) {
        if (controller instanceof PlayerAnimationController playerController) {
            Vector3f position = playerController.getPlayer().position();
            if (position == null) return EventResult.PASS;

            String[] segments = keyFrameData.getSound().split("\\|");
            String parsed = segments[0];
            if (parsed == null) return EventResult.PASS;
            segments = parsed.split(":");
            parsed = segments[segments.length-1].toUpperCase();

            try {
                playerController.getPlayer().getSession().playSoundEvent(SoundEvent.valueOf(parsed), position);
                return EventResult.SUCCESS;
            } catch (Exception ignore) {}
        }
        return EventResult.PASS;
    }
}
