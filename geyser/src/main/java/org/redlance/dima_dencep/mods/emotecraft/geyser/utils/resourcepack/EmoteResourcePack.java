package org.redlance.dima_dencep.mods.emotecraft.geyser.utils.resourcepack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zigythebird.playeranimcore.enums.Axis;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import io.github.kosmx.emotes.common.CommonData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.event.PostOrder;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.entity.property.type.GeyserIntEntityProperty;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineEntityPropertiesEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineResourcePacksEvent;
import org.geysermc.geyser.api.pack.PackCodec;
import org.geysermc.geyser.api.pack.ResourcePack;
import org.geysermc.geyser.api.pack.ResourcePackManifest;
import org.geysermc.geyser.api.util.Identifier;
import org.geysermc.geyser.pack.GeyserResourcePack;
import org.geysermc.geyser.pack.GeyserResourcePackManifest;
import org.redlance.dima_dencep.mods.emotecraft.geyser.animator.GeyserAnimationController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("unused")
public final class EmoteResourcePack extends PackCodec implements EventRegistrar {
    private static final Identifier PLAYER_IDENTIFIER = Identifier.of(Identifier.DEFAULT_NAMESPACE, "player");
    public static final String ANIMATION_NAME = String.format("animation.%s", CommonData.MOD_ID);

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(GeyserResourcePackManifest.Version.class, new ResourcePackVersionSerializer())
            .registerTypeHierarchyAdapter(Collection.class, new CollectionAdapter())
            .disableHtmlEscaping()
            .create();

    private final Map<String, EnumMap<TransformType, EnumMap<Axis, Integer>>> identifiers = new HashMap<>();
    private final Set<GeyserIntEntityProperty> registeredProperties = new HashSet<>(1);
    private final ResourcePackManifest manifest;

    private String molangScript;
    private byte[] packData;
    private byte[] sha256;

    public EmoteResourcePack(GeyserResourcePackManifest.Version version, String name, String description) {
        this(new GeyserResourcePackManifest(2, new GeyserResourcePackManifest.Header(
                UUID.randomUUID(), version, name, description, new GeyserResourcePackManifest.Version(1, 16, 0)),

                Collections.singleton(new GeyserResourcePackManifest.Module(
                        UUID.randomUUID(), version, "resources", description
                )), null, null, null
        ));
    }

    private EmoteResourcePack(ResourcePackManifest manifest) {
        this.manifest = manifest;
    }

    private JsonObject generateAnimation() {
        JsonObject bones = new JsonObject();

        int id = 0;

        this.identifiers.clear();
        for (String boneName : GeyserAnimationController.getRegisteredBones()) {
            JsonObject bone = new JsonObject();

            EnumMap<TransformType, EnumMap<Axis, Integer>> transformType = this.identifiers.computeIfAbsent(boneName,
                    k -> new EnumMap<>(TransformType.class)
            );

            bone.add("rotation", generateBone(id++, id++, id++, 0, transformType.computeIfAbsent(
                    TransformType.ROTATION, k -> new EnumMap<>(Axis.class)
            )));
            bone.add("position", generateBone(id++, id++, id++, 0, transformType.computeIfAbsent(
                    TransformType.POSITION, k -> new EnumMap<>(Axis.class)
            )));
            bone.add("scale", generateBone(id++, id++, id++, 1, transformType.computeIfAbsent(
                    TransformType.SCALE, k -> new EnumMap<>(Axis.class)
            )));

            String bedrockBone = UniversalAnimLoader.restorePlayerBoneName(boneName);
            if ("body".equals(bedrockBone)) bedrockBone = "waist";
            if ("torso".equals(bedrockBone)) bedrockBone = "body";
            bones.add(bedrockBone, bone);
        }

        JsonObject animation = new JsonObject();
        animation.addProperty("animation_length", 1.0F);
        animation.addProperty("loop", true);
        animation.addProperty("override_previous_animation", true);
        animation.add("bones", bones);

        JsonObject container = new JsonObject();
        container.add(ANIMATION_NAME, animation);

        JsonObject parent = new JsonObject();
        parent.add("animations", container);
        parent.addProperty("format_version", "1.8.0");

        return parent;
    }

