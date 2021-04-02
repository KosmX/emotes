package io.github.kosmx.emotes.main.quarktool;

public interface Playable {

    int playForward(int time) throws QuarkParsingError;

    int playBackward(int time) throws QuarkParsingError;

}
