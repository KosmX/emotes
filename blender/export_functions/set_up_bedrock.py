import bpy, os, math, json
from mathutils import *

def get_bone_axis_difference(arm: bpy.types.Object, bone_name: str, mode: str):
    # get the difference between bone's axes and the blockbench axes
    
    is_vanilla = bpy.data.objects["export_armature"].pose.bones["settings"]["vanilla"]
    prev_mode = arm.mode
    bpy.ops.object.mode_set(mode='EDIT')
    edit_bone = arm.data.edit_bones[bone_name + "_bend"*(mode == 'bend') + "_vanilla"*(is_vanilla and mode !="bend" and bone_name in ["left_arm", "right_arm", "left_leg", "right_leg"])]
    bone_axes = [
        edit_bone.x_axis, 
        edit_bone.y_axis, 
        edit_bone.z_axis
    ]
    blockbench_axes = [
            Vector((1, 0, 0)),
            Vector((0, 0, 1)),
            Vector((0, 1, 0))
            ]
    if mode == "rotation":
        blockbench_axes = [
            Vector((1, 0, 0)),
            Vector((0, 0, -1)),
            Vector((0, 1, 0))
            ]
    result = [(0,0), (0,0), (0,0)]
    for boi in 0, 1, 2:
        best_axis = None
        best_dot = 0
        for bli in 0, 1, 2:
            dot = bone_axes[boi].dot(blockbench_axes[bli])
            if abs(dot) > abs(best_dot):
                best_dot = dot
                best_axis = bli
        result[boi] = (best_axis, best_dot)
        if mode == "scale":
            result[boi] = (best_axis, abs(best_dot))
    bpy.ops.object.mode_set(mode=prev_mode)
    return result


def fcurves_to_mode_dict(fcurves: list[bpy.types.Curve], speed, is_bend: bool=False):
    def build_frame_map(fcurve: bpy.types.Curve):
        if fcurve == None:
            return {}
        return {round(kf.co.x, 6): kf for kf in fcurve.keyframe_points}
    framerate = bpy.data.scenes["Scene"].render.fps/bpy.data.scenes["Scene"].render.fps_base
    
    maps = [build_frame_map(fc) for fc in fcurves]
    all_frames = set()
    for m in maps:
        all_frames |= set(m.keys())
    result = {}
    for frame in sorted(all_frames):
        time = round(frame / speed / framerate, 3)
        if not is_bend:
            result[time] = {
                "vector": [
                    maps[0].get(frame, "pal.disabled"),
                    maps[1].get(frame, "pal.disabled"),
                    maps[2].get(frame, "pal.disabled"),
                ]
            }
        else:
            result[time] = {
                "vector": [
                    maps[0].get(frame, "pal.disabled"), 
                    "pal.disabled", 
                    "pal.disabled"
                ]
            }
    return result


def get_bezier_args(keyframe, mode, multiplier, speed):
    framerate = bpy.data.scenes["Scene"].render.fps/bpy.data.scenes["Scene"].render.fps_base
    
    handle_left_y = (keyframe.handle_left.y - keyframe.co.y)*multiplier
    handle_left_x = (keyframe.handle_left.x - keyframe.co.x)/framerate/speed
    handle_right_y = (keyframe.handle_right.y - keyframe.co.y)*multiplier
    handle_right_x = (keyframe.handle_right.x - keyframe.co.x)/framerate/speed
    
    if mode == "position":
        handle_left_y *= 4
        handle_right_y *= 4
    if mode == "rotation":
        handle_left_y = math.degrees(handle_left_y)
        handle_right_y = math.degrees(handle_right_y)
    
    return list(map(lambda x: round(x, 6), [handle_left_y, handle_left_x, handle_right_y, handle_right_x]))


def get_easingArgs(keyframe, mode, multiplier, speed,value_precision, force_bezier=False):
    if keyframe == "pal.disabled":
        return None
    if force_bezier:
        return get_bezier_args(keyframe, mode, multiplier, speed)
    match keyframe.interpolation:
        case "BEZIER":
            return get_bezier_args(keyframe, mode, multiplier, speed)
        case "ELASTIC":
            return [round(keyframe.period/keyframe.amplitude, value_precision)] #not accurate, but good enough I guess
        case "BACK":
            return [round(keyframe.back * 0.602, value_precision)] # seems to work idk
        case "BOUNCE":
            return [0.4] #there isn't a setting for bounce in blender, this seems pretty accurate
        case _:
            return None


