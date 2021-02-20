package com.kosmx.emotes.mixin;

import com.kosmx.emotes.Client;
import com.kosmx.emotes.EmotecraftCallbacks;
import com.kosmx.emotes.Main;
import com.kosmx.emotes.main.config.EmoteHolder;
import com.kosmx.emotes.mixinInterface.EmotePlayerInterface;
import com.kosmx.emotes.network.ClientNetwork;
import com.kosmx.emotes.proxy.PerspectiveReduxProxy;
import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.common.opennbs.SoundPlayer;
import com.kosmx.emotes.common.opennbs.format.Layer;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.options.Perspective;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Environment(EnvType.CLIENT)
@Mixin(AbstractClientPlayerEntity.class)
public abstract class EmotePlayerMixin extends PlayerEntity implements EmotePlayerInterface {

    @Nullable
    private EmotePlayer emote;

    private int lastUpdated;

    public EmotePlayerMixin(World world, BlockPos pos, float yaw, GameProfile profile){
        super(world, pos, yaw, profile);
    }

    @Override
    public void playEmote(EmoteData emote){
        ActionResult result = EmotecraftCallbacks.startPlayEmote.invoker().playEmote(emote, this);
        if(result == ActionResult.FAIL){
            return;
        }

        this.emote = new EmotePlayer(emote, this::notePlayer);

        if(this == MinecraftClient.getInstance().getCameraEntity() && Main.config.enablePerspective && (MinecraftClient.getInstance().options.getPerspective().isFirstPerson() || Client.isPersonRedux())){
            this.emote.perspective = MinecraftClient.getInstance().options.getPerspective();
            if(Client.isPersonRedux()){
                if(!PerspectiveReduxProxy.getPerspective()){
                    PerspectiveReduxProxy.setPerspective(true);
                    this.emote.perspectiveRedux = true;
                }
            }
            else MinecraftClient.getInstance().options.setPerspective(Perspective.THIRD_PERSON_FRONT);
        }
    }

    /**
     * Just to not make lambdas in Mixins
     * @param note note to play
     */
    public void notePlayer(Layer.Note note){
        //this.world.playSoundFromEntity(); this can't use pitch at client-side...
        this.world.playSound(this.getX(), this.getY(), this.getZ(), SoundPlayer.getInstrumentFromCode(note.instrument).getSound(), SoundCategory.PLAYERS, note.getVolume(), note.getPitch(), true);
    }

    @Override
    public void stopEmote() {
        if(this.emote != null)this.emote.stop();
    }

    @Override
    @Nullable
    public EmotePlayer getEmote(){
        return this.emote;
    }

    @Override
    public void resetLastUpdated(){
        this.lastUpdated = 0;
    }

    @Override
    public boolean isPlayingEmote() {
        return EmotePlayer.isRunningEmote(this.getEmote());
    }

    @Override
    public void tick(){
        super.tick();
        if(EmotePlayer.isRunningEmote(this.emote)){
            this.bodyYaw = (this.bodyYaw * 3 + this.yaw) / 4; //to set the body to the correct direction smooth.
            //if not the clientPlayer playing this emote (not the camera or the camera is in someone else) OR I can play that emote
            if(this == MinecraftClient.getInstance().getCameraEntity() && this.emote.perspectiveRedux){
                if(!PerspectiveReduxProxy.getPerspective()){
                    //this.emote.perspectiveRedux = false;
                    //this.emote.perspective = null;
                }
            }
            else if(this == MinecraftClient.getInstance().getCameraEntity() && this.emote.perspective != null){
                if(MinecraftClient.getInstance().options.getPerspective() != Perspective.THIRD_PERSON_FRONT){
                    this.emote.perspective = null;
                }
            }
            if(this != MinecraftClient.getInstance().getCameraEntity() && MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity || EmoteHolder.canRunEmote(this)){
                this.emote.tick();
                this.lastUpdated++;
                if(this == MinecraftClient.getInstance().getCameraEntity() && MinecraftClient.getInstance().getCameraEntity() instanceof ClientPlayerEntity && lastUpdated >= 100){
                    if(emote.getStopTick() - emote.getCurrentTick() < 50 && ! emote.isInfinite()) return;
                    ClientNetwork.sendEmotePacket(emote.getData(), this, true);
                    lastUpdated = 0;
                }else if((this != MinecraftClient.getInstance().getCameraEntity() || MinecraftClient.getInstance().getCameraEntity() instanceof OtherClientPlayerEntity) && lastUpdated > 300){
                    this.emote.stop();
                }
            }else{
                emote.stop();
                ClientNetwork.clientSendStop();
            }
        }
    }
}
