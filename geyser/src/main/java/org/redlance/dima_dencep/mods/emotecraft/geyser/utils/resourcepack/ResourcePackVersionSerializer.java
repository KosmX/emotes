package org.redlance.dima_dencep.mods.emotecraft.geyser.utils.resourcepack;

import com.google.gson.*;
import org.geysermc.geyser.pack.GeyserResourcePackManifest;

import java.lang.reflect.Type;

public class ResourcePackVersionSerializer extends GeyserResourcePackManifest.Version.VersionDeserializer implements JsonDeserializer<GeyserResourcePackManifest.Version>, JsonSerializer<GeyserResourcePackManifest.Version> {
    @Override
    public JsonElement serialize(GeyserResourcePackManifest.Version version, Type typeOfSrc, JsonSerializationContext ctx) {
        JsonArray array = new JsonArray(3);
        array.add(version.major());
        array.add(version.minor());
        array.add(version.patch());
        return array;
    }
}
