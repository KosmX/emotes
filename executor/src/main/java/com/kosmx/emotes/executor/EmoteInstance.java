package com.kosmx.emotes.executor;


import com.kosmx.emotes.common.SerializableConfig;
import com.kosmx.emotes.executor.dataTypes.IDefaultTypes;

public interface EmoteInstance {
    EmoteInstance instance = null;

    SerializableConfig config = null;
    Logger getLogger();

    IDefaultTypes getDefaults();

}
