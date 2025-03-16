package io.github.kosmx.emotes.common.network;

import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * I can't use Minecraft's string and uuid byte reader in a bukkit plugin, I need to implement these.
 * This can still here but it can be removed if unused
 */
public class CommonNetwork {
    public static String readString(ByteBuffer buf) {
        int len = buf.getInt();
        if (len <= 0) return null;
        byte[] b = new byte[len];
        buf.get(b); //that is safe to use.
        return new String(b, StandardCharsets.UTF_8);
    }

    public static void writeString(ByteBuffer buf, String str) {
        if (StringUtils.isBlank(str)) { // Minor optimization to avoid writing empty lines
            buf.putInt(0);
            return;
        }
        byte[] b = str.getBytes(StandardCharsets.UTF_8);
        buf.putInt(b.length);
        buf.put(b);
    }

    public static int stringSize(String str) {
        int size = 4;
        if (!StringUtils.isBlank(str)) {
            size += str.getBytes(StandardCharsets.UTF_8).length;
        }
        return size;
    }

    public static UUID readUUID(ByteBuffer buf){
        long a = buf.getLong();
        long b = buf.getLong();
        return new UUID(a, b); //The order is important
    }

    public static void writeUUID(ByteBuffer buf, UUID uuid){
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
    }

    public static <T> List<T> readList(ByteBuffer buf, Function<ByteBuffer, T> reader) {
        int count = buf.getInt();
        List<T> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(reader.apply(buf));
        }
        return list;
    }

    public static <T> void writeList(ByteBuffer buf, List<T> elements, BiConsumer<ByteBuffer, T> writter) {
        if (elements == null) {
            buf.putInt(0);
            return;
        }

        buf.putInt(elements.size());
        for (T entry : elements) {
            writter.accept(buf, entry);
        }
    }

    public static <T> int listSize(List<T> elements, Function<T, Integer> sizer) {
        int size = 4;
        for (T entry : elements) {
            size += sizer.apply(entry);
        }
        return size;
    }
}
