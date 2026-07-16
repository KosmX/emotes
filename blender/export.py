import sys, bpy, json
from pathlib import Path
project_dir = Path(bpy.data.filepath).parent
rig_object = bpy.data.objects["export_armature"]
action = rig_object.animation_data.action
scene = bpy.context.scene

emote_save_folder = project_dir
blender_save_folder = project_dir

prefix = ""
filename = prefix + action.name

name = f"{filename}"
description = ""
author = "3APA3EH"

isLoop = action.use_cyclic
speed = 1.0 # make the animation {speed} times faster
baking_error_threshold = 0.01 # how much error is fine when converting baked animation to bezier keyframes
                              # 0: just make every keyframe bezier; >0: curve is allowed to be off by this much
                              # even the default 0.01 greatly reduces the amount of keyframes

#from what bones to export read the animation
export_bones = [
            "body",
            "body_control",
            "head",
            "left_arm",
            "left_leg",
            "right_arm",
            "right_leg",
            "torso",
            "left_arm_bend",
            "left_leg_bend",
            "right_arm_bend",
            "right_leg_bend",
            "torso_bend",
            "right_item",
            "left_item",
            "cape",
            "cape_bend",
            "waist"
            ]

# https://misode.github.io/text-component/
badges = [
#  {
#    "translate": "mineemotes.emote.badge.dance",
#    "fallback": "Dance",
#    "color": "#E73A3A"
#  },
#  {
#    "translate": "mineemotes.emote.badge.test",
#    "fallback": "Test",
#    "color": "#003A3A"
#  }
#    {
#    "translate": "mineemotes.emote.badge.bendless",
#    "fallback": "Bendless",
#    "color": "#34c415"
#  }
]
#end of the settings

# how many decimal places to keep in values
value_precision = 3

framerate = bpy.data.scenes["Scene"].render.fps/bpy.data.scenes["Scene"].render.fps_base



bpy.ops.wm.save_mainfile(filepath=f"{blender_save_folder}\\{filename}.blend")


collect_animation_data = bpy.data.texts['collect_animation_data.py'].as_module().collect_animation_data
create_emote = bpy.data.texts['set_up_bedrock.py'].as_module().create_emote

print(f"Exporting {filename}.json!")
is_vanilla = bpy.data.objects["export_armature"].pose.bones["settings"]["vanilla"]

preview_frame = scene.frame_current
scene.frame_set(0)

animation_data, work_action = collect_animation_data(baking_error_threshold,
                       isLoop, 
                       int(action.frame_start),
                       int(action.frame_end),
                       export_bones
                       )

emote = create_emote(filename,
                     scene.frame_start,
                     int(action.frame_start),
                     int(action.frame_end),
                     speed,
                     isLoop,
                     name, author, description, badges,
                     export_bones,
                     animation_data,
                     value_precision
                     )

bpy.data.actions.remove(work_action)

print("Saving json...")
with open(str(emote_save_folder / (prefix + filename + ".json")), 'w', encoding="utf-8") as e:
    json.dump(emote, e, ensure_ascii=False, indent=4)
scene.frame_set(preview_frame)
print("Rendering icon...")
scene.render.filepath = str(emote_save_folder / (prefix + filename + ".png"))
bpy.ops.render.render(write_still = 1)


print("Emote has been exported successfuly!")