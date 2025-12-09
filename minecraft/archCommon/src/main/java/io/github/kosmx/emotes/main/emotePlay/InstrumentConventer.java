package io.github.kosmx.emotes.main.emotePlay;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.emotePlay.instances.SoundDirectInstance;
import io.github.kosmx.emotes.main.emotePlay.instances.SoundEventInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.Vec3;
import net.raphimc.noteblocklib.data.MinecraftInstrument;
import net.raphimc.noteblocklib.format.nbs.model.NbsCustomInstrument;
import net.raphimc.noteblocklib.model.Note;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class InstrumentConventer {
    public static SoundInstance getInstrument(Note note, Vec3 pos) {
        if (note.getInstrument() instanceof MinecraftInstrument instrument) {
            NoteBlockInstrument noteBlock = convertToNoteBlock(instrument.mcId());

            return createForNoteBlock(noteBlock, pos, note.getVolume(), note.getPitch());
        } else if (note.getInstrument() instanceof NbsCustomInstrument instrument) {
            String file = instrument.getSoundFilePathOr("").replace(File.separatorChar, '/');
            if (file.endsWith(".ogg")) {
                file = file.substring(0, file.length() - 4);
            }

            // support for old nbs files that encoded the pling sound as custom instrument
            /*if (file.equalsIgnoreCase("pling")) {
                return createForNoteBlock(NoteBlockInstrument.PLING, pos, note.getVolume(), note.getPitch());
            }*/

            Identifier sound = parseSoundFile(Minecraft.getInstance().getResourceManager(), file);
            if (sound != null) {
                return new SoundDirectInstance(sound, note.getVolume(), note.getPitch(), pos);
            }

            SoundEvent event = parseSoundName(instrument.getNameOr(""));
            if (event != null) {
                return new SoundEventInstance(event, note.getVolume(), note.getPitch(), pos);
            }

            CommonData.LOGGER.warn("Failed parse custom instrument: name={}, file={}", instrument.getNameOr(""), file);
            return new SoundDirectInstance(SoundManager.EMPTY_SOUND, note.getVolume(), note.getPitch(), pos);
        } else {
            CommonData.LOGGER.warn("Unsupported instrument type: {}", note.getInstrument().getClass().getName());
            return createForNoteBlock(NoteBlockInstrument.HARP, pos, note.getVolume(), note.getPitch());
        }
    }

    private static NoteBlockInstrument convertToNoteBlock(int mcId) {
        NoteBlockInstrument[] instruments = NoteBlockInstrument.values();
        if(mcId >= 0 && mcId < instruments.length){
            return instruments[mcId];
        }
        return NoteBlockInstrument.HARP; //I don't want to crash here
    }

    private static SoundInstance createForNoteBlock(NoteBlockInstrument instrument, Vec3 pos, float volume, float pitch) {
        return new SoundEventInstance(instrument.getSoundEvent().value(), volume, pitch, pos);
    }

    public static @Nullable Identifier parseSoundFile(ResourceManager manager, String file) {
        Identifier first = Identifier.tryParse(file);
        if (first != null && manager.getResource(Sound.SOUND_LISTER.idToFile(first)).isPresent()) {
            return first;
        }

        int namespaceIndex = file.indexOf("/");
        String namespace = namespaceIndex != -1 ? file.substring(0, namespaceIndex) : file;
        String path = namespaceIndex != -1 && file.startsWith(namespace) ? file.substring(namespace.length() + 1) : file;
        Identifier second = Identifier.tryBuild(namespace, path);
        if (second != null && manager.getResource(Sound.SOUND_LISTER.idToFile(second)).isPresent()) {
            return second;
        }

        return null;
    }

    public static @Nullable SoundEvent parseSoundName(@NotNull String name) {
        Identifier first = Identifier.tryParse(name);
        if (first != null && BuiltInRegistries.SOUND_EVENT.containsKey(first)) {
            return BuiltInRegistries.SOUND_EVENT.getValue(first);
        }

        if (name.contains(".firework.")) { // Migrations
            return parseSoundName(name.replace(".firework.", ".firework_rocket."));
        }
        return null;
    }
}
