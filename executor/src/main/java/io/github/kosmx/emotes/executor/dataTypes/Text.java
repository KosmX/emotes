package io.github.kosmx.emotes.executor.dataTypes;

import com.google.gson.JsonElement;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.other.TextFormatting;

/**
 * MC text is terrible...
 */
public interface Text {
    String getString();
    JsonElement toJsonTree();
    Text formatted(TextFormatting form);
    Text append(Text text);
    default Text append(String text){
        return append(EmoteInstance.instance.getDefaults().textFromString(text));
    }
}