    private byte[] generatePackData() {
        if (this.packData != null) return this.packData;

        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos)
        ) {
            String manifestJson = EmoteResourcePack.GSON.toJson(this.manifest);
            zos.putNextEntry(new ZipEntry("manifest.json"));
            zos.write(manifestJson.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            try (InputStream is = EmoteResourcePack.class.getClassLoader().getResourceAsStream("emotecraft_mod_logo.png")) {
                zos.putNextEntry(new ZipEntry("pack_icon.png"));
                zos.write(Objects.requireNonNull(is).readAllBytes());
                zos.closeEntry();
            } catch (Throwable th) {
                CommonData.LOGGER.warn("Failed to put icon!", th);
            }

            String animationJson = generateAnimation().toString();
            zos.putNextEntry(new ZipEntry("animations/player.animation.json"));
            zos.write(animationJson.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.finish();
            return this.packData = baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate resource pack", e);
        }
    }

    private JsonArray generateBone(int x, int y, int z, float defaultValue, EnumMap<Axis, Integer> axisMap) {
        axisMap.put(Axis.X, x);
        axisMap.put(Axis.Y, y);
        axisMap.put(Axis.Z, z);

        JsonArray axis = new JsonArray();
        axis.add(this.molangScript
                .replace("{BONE_ID}", String.valueOf(x))
                .replace("{DEFAULT_VALUE}", String.valueOf(defaultValue))
        );
        axis.add(this.molangScript
                .replace("{BONE_ID}", String.valueOf(y))
                .replace("{DEFAULT_VALUE}", String.valueOf(defaultValue))
        );
        axis.add(this.molangScript
                .replace("{BONE_ID}", String.valueOf(z))
                .replace("{DEFAULT_VALUE}", String.valueOf(defaultValue))
        );
        return axis;
    }

    @Override
    public byte @NonNull [] sha256() {
        if (this.sha256 != null) {
            return this.sha256;
        }

        try {
            return this.sha256 = MessageDigest.getInstance("SHA-256").digest(generatePackData());
        } catch (Exception e) {
            throw new RuntimeException("Could not calculate pack hash", e);
        }
    }

    @Override
    public long size() {
        return generatePackData().length;
    }

    @Override
    public @NonNull SeekableByteChannel serialize() {
        return new SeekableInMemoryByteChannel(generatePackData());
    }

    @Override
    protected @NonNull ResourcePack create() {
        return new GeyserResourcePack(this, this.manifest, "");
    }

    @Override
    protected ResourcePack.@NonNull Builder createBuilder() {
        throw new UnsupportedOperationException();
    }

    @Subscribe(postOrder = PostOrder.LAST)
    public void onDefineEntityProperties(GeyserDefineEntityPropertiesEvent event) {
        int reserved = /*Math.max(1, (32 - event.properties(PLAYER_IDENTIFIER).size()) / 3)*/32;
        CommonData.LOGGER.info("{} properties will be reserved by emotecraft! Please ignore the warnings below...", reserved);

        StringBuilder molangScript = new StringBuilder("variable.bone_{BONE_ID} = variable.bone_{BONE_ID} ?? {DEFAULT_VALUE};");
        molangScript.append("variable.bone_{BONE_ID}_target = variable.bone_{BONE_ID}_target ?? {DEFAULT_VALUE};");

        this.registeredProperties.clear();
        for (int i = 0; i < reserved; i++) {
            Identifier identifier = Identifier.of(CommonData.MOD_ID, String.format("property_%s", i));
            this.registeredProperties.add(event.registerIntegerProperty(PLAYER_IDENTIFIER, identifier, Integer.MIN_VALUE, Integer.MAX_VALUE));

            String prop = "variable." + identifier.path();

            molangScript.append(prop).append(" = q.property('").append(identifier).append("');");
            molangScript.append("variable.bone_{BONE_ID}_target = (math.floor(").append(prop).append(" / 10000000) == {BONE_ID}) ? ");
            molangScript.append("((").append(prop).append(" - {BONE_ID} * 10000000 - 1000000) / 100) : variable.bone_{BONE_ID}_target;");
        }

        molangScript.append("variable.bone_{BONE_ID} = math.lerp(variable.bone_{BONE_ID}, variable.bone_{BONE_ID}_target, 0.3);");
        molangScript.append("return variable.bone_{BONE_ID};");

        CommonData.LOGGER.debug("Registered {} properties!", this.registeredProperties.size());
        this.molangScript = molangScript.toString();
    }

    public Set<GeyserIntEntityProperty> getRegisteredProperties() {
        return Collections.unmodifiableSet(this.registeredProperties);
    }

    public Map<Axis, Integer> getAxisIds(String part, TransformType type) {
        return Collections.unmodifiableMap(this.identifiers.get(part).get(type));
    }

    @Subscribe
    public void onDefineResourcePacks(GeyserDefineResourcePacksEvent event) {
        event.register(create());
    }
}
