package com.kosmx.emotes.executor.dataTypes;


import com.kosmx.emotes.common.CommonData;

//II as InterfaceIdentifier
//Fabric calls it Identifier, forge IDK
public interface IIdentifier {
    IIdentifier newIdentifier(String namespace, String id);

    default IIdentifier newIdentifier(String id){
        return newIdentifier(CommonData.MOD_ID, id);
    }


}
