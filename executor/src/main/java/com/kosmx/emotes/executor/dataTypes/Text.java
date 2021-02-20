package com.kosmx.emotes.executor.dataTypes;

import com.google.gson.JsonElement;

/**
 * MC text is terrible...
 */
public interface Text {
    void setText();
    String getString();
    JsonElement toJsonTree();
}
