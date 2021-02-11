package com.kosmx.emotecraft.config;

import com.google.gson.*;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraftCommon.EmoteData;
import com.kosmx.emotecraftCommon.math.Easing;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Type;


public class EmoteSerializer implements JsonDeserializer<EmoteHolder>, JsonSerializer<EmoteHolder> {

    private final int modVersion = 1;
    @Override
    public EmoteHolder deserialize(JsonElement p, Type typeOf, JsonDeserializationContext ctxt) throws JsonParseException{
        JsonObject node = p.getAsJsonObject();

        int version = 1;
        if(node.has("version")) version = node.get("version").getAsInt();
        MutableText author = (MutableText) LiteralText.EMPTY;
        MutableText name = Text.Serializer.fromJson(node.get("name"));
        if(node.has("author")){
            author = Text.Serializer.fromJson(node.get("author"));
        }

        if(modVersion < version){
            Main.log(Level.ERROR, "Emote: " + name.getString() + " was made for a newer mod version", true);
            throw new JsonParseException(name.getString() + " is version " + Integer.toString(version) + ". Emotecraft can only process version " + Integer.toString(modVersion) + ".");
        }

        MutableText description = (MutableText) LiteralText.EMPTY;
        if(node.has("description")){
            description = (LiteralText) Text.Serializer.fromJson(node.get("description"));
        }
        node.entrySet().forEach((entry)->{
            String string = entry.getKey();
            if(string.equals("author") || string.equals("comment") || string.equals("name") || string.equals("description") || string.equals("emote") || string.equals("version"))
                return;
            Main.log(Level.WARN, "Can't understadt: " + string + " : " + entry.getValue());
            Main.log(Level.WARN, "If it is a comment, ignore the warning");
        });
        EmoteData emote = emoteDeserializer(node.getAsJsonObject("emote"));
        return new EmoteHolder(emote, name, description, author, node.hashCode());
    }

    private EmoteData emoteDeserializer(JsonObject node) throws JsonParseException{
        int beginTick = 0;
        if(node.has("beginTick")){
            beginTick = node.get("beginTick").getAsInt();
        }
        int endTick = node.get("endTick").getAsInt();
        if(endTick <= 0) throw new JsonParseException("endTick must be bigger than 0");
        boolean isLoop = false;
        int returnTick = 0;
        if(node.has("isLoop") && node.has("returnTick")){
            isLoop = node.get("isLoop").getAsBoolean();
            returnTick = node.get("returnTick").getAsInt();
            if(isLoop && (returnTick > endTick || returnTick < 0))
                throw new JsonParseException("return tick have to be smaller than endTick and not smaller than 0");
        }

        node.entrySet().forEach((entry)->{
            String string = entry.getKey();
            if(string.equals("beginTick") || string.equals("comment") || string.equals("endTick") || string.equals("stopTick") || string.equals("degrees") || string.equals("moves") || string.equals("returnTick") || string.equals("isLoop"))
                return;
            Main.log(Level.WARN, "Can't understadt: " + string + " : " + entry.getValue());
            Main.log(Level.WARN, "If it is a comment, ignore the warning");
        });
        int resetTick = node.has("stopTick") ? node.get("stopTick").getAsInt() : endTick;
        boolean degrees = ! node.has("degrees") || node.get("degrees").getAsBoolean();
        EmoteData emote = new EmoteData(beginTick, endTick, resetTick, isLoop, returnTick);
        moveDeserializer(emote, node.getAsJsonArray("moves"), degrees);
        return emote;
    }

    private void moveDeserializer(EmoteData emote, JsonArray node, boolean degrees){
        for(JsonElement n : node){
            JsonObject obj = n.getAsJsonObject();
            int tick = obj.get("tick").getAsInt();
            String easing = obj.has("easing") ? obj.get("easing").getAsString() : "linear";
            obj.entrySet().forEach((entry)->{
                String string = entry.getKey();
                if(string.equals("tick") || string.equals("comment") || string.equals("easing") || string.equals("turn") || string.equals("head") || string.equals("torso") || string.equals("rightArm") || string.equals("leftArm") || string.equals("rightLeg") || string.equals("leftLeg"))
                    return;
                Main.log(Level.WARN, "Can't understadt: " + string + " : " + entry.getValue());
                Main.log(Level.WARN, "If it is a comment, ignore the warning");
            });
            int turn = obj.has("turn") ? obj.get("turn").getAsInt() : 0;
            addBodyPartIfExists(emote.head, "head", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.torso, "torso", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.rightArm, "rightArm", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.leftArm, "leftArm", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.rightLeg, "rightLeg", obj, degrees, tick, easing, turn);
            addBodyPartIfExists(emote.leftLeg, "leftLeg", obj, degrees, tick, easing, turn);
        }
    }

