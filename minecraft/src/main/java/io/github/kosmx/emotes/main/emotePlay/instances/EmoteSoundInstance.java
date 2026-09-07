package io.github.kosmx.emotes.main.emotePlay.instances;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeMannequin;
import io.github.kosmx.emotes.common.opus.OpusSound;
import io.github.kosmx.emotes.main.emotePlay.PcmAudioStream;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Avatar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntSupplier;

/**
 * The emote's sound, streamed from decoded PCM instead of a resource pack file.
 */
public abstract class EmoteSoundInstance implements TickableSoundInstance {
    private static final FloatProvider DEFAULT_FLOAT = ConstantFloat.of(1.0F);
    private static final Sound SOUND = new Sound(
            McUtils.newIdentifier("emote_sound"), DEFAULT_FLOAT, DEFAULT_FLOAT, 1, Sound.Type.FILE, true, false, 16
    );

    private final Avatar avatar;
    private final OpusSound sound;
    private final IntSupplier offset;

    @Nullable
    private volatile OpusSound.DecodedSound decoded;
    private boolean stopped;
    private boolean started;

    protected EmoteSoundInstance(Avatar avatar, OpusSound sound, IntSupplier offset) {
        this.avatar = avatar;
        this.sound = sound;
        this.offset = offset;
    }

    /**
     * The engine waits on the decode itself, holding a channel meanwhile and letting it go if the emote
     * ends first, so the sound never has to be ready before the emote can start.
     */
    public CompletableFuture<AudioStream> stream() {
        this.started = true;
        return this.sound.decoded().thenApply(pcm -> {
            this.decoded = pcm;
            // Minutes of audio take a while to decode, so join the emote where it is by then
            return new PcmAudioStream(pcm.samples(), this.offset.getAsInt(), this.sound.loopStart());
        });
    }

    /**
     * @return whether the engine ever asked for the audio, as opposed to turning the sound down
     */
    public boolean started() {
        return this.started;
    }

    public void stop() {
        this.stopped = true;
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }

    /**
     * A preview should not outlive its screen, and audio should not give away a player that is not rendered.
     */
    public static boolean audible(Avatar avatar) {
        Minecraft mc = Minecraft.getInstance();
        if (avatar instanceof UnsafeMannequin) return mc.gui.screen() != null;

        return mc.player != null && !avatar.isRemoved() && !avatar.isInvisibleTo(mc.player);
    }

    @Override
    public void tick() {
        if (!audible(this.avatar)) this.stopped = true;
    }

    @Override
    public @NotNull Identifier getIdentifier() {
        return SOUND.getLocation();
    }

    @Override
    public @Nullable WeighedSoundEvents resolve(@NotNull SoundManager manager) {
        return new EmotecraftSoundEvents(SOUND);
    }

    @Override
    public @NotNull Sound getSound() {
        return SOUND;
    }

    @Override
    public @NotNull SoundSource getSource() {
        return SoundSource.PLAYERS;
    }

    @Override
    public boolean isLooping() {
        return false;
    }

    @Override
    public boolean isRelative() {
        return false;
    }

    @Override
    public int getDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        OpusSound.DecodedSound decoded = this.decoded;
        // Nothing is audible until the stream lands, and the engine asks for the volume again every tick
        if (decoded == null || !PlatformTools.getConfig().normalizeSoundVolume.get()) return 1.0F;
        return decoded.normalization();
    }

    @Override
    public float getPitch() {
        return 1.0F;
    }

    // Entity.getX/getY/getZ are final and read the field, missing the override a preview mannequin needs
    @Override
    public double getX() {
        return this.avatar.position().x();
    }

    @Override
    public double getY() {
        return this.avatar.position().y();
    }

    @Override
    public double getZ() {
        return this.avatar.position().z();
    }

    @Override
    public @NotNull Attenuation getAttenuation() {
        return Attenuation.LINEAR;
    }
}
