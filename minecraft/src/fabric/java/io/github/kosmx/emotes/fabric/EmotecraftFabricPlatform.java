package io.github.kosmx.emotes.fabric;

import io.github.kosmx.emotes.EmotecraftModPlatform;
import io.github.kosmx.emotes.common.opus.OpusSound;
import io.github.kosmx.emotes.main.emotePlay.instances.EmoteSoundInstance;
import net.fabricmc.fabric.api.client.sound.v1.FabricSoundInstance;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntSupplier;

public final class EmotecraftFabricPlatform implements EmotecraftModPlatform {
    @Override
    public String getModVersion(String modid) {
        return FabricLoader.getInstance().getModContainer(modid)
                .map(ModContainer::getMetadata)
                .map(ModMetadata::getVersion)
                .map(Version::getFriendlyString)
                .orElse(modid.toUpperCase() + "-UNKNOWN-FABRIC");
    }

    @Override
    public String getPlatformName() {
        return "fabric";
    }

    @Override
    public EmoteSoundInstance createSound(Avatar avatar, OpusSound sound, IntSupplier offset) {
        return new EmoteSound(avatar, sound, offset);
    }

    @Override
    public boolean isServiceActive() {
        try {
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // fabric-sound-api-v1 asks the instance itself for the stream
    private static final class EmoteSound extends EmoteSoundInstance implements FabricSoundInstance {
        private EmoteSound(Avatar avatar, OpusSound sound, IntSupplier offset) {
            super(avatar, sound, offset);
        }

        @Override
        public @NonNull CompletableFuture<AudioStream> getAudioStream(@NonNull SoundBufferLibrary library, @NonNull Identifier id, boolean repeatInstantly) {
            return stream();
        }
    }
}
