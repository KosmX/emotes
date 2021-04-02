package io.github.kosmx.emotes.main.quarktool;

import io.github.kosmx.emotes.common.tools.Ease;
import io.github.kosmx.emotes.common.tools.Easing;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Section implements Playable {
    public boolean isForward = true;
    public final List<Playable> elements = new ArrayList<>();
    private final boolean isParallel;
    private int line;
    @Nullable
    private Playable moveOperator;

    public Section(QuarkReader animData, int line, List<List<String>> text) throws QuarkParsingError{
        this.line = line;
        this.isParallel = text.get(line).get(1).equals("parallel");
        while(true){
            this.line++;
            if(this.line >= text.size()) throw new QuarkParsingError();
            if(text.get(this.line).size() == 0 || text.get(this.line).get(0).charAt(0) == '#') continue;
            String id = text.get(this.line).get(0);
            List<String> block = text.get(this.line);
            if(id.equals("end")) return;
            else if(id.equals("section")){
                Section section = new Section(animData, this.line, text);
                Playable moveOp = section.getMoveOperator();
                if(moveOp == null){
                    this.elements.add(section);
                }else{
                    this.elements.add(moveOp);
                }
                this.line = section.getLine();
            }else if(id.equals("move")){
                try{

                    int pos = block.size() - 3;
                    if(block.size() < 4) throw new QuarkParsingError();
                    Ease ease;
                    if(block.size() == 5){
                        ease = Easing.easeFromString(block.get(4));
                    }else if(block.size() == 8 || block.size() == 7 && block.get(5).equals("pause")){
                        ease = Easing.easeFromString(block.get(4));
                    }else ease = Ease.INOUTQUAD;
                    Move move = new Move(animData.getPFromStr(block.get(1)), Float.parseFloat(block.get(3)), (int) (Integer.parseInt(block.get(2)) * 0.02), ease);
                    switch(block.get(pos)){
                        case "repeat":
                            elements.add(new Repeat(move, (int) (Integer.parseInt(block.get(pos + 2)) * 0.02), Integer.parseInt(block.get(pos + 1))));
                            break;
                        case "yoyo":
                            elements.add(new Yoyo(move, (int) (Integer.parseInt(block.get(pos + 2)) * 0.02), Integer.parseInt(block.get(pos + 1))));
                            break;
                        case "pause":
                            elements.add(new Pauseable(move, (int) (Integer.parseInt(block.get(pos + 1)) * 0.02)));
                            break;
                        default:
                            elements.add(move);
                            break;
                    }
                }catch(NumberFormatException e){
                    throw new QuarkParsingError("While trying to add move, error has happened: " + e.getMessage(), this.line);
                }
            }else if(id.equals("repeat")){
                try{
                    this.setMoveOperator(new Repeat(this, (int) (Integer.parseInt(block.get(2)) * 0.02), Integer.parseInt(block.get(1))));
                }catch(NumberFormatException e){
                    throw new QuarkParsingError("While trying to add repeat, error has happened: " + e.getMessage(), this.line);
                }
            }else if(id.equals("yoyo")){
                try{
                    this.setMoveOperator(new Yoyo(this, (int) (Integer.parseInt(block.get(2)) * 0.02), Integer.parseInt(block.get(1))));
                }catch(NumberFormatException e){
                    throw new QuarkParsingError("While trying to add yoyo, error has happened: " + e.getMessage(), this.line);
                }
            }else if(id.equals("pause")){
                try{
                    elements.add(new Pause((int) (Integer.parseInt(block.get(1)) * 0.02)));
                }catch(NumberFormatException e){
                    throw new QuarkParsingError("While trying to add yoyo, error has happened: " + e.getMessage(), this.line);
                }
            }else if(id.equals("reset")){

            }else{
                throw new QuarkParsingError();
            }
        }
    }

    public void setMoveOperator(Playable object) throws QuarkParsingError{
        if(moveOperator != null) throw new QuarkParsingError();
        moveOperator = object;
    }

    public int getLine(){
        return line;
    }

    private int playObject(Playable object, int time) throws QuarkParsingError{
        return isForward ? object.playForward(time) : object.playBackward(time);
    }

    private int play(int time) throws QuarkParsingError{
        if(isParallel) return playParallel(time);
        else return playSequel(time);
    }

    @Nullable
    public Playable getMoveOperator(){
        return this.moveOperator;
    }

    @Override
    public int playForward(int time) throws QuarkParsingError{
        isForward = true;
        return play(time);
    }

    @Override
    public int playBackward(int time) throws QuarkParsingError{
        isForward = false;
        return play(time);
    }

    private int playParallel(int time) throws QuarkParsingError{
        int length = time;
        for(Playable object : elements){
            int t;
            if(isForward) t = object.playForward(time);
            else t = object.playBackward(time);
            if(t > length) length = t;
        }
        return length;
    }

    private int playSequel(int time) throws QuarkParsingError{
        int t = time;
        int i = isForward ? 0 : elements.size() - 1;
        while(true){
            if(i < 0){
                return t;
            }else if(i >= elements.size()){
                return t;
            }
            t = this.playObject(elements.get(i), t);
            i += isForward ? 1 : - 1;
        }
    }
}
