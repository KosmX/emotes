package com.kosmx.emotes.main.quarktool;

import com.kosmx.emotes.common.tools.Ease;

public class Move implements Playable {
    private final Ease ease;
    private final float value;
    private float valueBefore;
    private float valueAfter;
    private boolean isInitialized = false;
    private PartMap.PartValue part;
    private final int length;

    public Move(PartMap.PartValue part, float value, int length, Ease ease){
        this.ease = ease;
        this.length = length;
        this.value = value;
        this.part = part;
    }

    @Override
    public int playForward(int time) throws QuarkParsingError{
        if(! isInitialized){
            this.isInitialized = true;
            this.valueBefore = part.getValue();
            this.valueAfter = this.value;
            this.part.addValue(time, time + this.length, this.valueAfter, ease);
        }else{
            this.part.hold();
            this.part.addValue(time, valueBefore, ease);
            this.part.addValue(time + this.length, valueAfter, Ease.CONSTANT);
            //this.part.setValue(this.valueAfter);
        }
        return time + this.length;
    }

    @Override
    public int playBackward(int time) throws QuarkParsingError{
        if(! isInitialized) throw new QuarkParsingError();
        this.part.hold();
        this.part.addValue(time, this.valueAfter, InverseEase.inverse(ease));
        this.part.addValue(time + this.length, this.valueBefore, Ease.CONSTANT);
        //this.part.setValue(this.valueBefore);
        return time + this.length;
    }
}
