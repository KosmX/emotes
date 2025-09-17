package org.redlance.dima_dencep.mods.emotecraft.geyser.pal.animation;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.enums.PlayState;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import it.unimi.dsi.fastutil.Pair;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.geysermc.geyser.session.GeyserSession;
import org.redlance.dima_dencep.mods.emotecraft.geyser.pal.api.PlayerAnimationAccess;

import java.util.UUID;

public class GeyserEmotePlayer extends PlayerAnimationController {
    private int age = 0;
    private boolean isForced = false;
    public boolean perspective = false;
    
    public GeyserEmotePlayer(PlayerEntity player) {
        super(player, (controller, state, animationSetter) -> PlayState.STOP);
    }
    
    public void playEmote(Animation emote, float tick, boolean isForced) {
        this.stop();
        this.triggerAnimation(emote, tick);
        this.initEmotePerspective();
        if (this.isMainPlayer()) this.isForced = isForced;
    }

    public GeyserSession getSession() {
        return this.player.getSession();
    }

    public boolean isMainPlayer() {
        return this.getSession().getPlayerEntity() == this.player;
    }

    public void stopEmote(UUID emoteID) {
        Animation animation = this.getCurrentAnimationInstance();
        if (animation != null &&animation.uuid().equals(emoteID)) {
            stop();
        }
    }

    public void playerEntersInvalidPose() {
        if (!this.isActive() || isForcedEmote()) {
            return;
        }

//        if (PlatformTools.getConfig().checkPose.get()) {
//            ClientEmotePlay.clientStopLocalEmote(this.getCurrentAnimationInstance());
//        }
    }

//    public void playRawSound(SoundInstance instance) {
//        Minecraft.getInstance().getSoundManager().play(instance);
//    }

    public void initEmotePerspective() {
//        if (isMainPlayer() && PlatformTools.getConfig().enablePerspective.get() && PlatformTools.getPerspective() == CameraType.FIRST_PERSON) {
//            getEmote().perspective = true;
//            PlatformTools.setPerspective(PlatformTools.getConfig().getCameraType());
//        }
    }

    @Override
    public void tick(AnimationData data) {
        if (this.age <= 1) { //Emote init with a little delay (40-60 ms)
            if(this.age++ == 1) {
//                Pair<Animation, Float> p = ClientEmotePlay.getEmoteForUUID(player.getUuid());
//                if(p != null){
//                    ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(p.left(), p.right(), player.getUuid());
//                    this.playEmote(p.left(), p.right(), false);
//                }
                PlayerEntity clientPlayer = getSession().getPlayerEntity();
                if (!this.isMainPlayer() && clientPlayer != null && PlayerAnimationAccess.getPlayerAnimManager(clientPlayer).isActive()) {
//                    ClientEmotePlay.clientRepeatLocalEmote(this.getCurrentAnimationInstance(), this.getAnimationTicks(), player.getUuid());
                }
            }
        }

        if (this.isActive() && isMainPlayer()) {
//            if (this.perspective && PlatformTools.getPerspective() != PlatformTools.getConfig().getCameraType()) {
//                this.perspective = false;
//            }

//            if (!EmoteHolder.canRunEmote((AbstractClientPlayer) (Object) this)) {
//                ClientEmotePlay.clientStopLocalEmote(getEmote().getData());
//            }
        }
        super.tick(data);
    }

    public boolean isForcedEmote() {
        return this.isForcedEmote() && this.isForced;
    }
}