def get_bedrock_easing(keyframe):
    if keyframe == "pal.disabled":
        return None
    
    easing = keyframe.interpolation.lower()
    
    if easing in ['linear', 'bezier', 'constant']:
        return easing
    
    type = keyframe.easing.replace('_', '').lower()
    
    match type:
        case 'easein':
            if easing in ['back','elastic', 'bounce']:
                type = 'easeout'
            else:
                type = 'easein'
                
        case 'easeout':
            if easing in ['back','elastic', 'bounce']:
                type = 'easein'
            else:
                type = 'easeout'
                
        case 'easeinout':
            type = 'easeinout'
            
        case 'auto':
            if easing in ['bounce', 'elastic']:
                return "easein" + easing
            
            return "easeout" + easing
        
    return type + easing


def type_to_channel(type: str):
    axes_map = {
        "x": 0,
        "y": 1,
        "z": 2
    }
    return axes_map[type.lower()]


def type_to_channel_testing(type: str):
    axes_map = {
        "x": 0,
        "y": 2,
        "z": 1
    }
    return axes_map[type.lower()]


def get_easing_list(keyframes: list[bpy.types.Keyframe]):
    return [get_bedrock_easing(keyframe) for keyframe in keyframes]


def get_easingArgs_list(keyframes: list[bpy.types.Keyframe], mode: str, sign: float, speed, value_precision):
    return [get_easingArgs(keyframes[i], mode, round(sign[i]), speed, value_precision) for i in range(len(keyframes))]


def write_mode(bone_name: str, mode: str, animation_data, speed, rig_object, value_precision, default_bones, export_bones):
    is_vanilla = bpy.data.objects["export_armature"].pose.bones["settings"]["vanilla"]
    if mode == 'bend' and is_vanilla: return
    if mode == 'bend':
        if f"{bone_name}_bend" not in default_bones or f"{bone_name}_bend" not in export_bones: # bone isn't bendable or isn't selected for export
            return None
        
        mode_dict = fcurves_to_mode_dict(animation_data[f"{bone_name}_bend"]["rotation"], speed, is_bend=True)
    elif is_vanilla and bone_name in ["left_arm", "right_arm", "left_leg", "right_leg"]:
        if f"{bone_name}_bend" not in default_bones or f"{bone_name}_bend" not in export_bones: # bone isn't bendable or isn't selected for export
            return None
        
        mode_dict = fcurves_to_mode_dict(animation_data[f"{bone_name}_vanilla"][mode], speed)
    else:
        mode_dict = fcurves_to_mode_dict(animation_data[bone_name][mode], speed)
    
    if len(mode_dict) == 0:
        return None
    
    bone_axis_difference = get_bone_axis_difference(rig_object, bone_name, mode)
    bb_channel = [bone_axis_difference[c][0] for c in [0,1,2]]
    sign = [bone_axis_difference[c][1] for c in [0,1,2]]
    
    
    first_time = list(mode_dict.keys())[0]
    first_keyframes = mode_dict[first_time]["vector"]
    
    prev_easings = get_easing_list(first_keyframes)
    prev_easingArgss = get_easingArgs_list(first_keyframes, mode, sign, speed, value_precision)
    
    for time in mode_dict:
        
        keyframes = mode_dict[time]["vector"]
        
        current_easings = get_easing_list(keyframes)
        current_easingArgss = get_easingArgs_list(keyframes, mode, sign, speed,value_precision)
        
        vector = [0,0,0]
        for channel in 0,1,2:
            if keyframes[channel] == "pal.disabled":
                vector[bb_channel[channel]] = "pal.disabled"
            else:
                value = keyframes[channel].co.y
                if mode == "position":
                    value *= 4
                if mode in ["rotation", 'bend']:
                    value = math.degrees(value)
                vector[bb_channel[channel]] = round(value * sign[channel], value_precision)
        
        reordered_prev_easings = [prev_easings[bb_channel[c]] for c in [0,1,2]]
        reordered_prev_easingArgss = [prev_easingArgss[bb_channel[c]] for c in [0,1,2]]
        
        reordered_current_easings = [current_easings[bb_channel[c]] for c in [0,1,2]]
        reordered_current_easingArgss = [current_easingArgss[bb_channel[c]] for c in [0,1,2]]
        
        
        
        # save final keyframe
        if mode != 'bend':
            mode_dict[time] = {
                "vector": vector
            }
        else:
           mode_dict[time] = {
                "value": vector[0]
            } 
            
        final_easings = {}
        
        for axis in ['X', 'Y','Z']:
            channel = type_to_channel(axis)
            if vector[channel] == "pal.disabled":
                continue
            
            if mode == 'bend':
                axis = ''
            
            if reordered_current_easings[channel] == 'bezier' or reordered_prev_easings[channel] == 'bezier':
                final_easings[f"easing{axis}"] = 'bezier'
            else:
                final_easings[f"easing{axis}"] = reordered_prev_easings[channel]
            prev_easings[channel] = current_easings[channel]
            
            if reordered_prev_easingArgss[channel] != None:
                if reordered_current_easings[channel] == 'bezier' or reordered_prev_easings[channel] == 'bezier': 
                    final_easings[f"easingArgs{axis}"] = get_easingArgs(keyframes[bb_channel[channel]], mode, round(sign[channel]),speed,value_precision, force_bezier=True)
                else:
                    final_easings[f"easingArgs{axis}"] = reordered_prev_easingArgss[channel]
                    
            if final_easings[f"easing{axis}"] == 'bezier': 
                easingArgs = get_easingArgs(keyframes[bb_channel[channel]], mode, round(sign[channel]), speed,value_precision, force_bezier=True)
                final_easings[f"easingArgs{axis}"] = easingArgs
                prev_easingArgss[channel] = easingArgs   
            else:
                prev_easingArgss[channel] = current_easingArgss[channel]
        
        
        if final_easings.get('easingX', None) == final_easings.get('easingY', None) == final_easings.get('easingZ', None) != None:
            if final_easings.get('easingX', None) != 'linear':
                if final_easings.get('easingArgsX', None) == final_easings.get('easingArgsY', None) == final_easings.get('easingArgsZ', None) != None:
                    final_easings = {
                                    "easing": final_easings.get('easingX', None),
                                    "easingArgs": final_easings.get('easingArgsX', None)
                                    }
            else:
                final_easings = {"easing": "linear"}
                                
                
        mode_dict[time].update(final_easings)
            
        
    
    
    return mode_dict

 
