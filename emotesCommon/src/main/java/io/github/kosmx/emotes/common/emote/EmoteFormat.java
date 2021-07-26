package io.github.kosmx.emotes.common.emote;

/**
 * Where is the emote from
 */
public enum EmoteFormat {
    JSON_EMOTECRAFT("json"),
    JSON_MC_ANIM("json"),
    QUARK("emote"),
    BINARY("emotecraft"),
    UNKNOWN(null);

    private final String extension;


    EmoteFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
