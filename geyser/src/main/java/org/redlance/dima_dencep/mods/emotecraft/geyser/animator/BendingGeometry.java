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

        JsonObject secondBone = makeCubeBendable(bone, true);
        String name = bone.get("name").getAsString();
        for (JsonElement element : new ArrayList<>(bones.asList())) { // Fix hierarchy
            JsonObject boneObj = element.getAsJsonObject();

            if (boneObj.has("parent") && name.equals(boneObj.get("parent").getAsString())) {
                JsonObject firstCube = boneObj.has("cubes") ? boneObj.getAsJsonArray("cubes").get(0).getAsJsonObject() : new JsonObject();

                if (firstCube.has("inflate") && boneSize == firstCube.getAsJsonArray("size").get(1).getAsInt()) {
                    CommonData.LOGGER.info("Second layer detected! {}", boneObj);

                    JsonObject secondBoneSecondLayer = makeCubeBendable(boneObj, false);
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
    private static JsonObject makeCubeBendable(JsonObject bone, boolean drawCrossSection) {
        JsonObject bendableCube = bone.getAsJsonArray("cubes").get(0).getAsJsonObject();
        if (bendableCube.get("uv").isJsonArray()) bendableCube.add("uv", expandCubeUV(bendableCube));

        { // Patch size
            JsonArray size = bendableCube.getAsJsonArray("size");
            size.set(1, new JsonPrimitive(size.get(1).getAsFloat() / 2F));
        }

        JsonObject secondBendableCube = bendableCube.deepCopy();
        float secondBendableCubeSizeY = secondBendableCube.getAsJsonArray("size").get(1).getAsFloat();
        { // Patch second cube uv + pivot
            JsonObject uv = secondBendableCube.getAsJsonObject("uv");
            String[] sides = {"north", "south", "east", "west"};

            for (String side : sides) {
                JsonObject face = uv.getAsJsonObject(side);

                JsonArray uvCoords = face.getAsJsonArray("uv");
                uvCoords.set(1, new JsonPrimitive(uvCoords.get(1).getAsFloat() + secondBendableCubeSizeY));

                JsonArray uvSize = face.getAsJsonArray("uv_size");
                uvSize.set(1, new JsonPrimitive(secondBendableCubeSizeY));
            }

            if (!drawCrossSection) uv.remove("up");
        }

        { // Patch first cube uv
            JsonObject uv = bendableCube.getAsJsonObject("uv");
            String[] sides = {"north", "south", "east", "west"};

            for (String side : sides) {
                JsonObject face = uv.getAsJsonObject(side);
                JsonArray uvSize = face.getAsJsonArray("uv_size");
                uvSize.set(1, new JsonPrimitive(secondBendableCubeSizeY));
            }

            if (drawCrossSection) {
                uv.add("down", uv.getAsJsonObject("up").deepCopy());
            } else {
                uv.remove("down");
            }
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

    public static JsonObject expandCubeUV(JsonObject cube) {
        JsonArray uvArray = cube.getAsJsonArray("uv");
        float u = uvArray.get(0).getAsFloat();
        float v = uvArray.get(1).getAsFloat();

        JsonArray sizeArray = cube.getAsJsonArray("size");
        float w = sizeArray.get(0).getAsFloat();
        float h = sizeArray.get(1).getAsFloat();
        float d = sizeArray.get(2).getAsFloat();

        JsonObject root = new JsonObject();
        root.add("east", createFace(u, v + d, d, h));
        root.add("north", createFace(u + d, v + d, w, h));
        root.add("west", createFace(u + d + w, v + d, d, h));
        root.add("south", createFace(u + d + w + d, v + d, w, h));
        root.add("up", createFace(u + d, v, w, d));
        root.add("down", createFace(u + d + w, v, w, d));

        return root;
    }

    private static JsonObject createFace(float u, float v, float w, float h) {
        JsonObject face = new JsonObject();
        JsonArray uv = new JsonArray();
        uv.add(u); uv.add(v);
        face.add("uv", uv);
        JsonArray size = new JsonArray();
        size.add(w); size.add(h);
        face.add("uv_size", size);
        return face;
    }
}
