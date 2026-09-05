package io.github.kosmx.emotes.server.serializer;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.objects.SongPacket;
import io.github.kosmx.emotes.common.opus.OpusSound;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.common.tools.UUIDMap;

import java.io.File;
import java.io.IOException;
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

    public static void serializeEmotes(UUIDMap<Animation> emotes, Path externalEmotes) throws IOException {
        try (Stream<Path> paths = Files.walk(externalEmotes, FileVisitOption.FOLLOW_LINKS)) {
            paths.filter(Files::isRegularFile).filter(
                    file -> UniversalEmoteSerializer.findReader(file.getFileName().toString()).isPresent()
            ).parallel().forEach(file -> {
                String folderPath = externalEmotes.relativize(file.getParent()).normalize()
                        .toString().replace(externalEmotes.getFileSystem().getSeparator(), "/");
                if (folderPath.startsWith("server") || folderPath.contains("_export")) {
                    return;
                }
                emotes.addAll(serializeExternalEmote(file, folderPath).values());
            });
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

            Path sound = file.getParent().resolve(baseFileName + ".opus");
            if (Files.isRegularFile(sound)) {
                try {
                    OpusSound opus = OpusSound.read(sound);

                    for (Animation emote : emotes.values()) { // Avoid lambda
                        emote.data().put(SongPacket.OPUS_KEY, opus);
                    }
                } catch (Throwable th) {
                    CommonData.LOGGER.warn("Error while reading sound: {}", sound.getFileName(), th);
                }
            }

            // Only passed through, so clients old enough to still play it keep their sound
            Path song = file.getParent().resolve(baseFileName + ".nbs");
            if (Files.isRegularFile(song)) {
                try {
                    if (Files.size(song) > CommonData.MAX_PACKET_SIZE) throw new IOException("Song is too big to send");
                    byte[] nbs = Files.readAllBytes(song);

                    for (Animation emote : emotes.values()) { // Avoid lambda
                        emote.data().put(SongPacket.NBS_KEY, nbs);
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
