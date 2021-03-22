package com.kosmx.emotes.executor;


import com.kosmx.emotes.common.SerializableConfig;
import com.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import com.kosmx.emotes.executor.dataTypes.IClientMethods;
import com.kosmx.emotes.executor.dataTypes.IGetters;

import java.io.File;
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
    public abstract Path getConfigPath();

}
