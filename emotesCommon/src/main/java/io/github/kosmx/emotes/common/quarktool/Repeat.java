package io.github.kosmx.emotes.common.quarktool;

public class Repeat implements Playable {
    protected final Playable playable;
    protected final int delay;
    protected int count;


    public Repeat(Playable parent, int delay, int count) throws QuarkParsingError{
        this.playable = parent;
        if(count < 0 || count > 128) throw new QuarkParsingError();
        this.count = count;
        this.delay = delay;
    }

    public int playForward(int time) throws QuarkParsingError{
        for(int i = 0; i <= count; i++){
            time = this.playable.playForward(time);
            time += delay;
        }
        return time;
    }

    @Override
    public int playBackward(int time) throws QuarkParsingError{
        throw new QuarkParsingError();
    }
}
