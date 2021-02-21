package com.kosmx.emotes.executor;


import com.kosmx.emotes.common.SerializableConfig;
import com.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import com.kosmx.emotes.executor.dataTypes.IClientMethods;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public interface EmoteInstance {
    EmoteInstance instance = null;

    @Nonnull
    SerializableConfig config = null;
    Logger getLogger();

    IDefaultTypes getDefaults();

    IClientMethods getClientMethods();

    boolean isClient();

    Path getGameDirectory();

}
