package com.kosmx.emotes.executor.dataTypes;

import com.google.gson.JsonElement;
import com.kosmx.emotes.executor.dataTypes.other.TextFormatting;

/**
 * MC text is terrible...
 */
public interface Text {
    void setText();
    String getString();
    JsonElement toJsonTree();
    Text formatted(TextFormatting form);
    Text append(Text text);
}
