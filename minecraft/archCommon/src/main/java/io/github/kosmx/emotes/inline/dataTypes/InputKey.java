package io.github.kosmx.emotes.inline.dataTypes;

public interface InputKey {
    boolean equals(InputKey key);

    String getTranslationKey();

    Text getLocalizedText();

    boolean equals(Object object);

    int hashCode();
}
