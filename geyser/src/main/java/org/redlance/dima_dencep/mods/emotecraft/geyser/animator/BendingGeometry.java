package org.redlance.dima_dencep.mods.emotecraft.geyser.animator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import io.github.kosmx.emotes.common.CommonData;
import org.geysermc.geyser.api.skin.SkinGeometry;
import org.geysermc.geyser.skin.SkinManager;
import org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery.ReflectHacks;

import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Set;

public class BendingGeometry {
    private static final VarHandle SKIN_MANAGER_GEOMETRY = ReflectHacks.uncheck(() -> ReflectHacks.TRUSTED_LOOKUP.findStaticVarHandle(
            SkinManager.class, "GEOMETRY", String.class
    ));

    public static final Set<String> BENDABLE_BONES = Set.of(
            "right_arm", "left_arm",
            "body",
            "right_leg", "left_leg"
    );
    public static final String BEND_SUFFIX = "_bend";

    public static SkinGeometry addBoneBends(SkinGeometry geometry) {
        JsonObject geometryObj = PlayerAnimLib.GSON.fromJson(
                geometry.geometryData().isBlank() ? (String) SKIN_MANAGER_GEOMETRY.get() : geometry.geometryData(), JsonObject.class
        );
        for (JsonElement element : geometryObj.getAsJsonArray("minecraft:geometry")) {
            addBoneBends(element.getAsJsonObject());
        }
        return new SkinGeometry(geometry.geometryName(), PlayerAnimLib.GSON.toJson(geometryObj));
    }

    private static void addBoneBends(JsonObject geometry) {
        String identifier = geometry.getAsJsonObject("description").get("identifier").getAsString();
        CommonData.LOGGER.info("Patching '{}' for bends...", identifier);

        JsonArray bones = geometry.getAsJsonArray("bones");
        for (JsonElement element : new ArrayList<>(bones.asList())) {
            JsonObject boneObj = element.getAsJsonObject();

            if (!boneObj.has("cubes")) continue; // Skip bones without cubes

            String boneName = UniversalAnimLoader.getCorrectPlayerBoneName(boneObj.get("name").getAsString());
            if (BendingGeometry.BENDABLE_BONES.contains(boneName)) addBoneBendsToBone(bones, boneObj);
        }
        geometry.add("bones", bones);
    }

    private static void addBoneBendsToBone(JsonArray bones, JsonObject bone) {
        int boneSize = bone.getAsJsonArray("cubes").get(0).getAsJsonObject()
                .getAsJsonArray("size").get(1).getAsInt();

        JsonObject secondBone = makeCubeBendable(bone);
        String name = bone.get("name").getAsString();
        for (JsonElement element : new ArrayList<>(bones.asList())) { // Fix hierarchy
            JsonObject boneObj = element.getAsJsonObject();

            if (boneObj.has("parent") && name.equals(boneObj.get("parent").getAsString())) {
                JsonObject firstCube = boneObj.has("cubes") ? boneObj.getAsJsonArray("cubes").get(0).getAsJsonObject() : new JsonObject();

                if (firstCube.has("inflate") && boneSize == firstCube.getAsJsonArray("size").get(1).getAsInt()) {
                    CommonData.LOGGER.info("Second layer detected! {}", boneObj);

                    JsonObject secondBoneSecondLayer = makeCubeBendable(boneObj);
                    boneObj.add("parent", bone.get("name"));
                    secondBoneSecondLayer.add("parent", secondBone.get("name"));
                    bones.add(secondBoneSecondLayer);
                } else {
                    boneObj.add("parent", secondBone.get("name"));
                }
            }
        }
        bones.add(secondBone);
    }

    /**
     * Patches the bone and adds a second one
     * @param bone Mutable bone
     * @return Second bending bone
     */
    private static JsonObject makeCubeBendable(JsonObject bone) {
        JsonObject bendableCube = bone.getAsJsonArray("cubes").get(0).getAsJsonObject();
        { // Patch size
            JsonArray size = bendableCube.getAsJsonArray("size");
            size.set(1, new JsonPrimitive(size.get(1).getAsFloat() / 2F));
        }

        JsonObject secondBendableCube = bendableCube.deepCopy();
        float secondBendableCubeSizeY = secondBendableCube.getAsJsonArray("size").get(1).getAsFloat();
        { // Patch second cube uv + pivot
            JsonArray uv = secondBendableCube.getAsJsonArray("uv");
            uv.set(1, new JsonPrimitive(uv.get(1).getAsFloat() + secondBendableCubeSizeY));
        }

        JsonArray secondCubes = new JsonArray();
        secondCubes.add(secondBendableCube);

        { // Patch first cube origin
            float sizeY = bendableCube.getAsJsonArray("size").get(1).getAsFloat();
            JsonArray origin = bendableCube.getAsJsonArray("origin");
            origin.set(1, new JsonPrimitive(origin.get(1).getAsFloat() + sizeY));
        }

        JsonObject secondBone = new JsonObject();
        secondBone.add("parent", bone.get("name"));
        secondBone.addProperty("name", UniversalAnimLoader.restorePlayerBoneName(bone.get("name").getAsString() + BEND_SUFFIX));
        secondBone.add("cubes", secondCubes);

        JsonArray pivot = new JsonArray();
        pivot.add(bone.get("pivot").getAsJsonArray().get(0));
        pivot.add(bone.get("pivot").getAsJsonArray().get(1).getAsFloat() - secondBendableCubeSizeY);
        pivot.add(bone.get("pivot").getAsJsonArray().get(2));
        secondBone.add("pivot", pivot);

        return secondBone;
    }
}
