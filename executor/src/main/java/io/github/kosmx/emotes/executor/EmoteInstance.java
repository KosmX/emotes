package io.github.kosmx.emotes.executor;


import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import io.github.kosmx.emotes.executor.dataTypes.IClientMethods;
import io.github.kosmx.emotes.executor.dataTypes.IGetters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class EmoteInstance {
    public static EmoteInstance instance = null;


    public static SerializableConfig config = null;
    public abstract Logger getLogger();

    public abstract IDefaultTypes getDefaults();

    public abstract IGetters getGetters();

    public abstract IClientMethods getClientMethods();

    public abstract boolean isClient();

    public abstract Path getGameDirectory();
    public abstract File getExternalEmoteDir();
    public Path getConfigPath(){
        if(!Files.exists(getGameDirectory().resolve("config"))){
            try {
                Files.createDirectories(getGameDirectory().resolve("config"));
            }
            catch (IOException ignore){
            }
        }
        return getGameDirectory().resolve("config").resolve("emotecraft.json");
    }

}
