package io.github.kosmx.emotes.inline;

import io.github.kosmx.emotes.arch.executor.ClientMethods;
import io.github.kosmx.emotes.arch.executor.Defaults;
import io.github.kosmx.emotes.arch.executor.types.GettersImpl;
import io.github.kosmx.emotes.inline.dataTypes.IClientMethods;
import io.github.kosmx.emotes.inline.dataTypes.IDefaultTypes;
import io.github.kosmx.emotes.inline.dataTypes.IGetters;

public class TmpGetters {


    public static IDefaultTypes getDefaults() {
        return new Defaults();
    }

    public static IGetters getGetters() {
        return new GettersImpl();
    }


    public static IClientMethods getClientMethods() {
        return new ClientMethods();
    }
}
