package io.github.kosmx.emotes.arch.mixin;

import com.zigythebird.playeranim.accessors.IAnimatedAvatar;
import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin it into the player is way easier than storing it somewhere else...
 */
@Mixin(Avatar.class)
public abstract class EmoteAvatarMixin extends LivingEntity implements IPlayerEntity {

    @Unique
    private int emotecraft$age = 0;

    @Unique
    private final EmotePlayer emotecraft$container = new EmotePlayer((Avatar) (Object) this);

    @Unique
    private boolean emotecraft$isForced = false;

    protected EmoteAvatarMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(EntityType<? extends LivingEntity> entityType, Level level, CallbackInfo ci) {
        ((IAnimatedAvatar) this).playerAnimLib$getAnimManager().addAnimLayer(1000, emotecraft$container);
    }

    @Override
    public void emotecraft$playEmote(@Nullable Animation emote, Animation.LoopType loopType, float tick, boolean isForced) {
        stopEmote();
        if (emote != null) {
            this.emotecraft$container.triggerAnimation(RawAnimation.begin().then(emote, loopType), tick);
            this.initEmotePerspective();
            if (this.isMainAvatar()) this.emotecraft$isForced = isForced;
        }
    }

    @Override
    public @NotNull EmotePlayer emotecraft$getEmote() {
        return this.emotecraft$container;
    }

    @SuppressWarnings({"MixinAnnotationTarget", "UnresolvedMixinReference"})
    @Inject(method = {"tick", "method_5773"}, at = @At(value = "TAIL"), remap = false)
    public void tick(CallbackInfo ci) {
        if (this.emotecraft$age <= 1) { // Emote init with a little delay (40-60 ms)
            if(this.emotecraft$age++ == 1) {
                Pair<Animation, Float> p = ClientEmotePlay.getEmoteForUUID(getUUID());
                if (p != null) {
                    ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(p.left(), p.right(), getUUID());
                    this.emotecraft$playEmote(p.left(), p.right(), false);
                }
                if (!this.isMainAvatar() && ClientUtil.getClientPlayer() != null && ClientUtil.getClientPlayer().isPlayingEmote()) {
                    IPlayerEntity playerEntity = ClientUtil.getClientPlayer();
                    ClientEmotePlay.clientRepeatLocalEmote(playerEntity.emotecraft$getEmote().getCurrentAnimationInstance(), playerEntity.emotecraft$getEmote().getAnimationTicks(), this.getUUID());
                }
            }
        }

        if (isPlayingEmote() && isMainAvatar()) {
            if (emotecraft$getEmote().perspective && PlatformTools.getPerspective() != PlatformTools.getConfig().getCameraType()) {
                emotecraft$getEmote().perspective = false;
            }

            if (((Object) this) instanceof AbstractClientPlayer player && !EmoteHolder.canRunEmote(player)) {
                ClientEmotePlay.clientStopLocalEmote(player.emotecraft$getEmote().getCurrentAnimationInstance());
            }
        }
    }

    @Override
    public boolean emotecraft$isForcedEmote() {
        return this.isPlayingEmote() && this.emotecraft$isForced;
    }
}
