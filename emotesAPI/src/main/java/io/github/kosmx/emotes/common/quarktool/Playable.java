package io.github.kosmx.emotes.common.quarktool;

public interface Playable {

    int playForward(int time) throws QuarkParsingError;

    int playBackward(int time) throws QuarkParsingError;

}
