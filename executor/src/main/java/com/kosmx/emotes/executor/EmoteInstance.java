package com.kosmx.emotes.executor;


import com.kosmx.emotes.common.SerializableConfig;
import com.kosmx.emotes.executor.dataTypes.IDefaultTypes;

import javax.annotation.Nonnull;

public interface EmoteInstance {
    EmoteInstance instance = null;

    @Nonnull
    SerializableConfig config = null;
    Logger getLogger();

    IDefaultTypes getDefaults();

}
