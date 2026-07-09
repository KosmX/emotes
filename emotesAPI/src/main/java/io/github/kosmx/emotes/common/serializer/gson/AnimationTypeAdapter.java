package io.github.kosmx.emotes.common.serializer.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketBound;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.lang.reflect.Type;
import java.util.Base64;

/**
 * (De)serializes an {@link Animation} to the Emotecraft binary format as a Base64 JSON string, so an emote can be
 * embedded verbatim (e.g. in a config) rather than referenced by UUID. Ported from the Jackson variant; no downgrade.
 */
public final class AnimationTypeAdapter implements JsonSerializer<Animation>, JsonDeserializer<Animation> {
    public static final AnimationTypeAdapter INSTANCE = new AnimationTypeAdapter();

    private AnimationTypeAdapter() {}

    public static byte[] toBytes(Animation animation) {
        ByteBuf buf = Unpooled.buffer();
        try {
            new EmotePacket.Builder()
                    .setVersion(EmotePacket.defaultVersions)
                    .configureToSaveEmote(animation)
                    .build()
                    .write(buf, PacketBound.CLIENT);
            return MathHelper.readBytes(buf);
        } finally {
            buf.release();
        }
    }

    public static Animation fromBytes(byte[] bytes) {
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);
        try {
            NetData data = new EmotePacket(buf, PacketBound.CLIENT).data;
            if (data.emoteData == null) {
                throw new IllegalStateException("Binary emote is invalid!");
            }
            return data.emoteData;
        } finally {
            buf.release();
        }
    }

    public static String toBase64(Animation animation) {
        return Base64.getEncoder().encodeToString(toBytes(animation));
    }

    public static Animation fromBase64(String base64) {
        return fromBytes(Base64.getDecoder().decode(base64));
    }

    @Override
    public JsonElement serialize(Animation src, Type type, JsonSerializationContext ctx) {
        return new JsonPrimitive(toBase64(src));
    }

    @Override
    public Animation deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        try {
            return fromBase64(json.getAsString());
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Failed to deserialize embedded animation!", th);
            return null;
        }
    }
}
