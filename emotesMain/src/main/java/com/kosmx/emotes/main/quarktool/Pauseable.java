package com.kosmx.emotes.main.quarktool;

public class Pauseable implements Playable {
    private final Playable playable;
    private final int len;


    public Pauseable(Playable playable, int len){
        this.playable = playable;
        this.len = len;
    }


    @Override
    public int playForward(int time) throws QuarkParsingError{
        return playable.playForward(time + this.len);
    }

    @Override
    public int playBackward(int time) throws QuarkParsingError{
        return playable.playBackward(time) + this.len;
    }
}
