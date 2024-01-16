package io.github.kosmx.emotes.inline;

import io.github.kosmx.emotes.arch.executor.Defaults;
import io.github.kosmx.emotes.arch.executor.types.GettersImpl;
import io.github.kosmx.emotes.executor.dataTypes.IClientMethods;
import io.github.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import io.github.kosmx.emotes.executor.dataTypes.IGetters;

public class TmpGetters {


    public static IDefaultTypes getDefaults() {
        return new Defaults();
    }

    public static IGetters getGetters() {
        return new GettersImpl();
    }


    public static IClientMethods getClientMethods() {
        return null;
    }
}