def create_emote(filename, 
                 loop_return_frame,
                 export_frame_start,
                 export_frame_end,
                 speed,
                 isLoop,
                 name, author, description, badges,
                 export_bones,
                 animation_data,
                 value_precision
                 ):
    rig_object = bpy.data.objects["export_armature"]
    framerate = bpy.data.scenes["Scene"].render.fps/bpy.data.scenes["Scene"].render.fps_base
    
    emote = {
        "format_version": "1.8.0",
        "geckolib_format_version": 2,
        "model": {},
        "parents": {},
        "animations": {
            filename: {
                "loopTick": round(loop_return_frame/framerate/speed, 3),
                "loop": isLoop,
                "animation_length": round((export_frame_end-export_frame_start)/framerate/speed, 3),
                "player_animation_library": {
                    "name": name,
                    "author": author,
                    "description": description,
                    "bages": badges
    #                "applyBendToOtherBones": True
                },
                "bones":{}
            }
        }

    }
    
    print("Figuring out the custom bones...")
    default_bones = ["body","head","left_arm","left_leg","right_arm",
                    "right_leg","torso","left_arm_bend","left_leg_bend",
                    "right_arm_bend","right_leg_bend","torso_bend","right_item",
                    "left_item","cape","cape_bend"
                    ]
    bpy.ops.object.mode_set(mode='EDIT')
    # list the non-standard parents for all the bones
    for bone in rig_object.data.edit_bones:
        if bone.name not in export_bones or bone.name in default_bones or "_vanilla" in bone.name:
            continue
        pivot = bone.head
        #in blockbench compared to blender x is negated and y is swapped with z
        pivot = [-pivot[0]*4, pivot[2]*4, pivot[1]*4]
        emote["model"][bone.name] = {"pivot": pivot}
        
        for child in bone.children:
            if child.name in export_bones:
                if "_vanilla" in child.name: continue
                emote["parents"][child.name] = bone.name


    print("Fixing animation data for bedrock...")
    for bone_name in export_bones:
        if "_bend" in bone_name or "_vanilla" in bone_name: continue
        
        if bone_name not in [b.name for b in rig_object.pose.bones]:
            print(bone_name, "doesn't exist!")
            continue
        
        emote["animations"][filename]["bones"][bone_name] = {}
        for mode in ["position", "rotation", "bend", "scale"]:
            k = write_mode(bone_name, mode, animation_data, speed, rig_object,value_precision,default_bones, export_bones)
            if k is None: continue
            emote["animations"][filename]["bones"][bone_name][mode] = k
    bpy.ops.object.mode_set(mode='POSE')     
    return emote
    