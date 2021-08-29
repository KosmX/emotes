package io.github.kosmx.emotes.server.serializer;


import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.emote.EmoteFormat;
import io.github.kosmx.emotes.common.tools.UUIDMap;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.serializer.type.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class UniversalEmoteSerializer {
    public static String binaryExtension = EmoteFormat.BINARY.getExtension();
    public static List<IReader> readers = Arrays.asList(new JsonEmoteWrapper(), new QuarkReaderWrapper(), new BinaryFormat());
    public static UUIDMap<EmoteData> serverEmotes = new UUIDMap<>(); //Emotes have stable hash function.


    /**
     * Read an emote file
     * @param inputStream binary reader. No physical file needed
     * @param filename filename. can be null
     * @param format lowercase format string
     * @return List of reader emotes.
     * @throws EmoteSerializerException If the file is not valid or cannot be readed.
     */
    public static List<EmoteData> readData(InputStream inputStream, @Nullable String filename, String format) throws EmoteSerializerException {
        for(IReader reader : readers){
            if(reader.getFormatExtension().equals(format)){
                try {
                    return reader.read(inputStream, filename);
                }
                catch (EmoteSerializerException e){
                    throw e; //We don't need to catch it.
                }
                catch (Exception e){
                    throw new EmoteSerializerException(e.getClass().getName() + " has occurred while serializing an emote", format, e);
                }
            }
        }
        throw new EmoteSerializerException("No known reader for format", format);
    }

    /**
     * Read a file with a not known extension
     * @param inputStream binary file reader
     * @param filename filename. can NOT be null if no format parameter is supplied. {@link UniversalEmoteSerializer#readData(InputStream, String, String)} (InputStream, String)}
     * @return list of emotes
     * @throws EmoteSerializerException exception if something goes wrong
     */
    public static List<EmoteData> readData(InputStream inputStream, String filename) throws EmoteSerializerException{
        if(filename == null || filename.equals(""))throw new IllegalArgumentException("filename can not be null if no format type was given");
        String format = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return readData(inputStream, filename, format);
    }

    /**
     * Write emote into an OStream
     * @param stream output stream
     * @param emote emote
     * @param format target format. See {@link EmoteFormat}
     * @throws EmoteSerializerException this is a dangerous task, can go wrong
     */
    public static void writeEmoteData(OutputStream stream, EmoteData emote, EmoteFormat format) throws EmoteSerializerException{
        for(IReader writerCandidate:readers){
            if(writerCandidate instanceof ISerializer && writerCandidate.getFormatType() == format){
                ISerializer serializer = (ISerializer) writerCandidate;
                serializer.write(emote, stream);
                return;
            }
        }
        throw new EmoteSerializerException("No writer has been found for Format: " + format.toString(), format.getExtension());
    }

    public static void serializeServerEmotes(){
        serverEmotes = new UUIDMap<>();
        if(EmoteInstance.config.loadEmotesServerSide.get()){
            EmoteSerializer.serializeEmotes(serverEmotes, EmoteInstance.instance.getExternalEmoteDir());
        }
        File serverEmotesDir = EmoteInstance.instance.getExternalEmoteDir().toPath().resolve("server").toFile();
        if(! serverEmotesDir.isDirectory()) serverEmotesDir.mkdirs();

        EmoteSerializer.serializeEmotes(serverEmotes, serverEmotesDir);
    }

}
