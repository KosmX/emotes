import sys, bpy, json
from pathlib import Path
project_dir = Path(bpy.data.filepath).parent
rig_object = bpy.data.objects["export_armature"]
action = rig_object.animation_data.action
scene = bpy.context.scene

emote_save_folder = action.emote.emote_save_path

filename = action.name

name = action.emote.name
description = action.emote.description
author = action.emote.author

isLoop = action.use_cyclic
if action.use_cyclic and action.emote.hold_on_last_frame:
    isLoop = "hold_on_last_frame"
baking_error_threshold = action.emote.baking_error_threshold

#from what bones to read the animation
export_bones = [
            "body",
            "body_control",
            "head",
            "left_arm",
            "left_leg",
            "right_arm",
            "right_leg",
            "torso",
            "right_item",
            "left_item",
            "cape",
            "waist"
            ]
for pivot_bone in action.emote.pivot_bones:
    export_bones.append(pivot_bone.name)

def rgb_to_hex(color):
    r = round(color[0] * 255)
    g = round(color[1] * 255)
    b = round(color[2] * 255)
    return f"#{r:02X}{g:02X}{b:02X}"

# https://misode.github.io/text-component/
badges = []
for badge in action.emote.badges:
    badges.append({
        "text": badge.text,
        "color": rgb_to_hex(badge.color)
    })

# how many decimal places to keep in values
value_precision = action.emote.value_precision
#========================================
# User settings end here
#========================================

framerate = scene.render.fps/scene.render.fps_base


collect_animation_data = bpy.data.texts['collect_animation_data.py'].as_module().collect_animation_data
create_emote = bpy.data.texts['set_up_bedrock.py'].as_module().create_emote

print(f"Exporting {filename}.json!")
is_vanilla = rig_object.pose.bones["settings"]["vanilla"]

preview_frame = scene.frame_current
scene.frame_set(0)
export_frame_start = 0
export_frame_end = int(scene.frame_end)
if action.use_frame_range:
    export_frame_start = int(action.frame_start)
    export_frame_end = int(action.frame_end)

animation_data, work_action = collect_animation_data(baking_error_threshold,
                       isLoop, 
                       export_frame_start,
                       export_frame_end,
                       export_bones
                       )

emote = create_emote(filename,
                     scene.frame_start,
                     export_frame_start,
                     export_frame_end,
                     isLoop,
                     name, author, description, badges,
                     export_bones,
                     animation_data,
                     value_precision
                     )

bpy.data.actions.remove(work_action)

print("Saving json...")
with open(str(emote_save_folder + "/" + filename + ".json"), 'w', encoding="utf-8") as e:
    json.dump(emote, e, ensure_ascii=False, indent=4)

scene.frame_set(preview_frame)
print("Rendering icon...")
scene.render.filepath = emote_save_folder + "/" + filename + ".png"
bpy.ops.render.render(write_still = 1)


print("Emote has been exported successfuly!")