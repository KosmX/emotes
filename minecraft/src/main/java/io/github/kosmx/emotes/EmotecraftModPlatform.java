package io.github.kosmx.emotes;

import io.github.kosmx.emotes.common.opus.OpusSound;
import io.github.kosmx.emotes.main.emotePlay.instances.EmoteSoundInstance;
import net.minecraft.world.entity.Avatar;
import org.redlance.common.services.AdvancedService;
import org.redlance.common.services.ServiceUtils;

import java.util.function.IntSupplier;

public interface EmotecraftModPlatform extends AdvancedService {
    EmotecraftModPlatform INSTANCE = ServiceUtils.loadService(EmotecraftModPlatform.class);

    String getModVersion(String modid);
    String getPlatformName();

    /**
     * Both loaders let a sound instance hand the engine a stream of its own, each through its own method.
     *
     * @param offset where the emote is, in samples, asked once the PCM is ready
     */
    EmoteSoundInstance createSound(Avatar avatar, OpusSound sound, IntSupplier offset);
}
