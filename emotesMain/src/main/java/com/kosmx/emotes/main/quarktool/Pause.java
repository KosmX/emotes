package com.kosmx.emotes.main.quarktool;

public class Pause implements Playable {
    private final int len;

    public Pause(int len){
        this.len = len;
    }

    @Override
    public int playForward(int time){
        return time + this.len;
    }

    @Override
    public int playBackward(int time){
        return time + this.len;
    }
}
