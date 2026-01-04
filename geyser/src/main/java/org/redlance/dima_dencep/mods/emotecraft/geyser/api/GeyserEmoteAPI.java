package org.redlance.dima_dencep.mods.emotecraft.geyser.api;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;
import org.redlance.dima_dencep.mods.emotecraft.geyser.animator.ControllerHolder;
import org.redlance.dima_dencep.mods.emotecraft.geyser.animator.GeyserAnimationController;

import java.util.UUID;

public class GeyserEmoteAPI extends ServerEmoteAPI {
    @Override
    protected void setPlayerPlayingEmoteImpl(UUID player, @Nullable Animation animation, float tick, boolean isForced) {
        GeyserAnimationController controller = ControllerHolder.INSTANCE.getByUUID(player);
        if (controller != null) controller.triggerAnimation(animation, tick);
    }

    @Override
    protected Pair<Animation, Float> getPlayedEmoteImpl(UUID player) {
        GeyserAnimationController controller = ControllerHolder.INSTANCE.getByUUID(player);
        if (controller == null || !controller.isActive()) return null;
        return Pair.of(controller.getCurrentAnimationInstance(), controller.getAnimationTicks());
    }

    @Override
    protected boolean isForcedEmoteImpl(UUID player) {
        return false; // TODO
    }

    @Override
    public int getPriority() {
        return super.getPriority() - 1;
    }
}
