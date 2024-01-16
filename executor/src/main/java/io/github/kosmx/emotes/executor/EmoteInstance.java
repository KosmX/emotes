package io.github.kosmx.emotes.executor;


import io.github.kosmx.emotes.common.SerializableConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class EmoteInstance {
    public static EmoteInstance instance = null;


    public static SerializableConfig config = null;

    public abstract Logger getLogger();

    public abstract boolean isClient();

    public abstract Path getGameDirectory();

    public File getExternalEmoteDir(){
        return getGameDirectory().resolve(config.emotesDir.get()).toFile();
    }

    public Path getConfigPath() {
        String directoryName = "config";

        try {
            directoryName = System.getProperty("emotecraftConfigDir", "config");
            if (directoryName.equals("pluginDefault")) {
                directoryName = "plugins/emotecraft";
            }

        } catch(Throwable ignore) { }


        if (!Files.exists(getGameDirectory().resolve(directoryName))) {
            try {
                Files.createDirectories(getGameDirectory().resolve(directoryName));
            } catch(IOException ignore) {
            }
        }
        return getGameDirectory().resolve(directoryName).resolve("emotecraft.json");
    }

}
