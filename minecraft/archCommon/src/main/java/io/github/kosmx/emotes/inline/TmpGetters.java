package io.github.kosmx.emotes.inline;

import io.github.kosmx.emotes.arch.executor.ClientMethods;
import io.github.kosmx.emotes.arch.executor.Defaults;

public class TmpGetters {


    public static Defaults getDefaults() {
        return new Defaults();
    }

    public static ClientMethods getClientMethods() {
        return new ClientMethods();
    }
}
