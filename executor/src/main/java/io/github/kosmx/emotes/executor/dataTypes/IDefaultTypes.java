package io.github.kosmx.emotes.executor.dataTypes;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.kosmx.emotes.common.CommonData;

public interface IDefaultTypes {
    InputKey getUnknownKey();
    InputKey getKeyFromString(String str);
    InputKey getKeyFromCode(int keyCode, int scanCode);
    InputKey getMouseKeyFromCode(int keyCode);
    Text emptyTex();
    Text textFromString(String str);
    Text fromJson(JsonElement node);

    default Text fromJson(String json){
        if(json == null){
            return textFromString("");
        }
        try {
            return fromJson(new JsonParser().parse(json));
        }catch (JsonParseException e){
            return textFromString(json);
        }
    }

    default Text fromJson(Object obj) {
        if (obj == null || obj instanceof String) {
            return fromJson((String) obj);
        } else if (obj instanceof JsonElement) {
            return fromJson((JsonElement) obj);
        } else throw new IllegalArgumentException("Can not create Text from " + obj.getClass().getName());
    }

    Text newTranslationText(String key);

    IIdentifier newIdentifier(String namespace, String id);

    default IIdentifier newIdentifier(String id){
        return newIdentifier(CommonData.MOD_ID, id);
    }

    Text defaultTextsDone();
    Text defaultTextCancel();

}
