package io.github.kosmx.emotes.common.quarktool;

public class QuarkParsingError extends Exception {
    final String message;

    public QuarkParsingError(String message, int i){
        this.message = "at line " + i + " " + message;
    }

    public QuarkParsingError(){
        this.message = null;
    }
}
