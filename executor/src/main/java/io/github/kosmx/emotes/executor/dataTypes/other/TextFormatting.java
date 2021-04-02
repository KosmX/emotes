package io.github.kosmx.emotes.executor.dataTypes.other;

/**
 * This is a copy form MC but I can't do everything without things like this.
 */
public enum TextFormatting {
    GOLD('6'),
    YELLOW('e')
    ;

    final char code;

    TextFormatting(char code) {
        this.code = code;
    }

    public char getCode() {
        return code;
    }
}
