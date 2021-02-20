package com.kosmx.emotes.executor.dataTypes;

public interface InputKey {
    InputKey getUnknown();

    boolean equals(InputKey key);

    String getTranslationKey();
}
