package org.redlance.dima_dencep.mods.emotecraft.geyser.utils.resourcepack;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.geysermc.geyser.pack.GeyserResourcePackManifest;

import java.io.IOException;

public class ResourcePackVersionSerializer extends JsonSerializer<GeyserResourcePackManifest.Version> {
    @Override
    public void serialize(GeyserResourcePackManifest.Version version, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartArray();
        generator.writeNumber(version.major());
        generator.writeNumber(version.minor());
        generator.writeNumber(version.patch());
        generator.writeEndArray();
    }
}
