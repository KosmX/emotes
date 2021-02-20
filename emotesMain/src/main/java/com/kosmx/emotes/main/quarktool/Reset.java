package com.kosmx.emotes.main.quarktool;

import com.kosmx.emotes.common.tools.Ease;

public class Reset implements Playable {
    private Playable[] parts;

    public Reset(QuarkReader reader, String all, int len) throws QuarkParsingError{
        if(all.equals("all")){
            parts = new Playable[18];
            addParts(0, reader.head, len);
            addParts(3, reader.rightArm, len);
            addParts(6, reader.rightLeg, len);
            addParts(9, reader.leftArm, len);
            addParts(12, reader.leftLeg, len);
            addParts(15, reader.torso, len);
        }else{
            parts = new Playable[3];
            addParts(0, reader.getBPFromStr(all.split("_")), len);
        }
    }

    private void addParts(int i, PartMap part, int len){
        parts[i] = new Move(part.x, 0, len, Ease.INOUTQUAD);
        parts[i + 1] = new Move(part.y, 0, len, Ease.INOUTQUAD);
        parts[i + 2] = new Move(part.z, 0, len, Ease.INOUTQUAD);
    }

    @Override
    public int playForward(int time) throws QuarkParsingError{
        return 0;
    }

    @Override
    public int playBackward(int time) throws QuarkParsingError{
        return 0;
    }
}
