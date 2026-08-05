import sys, bpy, json
from pathlib import Path
import base64
import tempfile
import os
rig_object = bpy.context.active_object
action = rig_object.animation_data.action
scene = bpy.context.scene

#from what bones to read the animation
export_bones = [
            "body", "body_control",
            "head",
            "left_arm", "right_arm",
            "left_leg", "right_leg",
            "waist", "torso", "cape",
            "left_item", "right_item",
            ]
for pivot_bone in action.emote.pivot_bones:
    export_bones.append(pivot_bone.name)


collect_animation_data = bpy.data.texts['collect_animation_data.py'].as_module().collect_animation_data
create_emote = bpy.data.texts['set_up_bedrock.py'].as_module().create_emote

print(f"Exporting {action.name}.json!")
preview_frame = scene.frame_current
scene.frame_set(0)

animation_data, work_action = collect_animation_data(rig_object, export_bones)
emote = create_emote(rig_object, export_bones, animation_data)

bpy.data.actions.remove(work_action)

emote_save_folder = action.emote.emote_save_path

print("Rendering icon...")
scene.frame_set(preview_frame)
bpy.ops.render.render()


image = bpy.data.images["Render Result"]

with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp:
    path = tmp.name

try:
    image.save_render(path)

    with open(path, "rb") as f:
        image_base64 = base64.b64encode(f.read()).decode("ascii")
finally:
    os.remove(path)

emote["animations"][action.name]["player_animation_library"]["iconData"] = image_base64 

print("Saving json...")
with open(str(emote_save_folder + "/" + action.name + ".json"), 'w', encoding="utf-8") as e:
    json.dump(emote, e, ensure_ascii=False, indent=4)


print("Emote has been exported successfuly!")