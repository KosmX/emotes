package io.github.kosmx.emotes.server.serializer;

import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.opennbs.NBSFileUtils;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.UUIDMap;
import io.github.kosmx.emotes.executor.EmoteInstance;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;


public class EmoteSerializer {


    public static void serializeEmotes(UUIDMap<KeyframeAnimation> emotes, File externalEmotes) {
        if (!externalEmotes.isDirectory()) {
            if (!externalEmotes.mkdir()) {
                return;
            }
        }

        for (File file : Objects.requireNonNull(externalEmotes.listFiles((dir, name) -> name.endsWith(".json")))) {
            emotes.addAll(serializeExternalEmote(file));
        }
        for (File file : Objects.requireNonNull(externalEmotes.listFiles((dir, name) -> name.endsWith("." + AnimationFormat.BINARY.getExtension())))) {
            emotes.addAll(serializeExternalEmote(file));
        }

        if (EmoteInstance.config.enableQuark.get()) {
            EmoteInstance.instance.getLogger().log(Level.INFO, "Quark importer is active", true);
            for (File file : Objects.requireNonNull(externalEmotes.listFiles((dir, name) -> name.endsWith(".emote")))) {
                emotes.addAll(serializeExternalEmote(file));
            }
        }
    }

    private static List<KeyframeAnimation> serializeExternalEmote(File file) {
        File externalEmotes = EmoteInstance.instance.getExternalEmoteDir();
        List<KeyframeAnimation> emotes = new LinkedList<>();
        try {
            InputStream reader = Files.newInputStream(file.toPath());
            emotes = UniversalEmoteSerializer.readData(reader, file.getName());
            //EmoteHolder.addEmoteToList(emotes);
            reader.close();
            Path icon = externalEmotes.toPath().resolve(file.getName().substring(0, file.getName().length() - 5) + ".png");

            if (icon.toFile().isFile()) {
                InputStream iconStream = Files.newInputStream(icon);
                emotes.forEach(emote -> {
                    try {
                        emote.extraData.put("iconData", MathHelper.readFromIStream(iconStream));
                        iconStream.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            File song = externalEmotes.toPath().resolve(file.getName().substring(0, file.getName().length() - 5) + ".nbs").toFile();
            if (song.isFile() && emotes.size() == 1) {

                try (DataInputStream bis = new DataInputStream(Files.newInputStream(song.toPath()))) {
                    emotes.get(0).extraData.put("song", NBSFileUtils.read(bis));
                } catch(IOException exception) {
                    EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while reading song: " + exception.getMessage(), true);
                    if (EmoteInstance.config.showDebug.get()) exception.printStackTrace();
                }
            }
        } catch(Exception e) {
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while importing external emote: " + file.getName() + ".", true);
            EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage());
            if (EmoteInstance.config.showDebug.get()) e.printStackTrace();
        }
        return emotes;
    }

}
