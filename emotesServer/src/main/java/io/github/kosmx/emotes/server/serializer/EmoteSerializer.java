package io.github.kosmx.emotes.server.serializer;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.UUIDMap;
import io.github.kosmx.emotes.api.services.LoggerService;
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
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Serializing emotes.
 */
public class EmoteSerializer {
    public static final String FOLDER_PATH_KEY = "folderpath";

    public static void serializeEmotes(UUIDMap<KeyframeAnimation> emotes, Path externalEmotes) {
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
                emotes.addAll(serializeExternalEmote(file, folderPath));
            });
        } catch (Throwable e) {
            LoggerService.INSTANCE.log(Level.WARNING, "Failed to walk emotes!", e);
        }
    }

    public static List<KeyframeAnimation> serializeExternalEmote(Path file) {
        return EmoteSerializer.serializeExternalEmote(file, null);
    }

    public static List<KeyframeAnimation> serializeExternalEmote(Path file, String folderPath) {
        String fileName = file.getFileName().toString();
        String baseFileName = getBaseName(fileName);

        try (InputStream reader = Files.newInputStream(file)) {
            List<KeyframeAnimation> emotes = UniversalEmoteSerializer.readData(reader, fileName);
            if (folderPath != null && !folderPath.isBlank()) {
                for (KeyframeAnimation emote : emotes) { // Avoid lambda
                    emote.extraData.put(EmoteSerializer.FOLDER_PATH_KEY, folderPath);
                }
            }

            Path icon = file.getParent().resolve(baseFileName + ".png");
            if (Files.isRegularFile(icon)) {
                try (InputStream iconStream = Files.newInputStream(icon)) {
                    final ByteBuffer byteBuffer = MathHelper.readFromIStream(iconStream);

                    for (KeyframeAnimation emote : emotes) { // Avoid lambda
                        emote.extraData.put("iconData", byteBuffer);
                    }
                } catch (Throwable th) {
                    LoggerService.INSTANCE.log(Level.WARNING, "Error while reading icon: " + icon.getFileName(), th);
                }
            }

            Path song = file.getParent().resolve(baseFileName + ".nbs");
            if (Files.isRegularFile(song)) {
                try {
                    Song nbs = NoteBlockLib.readSong(song, SongFormat.NBS);

                    for (KeyframeAnimation emote : emotes) { // Avoid lambda
                        emote.extraData.put("song", nbs);
                    }
                } catch (Throwable th) {
                    LoggerService.INSTANCE.log(Level.WARNING, "Error while reading song: " + song.getFileName(), th);
                }
            }

            return emotes;
        } catch (Throwable th) {
            LoggerService.INSTANCE.log(Level.WARNING, "Error while importing external emote: " + file.getFileName(), th);
            return Collections.emptyList();
        }
    }

    private static String getBaseName(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            fileName = fileName.substring(0, i);
        }

        return fileName;
    }
}
