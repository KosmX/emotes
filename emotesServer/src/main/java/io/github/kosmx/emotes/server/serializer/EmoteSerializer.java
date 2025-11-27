package io.github.kosmx.emotes.server.serializer;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.common.tools.UUIDMap;
import net.raphimc.noteblocklib.NoteBlockLib;
import net.raphimc.noteblocklib.format.SongFormat;
import net.raphimc.noteblocklib.model.Song;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Serializing emotes.
 */
public class EmoteSerializer {
    public static final String FOLDER_PATH_KEY = "folderpath";
    public static final String BUILT_IN_KEY = "isBuiltin";
    public static final String FILENAME_KEY = "fileName";

    public static void serializeEmotes(UUIDMap<Animation> emotes, Path externalEmotes) {
        if (!Files.isDirectory(externalEmotes)) {
            return; // Just skip
        }

        try (Stream<Path> paths = Files.walk(externalEmotes, FileVisitOption.FOLLOW_LINKS)) {
            paths.filter(
                    file -> UniversalEmoteSerializer.findReader(file.getFileName().toString()).isPresent()
            ).parallel().forEach(file -> {
                String folderPath = externalEmotes.relativize(file.getParent()).normalize()
                        .toString().replace(File.separator, "/");
                if (folderPath.startsWith("server") || folderPath.contains("_export")) {
                    return;
                }
                emotes.addAll(serializeExternalEmote(file, folderPath).values());
            });
        } catch (Throwable e) {
            CommonData.LOGGER.warn("Failed to walk emotes!", e);
        }
    }

    public static Map<String, Animation> serializeExternalEmote(Path file) {
        return EmoteSerializer.serializeExternalEmote(file, null);
    }

    public static Map<String, Animation> serializeExternalEmote(Path file, String folderPath) {
        String fileName = file.getFileName().toString();
        String baseFileName = getBaseName(fileName);

        try (InputStream reader = Files.newInputStream(file)) {
            Map<String, Animation> emotes = UniversalEmoteSerializer.readData(reader, fileName);
            for (Animation emote : emotes.values()) { // Avoid lambda
                ExtraAnimationData data = emote.data();
                if (folderPath != null && !folderPath.isBlank()) {
                    data.put(EmoteSerializer.FOLDER_PATH_KEY, folderPath);
                }
                data.put(EmoteSerializer.FILENAME_KEY, fileName);
                data.data().remove(EmoteSerializer.BUILT_IN_KEY);
            }

            Path icon = file.getParent().resolve(baseFileName + ".png");
            if (Files.isRegularFile(icon)) {
                try (InputStream iconStream = Files.newInputStream(icon)) {
                    final ByteBuffer byteBuffer = MathHelper.readFromIStream(iconStream);

                    for (Animation emote : emotes.values()) { // Avoid lambda
                        emote.data().put("iconData", byteBuffer);
                    }
                } catch (Throwable th) {
                    CommonData.LOGGER.warn("Error while reading icon: {}", icon.getFileName(), th);
                }
            }

            Path song = file.getParent().resolve(baseFileName + ".nbs");
            if (Files.isRegularFile(song)) {
                try {
                    Song nbs = NoteBlockLib.readSong(song, SongFormat.NBS);

                    for (Animation emote : emotes.values()) { // Avoid lambda
                        emote.data().put("song", nbs);
                    }
                } catch (Throwable th) {
                    CommonData.LOGGER.warn("Error while reading song: {}", song.getFileName(), th);
                }
            }

            return emotes;
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Error while importing external emote: {}", file.getFileName(), th);
            return Collections.emptyMap();
        }
    }

    protected static String getBaseName(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            fileName = fileName.substring(0, i);
        }

        return fileName;
    }
}
