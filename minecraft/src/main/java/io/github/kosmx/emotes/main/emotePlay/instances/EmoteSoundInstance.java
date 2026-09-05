package io.github.kosmx.emotes.main.emotePlay.instances;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeMannequin;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.opus.OpusSound;
import io.github.kosmx.emotes.main.emotePlay.PcmAudioStream;
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

/**
 * The emote's sound, streamed from decoded PCM instead of a resource pack file.
 */
public class EmoteSoundInstance implements TickableSoundInstance {
    private static final FloatProvider DEFAULT_FLOAT = ConstantFloat.of(1.0F);
    private static final Sound SOUND = new Sound(
            Identifier.fromNamespaceAndPath(CommonData.MOD_ID, "emote_sound"),
            DEFAULT_FLOAT, DEFAULT_FLOAT, 1, Sound.Type.FILE, true, false, 16
    );

    private final Avatar avatar;
    private final OpusSound.DecodedSound decoded;
    private final int offset;
    private final int loopStart;

    private boolean stopped;

    public EmoteSoundInstance(Avatar avatar, OpusSound.DecodedSound decoded, int offset, int loopStart) {
        this.avatar = avatar;
        this.decoded = decoded;
        this.offset = offset;
        this.loopStart = loopStart;
    }

    public AudioStream stream() {
        return new PcmAudioStream(this.decoded.samples(), this.offset, this.loopStart);
    }

    public void stop() {
        this.stopped = true;
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }

    @Override
    public void tick() {
        if (this.avatar instanceof UnsafeMannequin) return;

        Minecraft mc = Minecraft.getInstance();
        // Audio would give away a player the client is deliberately not rendering
        if (mc.player == null || this.avatar.isRemoved() || this.avatar.isInvisibleTo(mc.player)) this.stopped = true;
    }

    @Override
    public @NotNull Identifier getIdentifier() {
        return SOUND.getLocation();
    }

    @Override
    public @Nullable WeighedSoundEvents resolve(SoundManager manager) {
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
        return PlatformTools.getConfig().normalizeSoundVolume.get() ? this.decoded.normalization() : 1.0F;
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
