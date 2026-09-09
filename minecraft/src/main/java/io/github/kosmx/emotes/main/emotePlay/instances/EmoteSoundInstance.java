package io.github.kosmx.emotes.main.emotePlay.instances;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeMannequin;
import io.github.kosmx.emotes.common.opus.OpusPackets;
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
import net.minecraft.util.Util;
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
    private final IntSupplier position;

    // Taken on the client thread, the only one that may ask the animation
    private volatile int offset;
    // What the decode measured, never zero, so zero is "not decoded yet"
    private volatile float normalization;
    // How much audio is left, in ms of playing time, which the animation clock drifts from
    private volatile long remaining = Long.MAX_VALUE;
    private long ticked; // client thread only, like the ticks it counts
    private volatile boolean stopped;

    protected EmoteSoundInstance(Avatar avatar, OpusSound sound, IntSupplier position) {
        this.avatar = avatar;
        this.sound = sound;
        this.position = position;
        this.offset = position.getAsInt();
        this.ticked = Util.getMillis();
        this.normalization = sound.normalization(); // a replay knows its level before the engine asks
    }

    /**
     * The engine waits on the decode itself, holding a channel meanwhile and letting it go if the emote
     * ends first, so the sound never has to be ready before the emote can start.
     */
    public CompletableFuture<AudioStream> stream() {
        return this.sound.decoded(this::isStopped).handle((pcm, error) -> {
            if (error != null || this.stopped) {
                stop();
                return silence();
            }

            // Minutes of audio take a while to decode, so join the emote where it is by then
            int offset = this.offset;
            PcmAudioStream stream = new PcmAudioStream(pcm.samples(), offset, this.sound.loopStart());
            if (stream.exhausted()) return silence(); // the emote outran the track while it decoded

            this.normalization = pcm.normalization();
            if (this.sound.loopStart() == OpusSound.NO_LOOP) {
                this.remaining = (this.sound.playable() - offset) / (OpusPackets.SAMPLE_RATE / 1000);
            }
            return stream;
        });
    }

    /**
     * Counted at the rate the engine plays, not the emote's: a channel thrown away by a reload leaves the
     * rest of the track to be picked up again, while one that ran out stays quiet.
     *
     * @return whether the track has been played out
     */
    public boolean finished() {
        return this.remaining <= 0;
    }

    /** A source that never got a buffer never stops, and the engine frees only channels it saw stop. */
    private static AudioStream silence() {
        return new PcmAudioStream(new short[1], 0, OpusSound.NO_LOOP);
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
        // The engine refuses a silenced source, and would refuse it all emote long
        if (mc.options.getFinalSoundSourceVolume(SoundSource.PLAYERS) <= 0.0F) return false;
        if (avatar instanceof UnsafeMannequin) return mc.gui.screen() != null;

        return mc.player != null && !avatar.isRemoved() && !avatar.isInvisibleTo(mc.player);
    }

    @Override
    public void tick() {
        // The engine pauses the channel along with the game, and stops ticking us just the same
        long now = Util.getMillis();
        if (this.remaining != Long.MAX_VALUE) this.remaining -= now - this.ticked;
        this.ticked = now;

        if (audible(this.avatar)) this.offset = this.position.getAsInt();
        else this.stopped = true;
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
        float normalization = this.normalization;
        // The engine takes the volume before the stream, so a decoding track must not start loud
        if (normalization == 0.0F) return 0.0F;
        return PlatformTools.getConfig().normalizeSoundVolume.get() ? normalization : 1.0F;
    }

    /** The channel is held through the decode, silent as it is. */
    @Override
    public boolean canStartSilent() {
        return true;
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
