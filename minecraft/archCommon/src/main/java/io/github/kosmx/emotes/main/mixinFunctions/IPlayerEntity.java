package io.github.kosmx.emotes.main.mixinFunctions;

import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IPlayerEntity {
    default void initEmotePerspective() {
        if (isMainAvatar() && PlatformTools.getConfig().enablePerspective.get() && PlatformTools.getPerspective() == CameraType.FIRST_PERSON) {
            emotecraft$getEmote().perspective = true;
            PlatformTools.setPerspective(PlatformTools.getConfig().getCameraType());
        }
    }

    default void emotecraft$playEmote(@Nullable Animation emote, float tick, boolean isForced) {
        emotecraft$playEmote(emote, Animation.LoopType.DEFAULT, tick, isForced);
    }

    @ApiStatus.Internal
    default void emotecraft$playEmote(@Nullable Animation emote, Animation.LoopType loopType, float tick, boolean isForced) {
        throw new NotImplementedException();
    }

    default @NotNull EmotePlayer emotecraft$getEmote() {
        throw new NotImplementedException();
    }

    default boolean isPlayingEmote() {
        return EmotePlayer.isRunningEmote(this.emotecraft$getEmote());
    }

    default boolean isMainAvatar() {
        return ClientUtil.getClientPlayer() == this;
    }

    /**
     * Use this ONLY for the main player
     */
    default void stopEmote() {
        emotecraft$getEmote().stop();
    }

    default void stopEmote(UUID emoteID) {
        Animation animation = emotecraft$getEmote().getCurrentAnimationInstance();
        if (animation != null &&animation.uuid().equals(emoteID)) {
            stopEmote();
        }
    }

    default boolean emotecraft$isForcedEmote() {
        throw new NotImplementedException();
    }

    default void emotecraft$playerEntersInvalidPose() {
        if (!isPlayingEmote() || emotecraft$isForcedEmote()) {
            return;
        }

        if (PlatformTools.getConfig().checkPose.get()) {
            ClientEmotePlay.clientStopLocalEmote(emotecraft$getEmote().getCurrentAnimationInstance());
        }
    }

    default void emotecraft$playRawSound(SoundInstance instance) {
        Minecraft.getInstance().getSoundManager().play(instance);
    }
}
