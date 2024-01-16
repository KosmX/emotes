package io.github.kosmx.emotes.inline.dataTypes;

import com.google.gson.JsonElement;
import io.github.kosmx.emotes.inline.dataTypes.other.EmotesTextFormatting;

/**
 * MC text is terrible...
 */
public interface Text {
    String getString();
    JsonElement toJsonTree();
    Text formatted(EmotesTextFormatting form);
    Text append(Text text);
    Text append(String text);
    Text copyIt();
}
