package com.kosmx.emotes.executor.dataTypes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kosmx.emotes.common.CommonData;

public interface IDefaultTypes {
    InputKey getUnknownKey();
    InputKey getKeyFromString(String str);
    Text emptyTex();
    Text textFromString(String str);
    Text fromJson(JsonElement node);
    Text newTranslationText(String key);

    IIdentifier newIdentifier(String namespace, String id);

    default IIdentifier newIdentifier(String id){
        return newIdentifier(CommonData.MOD_ID, id);
    }


}
