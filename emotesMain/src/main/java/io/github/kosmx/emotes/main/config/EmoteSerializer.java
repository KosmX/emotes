package io.github.kosmx.emotes.main.config;

import com.google.gson.*;
import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.common.tools.Easing;
import io.github.kosmx.emotes.common.quarktool.QuarkReader;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


public class EmoteSerializer implements JsonDeserializer<List<EmoteHolder>>, JsonSerializer<EmoteHolder> {

    private final int modVersion = 2;
    @Override
    public List<EmoteHolder> deserialize(JsonElement p, Type typeOf, JsonDeserializationContext ctxt) throws JsonParseException{
        JsonObject node = p.getAsJsonObject();

        if(!node.has("emote")){
            return GeckoLibSerializer.serialize(node);
        }

        int version = 1;
        if(node.has("version")) version = node.get("version").getAsInt();
        Text author = EmoteInstance.instance.getDefaults().emptyTex();
        Text name = EmoteInstance.instance.getDefaults().fromJson(node.get("name"));
        if(node.has("author")){
            author = EmoteInstance.instance.getDefaults().fromJson(node.get("author"));
        }

        if(modVersion < version){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Emote: " + name.getString() + " was made for a newer mod version", true);
            throw new JsonParseException(name.getString() + " is version " + Integer.toString(version) + ". Emotecraft can only process version " + Integer.toString(modVersion) + ".");
        }

        Text description = EmoteInstance.instance.getDefaults().emptyTex();
        if(node.has("description")){
            description = EmoteInstance.instance.getDefaults().fromJson(node.get("description"));
        }
        node.entrySet().forEach((entry)->{
            String string = entry.getKey();
            if(string.equals("author") || string.equals("comment") || string.equals("name") || string.equals("description") || string.equals("emote") || string.equals("version"))
                return;
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Can't understadt: " + string + " : " + entry.getValue());
            EmoteInstance.instance.getLogger().log(Level.WARNING, "If it is a comment, ignore the warning");
        });
        EmoteData emote = emoteDeserializer(node.getAsJsonObject("emote"));
        emote.optimizeEmote();
        List<EmoteHolder> list = new ArrayList<>();
        list.add(new EmoteHolder(emote, name, description, author, node.hashCode()));
        return list;
    }

    private EmoteData emoteDeserializer(JsonObject node) throws JsonParseException{
        EmoteData.EmoteBuilder builder = new EmoteData.EmoteBuilder();
        if(node.has("beginTick")){
            builder.beginTick = node.get("beginTick").getAsInt();
        }
        builder.endTick = node.get("endTick").getAsInt();
        if(builder.endTick <= 0) throw new JsonParseException("endTick must be bigger than 0");
        if(node.has("isLoop") && node.has("returnTick")){
            builder.isLooped = node.get("isLoop").getAsBoolean();
            builder.returnTick = node.get("returnTick").getAsInt();
            if(builder.isLooped && (builder.returnTick > builder.endTick || builder.returnTick < 0))
                throw new JsonParseException("return tick have to be smaller than endTick and not smaller than 0");
        }

        if(node.has("nsfw")){
            builder.nsfw = node.get("nsfw").getAsBoolean();
        }

        node.entrySet().forEach((entry)->{
            String string = entry.getKey();
            if(string.equals("beginTick") || string.equals("comment") || string.equals("endTick") || string.equals("stopTick") || string.equals("degrees") || string.equals("moves") || string.equals("returnTick") || string.equals("isLoop") || string.equals("easeBeforeKeyframe") || string.equals("nsfw"))
                return;
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Can't understadt: " + string + " : " + entry.getValue());
            EmoteInstance.instance.getLogger().log(Level.WARNING, "If it is a comment, ignore the warning");
        });
        builder.stopTick = node.has("stopTick") ? node.get("stopTick").getAsInt() : builder.endTick;
        boolean degrees = ! node.has("degrees") || node.get("degrees").getAsBoolean();
        //EmoteData emote = new EmoteData(beginTick, endTick, resetTick, isLoop, returnTick);
        if(node.has("easeBeforeKeyframe"))builder.isEasingBefore = node.get("easeBeforeKeyframe").getAsBoolean();
        EmoteData emote = builder.build();
        moveDeserializer(emote, node.getAsJsonArray("moves"), degrees);

        emote.fullyEnableParts();

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
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Can't understadt: " + string + " : " + entry.getValue());
                EmoteInstance.instance.getLogger().log(Level.WARNING, "If it is a comment, ignore the warning");
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
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Can't understadt: " + string + " : " + entry.getValue());
                EmoteInstance.instance.getLogger().log(Level.WARNING, "If it is a comment, ignore the warning");
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
     * This code is not used in the mod, but I left it here for modders.
     *
     * If you want to serialize an emote without EmoteHolder
     * do new EmoteHolder(emote, new LiteralText("name").formatted(Formatting.WHITE), new LiteralText("someString").formatted(Formatting.GRAY), new LiteralText("author").formatted(Formatting.GRAY), some random hash(int));
     * (this code is from {@link QuarkReader#getEmote()})
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
        node.addProperty("version", emote.getEmote().isEasingBefore ? 2 : 1); //to make compatible emotes. I won't do it.
        node.add("name", emote.name.toJsonTree());
        node.add("description", emote.description.toJsonTree()); // :D
        if(!emote.author.getString().equals("")){
            node.add("author", emote.author.toJsonTree());
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
        node.addProperty("nsfw", emote.nsfw);
        node.addProperty("degrees", false); //No program uses degrees.
        if(emote.isEasingBefore)node.addProperty("easeBeforeKeyframe", true);
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
        if(bodyPart.isBendable) {
            partDeserialize(node, bodyPart.bend, bodyPart.name);
            partDeserialize(node, bodyPart.bendDirection, bodyPart.name);
        }
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
