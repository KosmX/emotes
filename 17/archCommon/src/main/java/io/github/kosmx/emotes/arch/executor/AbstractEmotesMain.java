package io.github.kosmx.emotes.arch.executor;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import io.github.kosmx.emotes.executor.dataTypes.IGetters;
import io.github.kosmx.emotes.arch.executor.types.GettersImpl;

import java.io.File;

public abstract class AbstractEmotesMain extends EmoteInstance {

    @Override
    public IDefaultTypes getDefaults() {
        return new Defaults();
    }

    @Override
    public IGetters getGetters() {
        return new GettersImpl();
    }

    @Override
    public File getExternalEmoteDir() {
        return getGameDirectory().resolve("emotes").toFile();
    }
}
