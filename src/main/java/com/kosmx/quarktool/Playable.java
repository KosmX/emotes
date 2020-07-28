package com.kosmx.quarktool;

import com.kosmx.emotecraft.Emote;

public interface Playable {

    int playForward(int time) throws QuarkParsingError;

    int playBackward(int time) throws QuarkParsingError;

}