    private void addBodyPartIfExists(EmoteData.StateCollection part, String name, JsonObject node, boolean degrees, int tick, String easing, int turn){
        if(node.has(name)){
            JsonObject partNode = node.get(name).getAsJsonObject();
            partNode.entrySet().forEach((entry)->{
                String string = entry.getKey();
                if(string.equals("x") || string.equals("y") || string.equals("z") || string.equals("pitch") || string.equals("yaw") || string.equals("roll") || string.equals("comment") || string.equals("bend") || string.equals("axis"))
                    return;
                Main.log(Level.WARN, "Can't understadt: " + string + " : " + entry.getValue());
                Main.log(Level.WARN, "If it is a comment, ignore the warning");
            });
            addPartIfExists(part.x, "x", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.y, "y", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.z, "z", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.pitch, "pitch", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.yaw, "yaw", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.roll, "roll", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.bend, "bend", partNode, degrees, tick, easing, turn);
            addPartIfExists(part.bendDirection, "axis", partNode, degrees, tick, easing, turn);
        }
    }

    private void addPartIfExists(EmoteData.StateCollection.State part, String name, JsonObject node, boolean degrees, int tick, String easing, int turn){
        if(node.has(name)){
            part.addKeyFrame(tick, node.get(name).getAsFloat(), Easing.easeFromString(easing), turn, degrees);
        }
    }




    /**
     * To serialize emotes to Json.
     * This code was not used in the mod, but I left it here for modders.
     *
     * If you want to serialize an emote without EmoteHolder
     * do new EmoteHolder(emote, new LiteralText("name").formatted(Formatting.WHITE), new LiteralText("someString").formatted(Formatting.GRAY), new LiteralText("author").formatted(Formatting.GRAY), some random hash(int));
     * (this code is from {@link com.kosmx.emotecraft.quarktool.QuarkReader#getEmote()})
     *
     * or use {@link EmoteSerializer#emoteSerializer(EmoteData)}
     *
     *
     * @param emote source EmoteHolder
     * @param typeOfSrc idk
     * @param context :)
     * @return :D
     * Sorry for these really... useful comments
     */
    @Override
    public JsonElement serialize(EmoteHolder emote, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject node = new JsonObject();
        node.add("name", Text.Serializer.toJsonTree(emote.name));
        node.add("description", Text.Serializer.toJsonTree(emote.description)); // :D
        if(!emote.author.getString().equals("")){
            node.add("author", Text.Serializer.toJsonTree(emote.author));
        }
        node.add("emote", emoteSerializer(emote.getEmote()));
        return node;
    }

    /**
     * serialize an emote to json
     * It won't be the same json file (not impossible) but multiple jsons can mean the same emote...
     *
     * Oh, and it's public and static, so you can call it from anywhere.
     *
     * @param emote Emote to serialize
     * @return return Json object
     */
    public static JsonObject emoteSerializer(EmoteData emote){
        JsonObject node = new JsonObject();
        node.addProperty("beginTick", emote.beginTick);
        node.addProperty("endTick", emote.endTick);
        node.addProperty("stopTick", emote.stopTick);
        node.addProperty("isLoop", emote.isInfinite);
        node.addProperty("returnTick", emote.returnToTick);
        node.addProperty("degrees", false); //No program uses degrees.
        node.add("moves", moveSerializer(emote));
        return node;
    }

    public static JsonArray moveSerializer(EmoteData emote){
        JsonArray node = new JsonArray();
        bodyPartDeserializer(node, emote.head);
        bodyPartDeserializer(node, emote.torso);
        bodyPartDeserializer(node, emote.rightArm);
        bodyPartDeserializer(node, emote.leftArm);
        bodyPartDeserializer(node, emote.rightLeg);
        bodyPartDeserializer(node, emote.leftLeg);
        return node;
    }

    /*
     * from here and below the methods are not public
     * these are really depend on the upper method and I don't think anyone will use these.
     */
    private static void bodyPartDeserializer(JsonArray node, EmoteData.StateCollection bodyPart){
        partDeserialize(node, bodyPart.x, bodyPart.name);
        partDeserialize(node, bodyPart.y, bodyPart.name);
        partDeserialize(node, bodyPart.z, bodyPart.name);
        partDeserialize(node, bodyPart.pitch, bodyPart.name);
        partDeserialize(node, bodyPart.yaw, bodyPart.name);
        partDeserialize(node, bodyPart.roll, bodyPart.name);
        partDeserialize(node, bodyPart.bend, bodyPart.name);
        partDeserialize(node, bodyPart.bendDirection, bodyPart.name);
    }

    private static void partDeserialize(JsonArray array, EmoteData.StateCollection.State part, String parentName){
        for(EmoteData.KeyFrame keyFrame : part.keyFrames){
            JsonObject node = new JsonObject();
            node.addProperty("tick", keyFrame.tick);
            node.addProperty("easing", keyFrame.ease.toString());
            JsonObject jsonMove = new JsonObject();
            jsonMove.addProperty(part.name, keyFrame.value);
            node.add(parentName, jsonMove);
            array.add(node);
        }
    }
}
