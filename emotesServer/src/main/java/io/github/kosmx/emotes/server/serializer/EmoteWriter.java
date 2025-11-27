package io.github.kosmx.emotes.server.serializer;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.server.serializer.type.IWriter;
import net.raphimc.noteblocklib.NoteBlockLib;
import net.raphimc.noteblocklib.model.Song;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class EmoteWriter {
    private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");

    public static void writeAnimationInBestFormat(Animation animation, Path exportDir) throws Exception {
        writeAnimationInFormat(animation, exportDir, UniversalEmoteSerializer.findWriter(null));
    }

    public static void writeAnimationInFormat(Animation animation, Path exportDir, IWriter format) throws Exception {
        Path file = createFileName(animation, exportDir, format.getExtension());

        try (OutputStream stream = Files.newOutputStream(file)) {
            format.write(animation, stream, file.getFileName().toString());
        }

        if (format.onlyEmoteFile()) {
            String fileName = EmoteSerializer.getBaseName(file.getFileName().toString());

            if (animation.data().getRaw("iconData") instanceof ByteBuffer iconData) {
                Path iconPath = exportDir.resolve(fileName + ".png");
                if (Files.exists(iconPath)) throw new FileAlreadyExistsException(iconPath.toString());

                try (OutputStream iconStream = Files.newOutputStream(iconPath)) {
                    iconStream.write(MathHelper.safeGetBytesFromBuffer(iconData));
                    iconStream.flush();
                }
            }

            if (animation.data().getRaw("song") instanceof Song song) {
                Path songPath = exportDir.resolve(fileName + ".nbs");
                if (Files.exists(songPath)) throw new FileAlreadyExistsException(songPath.toString());
                NoteBlockLib.writeSong(song, songPath);
            }
        }
    }

    private static Path createFileName(Animation emote, Path originPath, String ext) {
        String fileName = emote.data().<String>get(EmoteSerializer.FILENAME_KEY)
                .map(EmoteSerializer::getBaseName)
                .orElseGet(emote.data()::name);

        if (fileName == null) throw new NullPointerException();
        String baseName = EmoteWriter.INVALID_FILENAME_CHARS.matcher(fileName).replaceAll("#");
        Path file = originPath.resolve(baseName + "." + ext);

        int i = 1;
        while (Files.exists(file)) {
            file = originPath.resolve(baseName + "_" + i++ + "." + ext);
        }

        return file;
    }
}
