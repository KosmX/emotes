package io.github.kosmx.emotes.arch.executor;

import io.github.kosmx.emotes.arch.executor.types.GettersImpl;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import io.github.kosmx.emotes.executor.dataTypes.IGetters;

public abstract class AbstractEmotesMain extends EmoteInstance {

    @Override
    public IDefaultTypes getDefaults() {
        return new Defaults();
    }

    @Override
    public IGetters getGetters() {
        return new GettersImpl();
    }

}
