package io.github.kosmx.emotes.main.quarktool;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.other.EmotesTextFormatting;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.common.emote.EmoteData;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Stream;

public class QuarkReader {
    private final EmoteData.EmoteBuilder emote = new EmoteData.EmoteBuilder();
    private boolean isSuccess = false;
    private String name;
    final PartMap head = new PartMap(emote.head);
    final PartMap torso = new PartMap(emote.torso);
    final PartMap rightLeg = new PartMap(emote.rightLeg);
    final PartMap leftLeg = new PartMap(emote.leftLeg);
    final PartMap rightArm = new PartMap(emote.rightArm);
    final PartMap leftArm = new PartMap(emote.leftArm);
    private int hash;
    private Playable animation;

    public boolean deserialize(BufferedReader reader, String name){
        this.name = name;
        List<List<String>> strings = new ArrayList<>();
        Stream<String> stream = reader.lines();
        stream.forEach((s->{
            strings.add(read(s.replaceAll("\t", "")));
        }));
        this.hash = strings.hashCode();
        int i = 0;
        try{
            while(i < strings.size()){
                if(strings.get(i).size() == 0 || strings.get(i).get(0).charAt(0) == '#'){
                    i++;
                    continue;
                }
                i = getMethod(strings.get(i), i, strings);
            }
        }catch(QuarkParsingError e){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while importing quark at line " + i + ": " + e.message);
            if(EmoteInstance.config.showDebug.get()) e.printStackTrace();
        }
        try{
            if(this.animation == null){
                throw new QuarkParsingError();
            }
            int length = this.animation.playForward(0);
            this.isSuccess = true;
            this.emote.endTick = length;
        }catch(QuarkParsingError e){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while parsing quark: " + e.getMessage());
            if(EmoteInstance.config.showDebug.get()) e.printStackTrace();
        }
        return this.isSuccess;
    }

    public EmoteHolder getEmote(){
        if(isSuccess){
            //return new EmoteHolder(this.emote.build(), new LiteralText(this.name).formatted(Formatting.WHITE), new LiteralText("Imported from quark").formatted(Formatting.GRAY), new LiteralText("").formatted(Formatting.GRAY), this.hash).setQuarkEmote(true);
            return new EmoteHolder(this.emote.build().optimizeEmote(),
                                   EmoteInstance.instance.getDefaults().textFromString(this.name).formatted(EmotesTextFormatting.WHITE),
                                   EmoteInstance.instance.getDefaults().textFromString("Imported from quark").formatted(EmotesTextFormatting.GRAY),
                                   EmoteInstance.instance.getDefaults().emptyTex(),
                                   this.hash).setQuarkEmote(true);
        }else return null;
    }

    public static List<String> read(String s){
        int i = 0;
        while(i < s.length() && s.charAt(i) == ' '){
            i++;
        }
        s = new StringBuffer(s).replace(0, i, "").toString();
        List<String> list = new ArrayList<>(Arrays.asList(s.split(" ")));
        list.removeIf(s1->{
            return s1.equals("");
        });
        return list;
    }

    public int getMethod(List<String> str, int i, List<List<String>> strings) throws QuarkParsingError{
        if(str.get(0).equals("name")){
            this.name = str.get(1);
        }else if(str.get(0).equals("animation")){
            Section anim = new Section(this, i, strings);
            if(anim.getMoveOperator() == null){
                this.animation = anim;
            }else this.animation = anim.getMoveOperator();
            return anim.getLine();
        }
        return i + 1;
    }

    public PartMap getBPFromStr(String[] inf) throws QuarkParsingError{
        if(inf.length == 2){
            if(inf[0].equals("body")) return this.torso;
            else if(inf[0].equals("head")) return this.head;
            else throw new QuarkParsingError();
        }else if(inf.length == 3){
            if(inf[0].equals("right")){
                if(inf[1].equals("arm")) return this.rightArm;
                else if(inf[1].equals("leg")) return this.rightLeg;
                else throw new QuarkParsingError();
            }else if(inf[0].equals("left")){
                if(inf[1].equals("arm")) return this.leftArm;
                else if(inf[1].equals("leg")) return this.leftLeg;
                else throw new QuarkParsingError();
            }else throw new QuarkParsingError();
        }else throw new QuarkParsingError();
    }

    public PartMap.PartValue getPFromStr(String str) throws QuarkParsingError{
        String[] inf = str.split("_");
        return getPFromStrHelper(inf[inf.length - 1], getBPFromStr(inf));
    }

    private PartMap.PartValue getPFromStrHelper(String string, PartMap part) throws QuarkParsingError{
        if(string.equals("x")) return part.x;
        else if(string.equals("y")) return part.y;
        else if(string.equals("z")) return part.z;
        else throw new QuarkParsingError();
    }

}
