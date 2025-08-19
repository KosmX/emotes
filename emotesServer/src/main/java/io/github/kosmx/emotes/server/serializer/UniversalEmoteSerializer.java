package io.github.kosmx.emotes.server.serializer;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.tools.MathHelper;
import io.github.kosmx.emotes.common.tools.ServiceLoaderUtil;
import io.github.kosmx.emotes.common.tools.UUIDMap;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.type.*;

import io.github.kosmx.emotes.server.services.InstanceService;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class UniversalEmoteSerializer {
    public static final List<IReader> READERS = ServiceLoaderUtil.loadServicesSorted(IReader.class).toList();
    public static final UUIDMap<Animation> SERVER_EMOTES = new UUIDMap<>(); // Emotes have stable hash function.
    public static final UUIDMap<Animation> HIDDEN_SERVER_EMOTES = new UUIDMap<>(); // server-side loaded but NOT streamed emotes.

    /**
     * Read an emote file
     * @param inputStream binary reader. No physical file needed
     * @param filename filename. can be null
     * @param format lowercase format string
     * @return List of reader emotes.
     * @throws EmoteSerializerException If the file is not valid or cannot be readed.
     */
    public static List<Animation> readData(InputStream inputStream, @Nullable String filename, String format) throws EmoteSerializerException {
        return UniversalEmoteSerializer.readData(inputStream, Objects.requireNonNullElse(filename, "emote." + format));
    }

    /**
     * Read an emote file
     * @param inputStream binary file reader
     * @param fileName filename. can NOT be null if no format parameter is supplied.
     * @return list of emotes
     * @throws EmoteSerializerException exception if something goes wrong
     */
    public static List<Animation> readData(InputStream inputStream, String fileName) throws EmoteSerializerException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("filename can not be null if no format type was given");
        }

        try {
            return UniversalEmoteSerializer.findReader(fileName)
                    .orElseThrow(() -> new EmoteSerializerException("No known reader for format", fileName))
                    .read(inputStream, fileName);
        } catch (EmoteSerializerException e) {
            throw e; //We don't need to catch it.
        } catch (Throwable cause) {
            throw new EmoteSerializerException("Error has occurred while serializing an emote", fileName, cause);
        }
    }

    public static Optional<IReader> findReader(String fileName) {
        return UniversalEmoteSerializer.READERS.stream()
                .filter(reader -> reader.canRead(fileName))
                .findFirst();
    }

    public static ISerializer findBestSerializer() throws EmoteSerializerException{
        return UniversalEmoteSerializer.getSerializers()
                .max(Comparator.comparingInt(t -> t.onlyEmoteFile() ? 0 : 1))
                .orElseThrow(() -> new EmoteSerializerException("No writer has been found!", null));
    }

    /**
     * Write emote into an OStream
     * @param stream output stream
     * @param emote emote
     * @param fileName target format.
     * @throws EmoteSerializerException this is a dangerous task, can go wrong
     */
    public static void writeKeyframeAnimation(OutputStream stream, Animation emote, String fileName) throws EmoteSerializerException {
        UniversalEmoteSerializer.findBestSerializer().write(emote, stream, fileName);
    }

    public static Stream<ISerializer> getSerializers() {
        return UniversalEmoteSerializer.READERS.stream()
                .filter(reader -> reader instanceof ISerializer)
                .map(reader -> (ISerializer) reader);
    }

    public static UUIDMap<Animation> loadEmotes() {
        SERVER_EMOTES.clear();
        HIDDEN_SERVER_EMOTES.clear();

        serializeInternalJson("waving");
        serializeInternalJson("clap");
        serializeInternalJson("crying");
        serializeInternalJson("point");
        serializeInternalJson("here");
        serializeInternalJson("palm");
        serializeInternalJson("backflip");
        serializeInternalJson("roblox_potion_dance");
        serializeInternalJson("kazotsky_kick");
        serializeInternalJson("twerk");
        serializeInternalJson("club_penguin_dance");

        Path path = InstanceService.INSTANCE.getExternalEmoteDir();
        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectories(path);
            } catch(IOException ignored) {
            }
        }

        EmoteSerializer.serializeEmotes(Serializer.getConfig().loadEmotesServerSide.get() ? SERVER_EMOTES : HIDDEN_SERVER_EMOTES, path);

        Path serverEmotesDir = path.resolve("server");
        if (Files.isDirectory(serverEmotesDir)) {
            EmoteSerializer.serializeEmotes(SERVER_EMOTES, serverEmotesDir);
        }

        return UniversalEmoteSerializer.getLoadedEmotes();
    }

    private static void serializeInternalJson(String name){
        if (!Serializer.getConfig().loadBuiltinEmotes.get()) {
            return;
        }
        try (InputStream stream = UniversalEmoteSerializer.class.getResourceAsStream("/assets/" + CommonData.MOD_ID + "/emotes/" + name + ".json")) {
            List<Animation> emotes = UniversalEmoteSerializer.readData(stream, name + ".json");

            for (Animation emote : emotes) {
                emote.data().put(EmoteSerializer.BUILT_IN_KEY, true);

                InputStream iconStream = UniversalEmoteSerializer.class.getResourceAsStream("/assets/" + CommonData.MOD_ID + "/emotes/" + name + ".png");
                if(iconStream != null) {
                    emote.data().put("iconData", MathHelper.readFromIStream(iconStream));
                    iconStream.close();
                }
            }

            HIDDEN_SERVER_EMOTES.addAll(emotes);
        } catch (EmoteSerializerException | IOException e) {
            CommonData.LOGGER.warn("Failed to load built-in emote!", e);
        }
    }

    /**
     * Get the emote by its UUID
     * @param uuid Emotes UUID
     * @return Emote or null if no such emote
     */
    @Nullable
    public static Animation getEmote(UUID uuid) {
        return SERVER_EMOTES.getOrDefault(uuid, HIDDEN_SERVER_EMOTES.get(uuid));
    }

    /**
     * Returns a copy of the list of all loaded emotes
     * @return all server-side loaded emotes
     */
    public static UUIDMap<Animation> getLoadedEmotes() {
        UUIDMap<Animation> map = new UUIDMap<>();
        map.putAll(UniversalEmoteSerializer.HIDDEN_SERVER_EMOTES);
        map.putAll(UniversalEmoteSerializer.SERVER_EMOTES);
        return map;
    }

    public static Stream<ByteBuffer> preparePackets(HashMap<Byte, Byte> compatibilityMap) {
        return UniversalEmoteSerializer.SERVER_EMOTES.values().stream().map(emote -> {
            try {
                return new EmotePacket.Builder().configureToSaveEmote(emote).setSizeLimit(0x100000, false).setVersion(compatibilityMap).build().write();
            } catch (IOException e) {
                CommonData.LOGGER.warn("Failed to prepare emote packet!", e);
                return null;
            }
        }).filter(Objects::nonNull);
    }
}
