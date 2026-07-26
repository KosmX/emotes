import bpy, os, math, json
from mathutils import *
from bpy_extras import anim_utils

PAL_TMP_SUFFIX = "_pal_export_tmp"

def _normalize(v):
    if v.length == 0.0:
        return Vector((0.0, 0.0))
    return v.normalized()


def _chord_length_parameterize(points):
    u = [0.0]
    total = 0.0
    for i in range(1, len(points)):
        total += (points[i] - points[i - 1]).length
        u.append(total)
    if total == 0.0:
        return [0.0 for _ in points]
    return [val / total for val in u]


def _bezier_point(p0, p1, p2, p3, t):
    mt = 1.0 - t
    return (
        p0 * (mt ** 3) +
        p1 * (3.0 * t * (mt ** 2)) +
        p2 * (3.0 * (t ** 2) * mt) +
        p3 * (t ** 3)
    )


def _generate_bezier(points, u):
    p0 = points[0]
    p3 = points[-1]

    t_hat1 = _normalize(points[1] - points[0])
    t_hat2 = _normalize(points[-2] - points[-1])

    C = [[0.0, 0.0], [0.0, 0.0]]
    X = [0.0, 0.0]

    for i in range(len(points)):
        ui = u[i]
        mt = 1.0 - ui

        b0 = mt ** 3
        b1 = 3.0 * ui * (mt ** 2)
        b2 = 3.0 * (ui ** 2) * mt
        b3 = ui ** 3

        a1 = t_hat1 * b1
        a2 = t_hat2 * b2

        C[0][0] += a1.dot(a1)
        C[0][1] += a1.dot(a2)
        C[1][0] += a1.dot(a2)
        C[1][1] += a2.dot(a2)

        tmp = points[i] - (p0 * (b0 + b1) + p3 * (b2 + b3))

        X[0] += a1.dot(tmp)
        X[1] += a2.dot(tmp)

    det = C[0][0] * C[1][1] - C[0][1] * C[1][0]

    seg_len = (p3 - p0).length
    if abs(det) > 1e-12:
        alpha1 = (X[0] * C[1][1] - X[1] * C[0][1]) / det
        alpha2 = (C[0][0] * X[1] - C[1][0] * X[0]) / det
    else:
        alpha1 = alpha2 = seg_len / 3.0

    epsilon = 1e-6 * seg_len
    if alpha1 < epsilon or alpha2 < epsilon:
        alpha1 = alpha2 = seg_len / 3.0

    p1 = p0 + t_hat1 * alpha1
    p2 = p3 + t_hat2 * alpha2

    return p0, p1, p2, p3


def _compute_max_error(points, bezier, u):
    max_dist = 0.0
    split = len(points) // 2
    for i in range(1, len(points) - 1):
        p = _bezier_point(*bezier, u[i])
        dist = (p - points[i]).length
        if dist > max_dist:
            max_dist = dist
            split = i
    return max_dist, split


def _fit_curve(points, error):
    if len(points) == 2:
        p0 = points[0]
        p3 = points[1]
        delta = (p3 - p0) / 3.0
        return [(p0, p0 + delta, p3 - delta, p3)]

    u = _chord_length_parameterize(points)
    bezier = _generate_bezier(points, u)
    max_error, split = _compute_max_error(points, bezier, u)

    if max_error <= error:
        return [bezier]

    left = _fit_curve(points[:split + 1], error)
    right = _fit_curve(points[split:], error)
    return left + right


def baked_curve_to_bezier(obj, action_name=None, error_threshold=0.01):
    if obj.animation_data is None:
        raise ValueError("Object has no animation data")

    action = obj.animation_data.action
    if action_name:
        action = bpy.data.actions.get(action_name)

    if action is None:
        raise ValueError("Action not found")

    slot = obj.animation_data.action_slot
    channelbag = anim_utils.action_get_channelbag_for_slot(action, slot)

    for fcurve in channelbag.fcurves:
        raw = [(kp.co.x, kp.co.y) for kp in fcurve.keyframe_points]
        if len(raw) < 2:
            continue

        points = [Vector((x, y)) for x, y in raw]
        segments = _fit_curve(points, error_threshold)

        fcurve.keyframe_points.clear()

        first = True
        for p0, p1, p2, p3 in segments:
            if first:
                kp0 = fcurve.keyframe_points.insert(frame=p0.x, value=p0.y)
                kp0.interpolation = 'BEZIER'
                kp0.type = 'GENERATED'
                kp0.handle_right_type = 'FREE'
                kp0.handle_right = (p1.x, p1.y)
                first = False
            else:
                prev = fcurve.keyframe_points[-1]
                prev.handle_right_type = 'FREE'
                prev.handle_right = (p1.x, p1.y)

            kp1 = fcurve.keyframe_points.insert(frame=p3.x, value=p3.y)
            kp1.interpolation = 'BEZIER'
            kp1.handle_left_type = 'FREE'
            kp1.handle_left = (p2.x, p2.y)
            kp1.type = 'GENERATED'

        fcurve.update()

def _insert_segment(base_fcurve, baked_fcurve, start, end):
    for bk in baked_fcurve.keyframe_points:
        if start <= bk.co.x <= end:
            k = base_fcurve.keyframe_points.insert(
                frame=bk.co.x,
                value=bk.co.y,
                options={'FAST'}
            )
            k.interpolation = 'BEZIER'
            k.type = 'GENERATED'

def blender_type(type):
    mapping = {"location": "position",
        "rotation_euler": "rotation",
        "scale": "scale",
        "bend": "bend"
        }
    return mapping[type]

def merge_fcurves(
    base_fcurve,
    baked_fcurve,
    threshold=1e-3
):
    if base_fcurve is None or baked_fcurve is None:
        return
    modifiers_state = [m.mute for m in base_fcurve.modifiers]
    for m in base_fcurve.modifiers:
        m.mute = True

    try:
        base_keys = {k.co.x: k for k in base_fcurve.keyframe_points}
        baked_keys = baked_fcurve.keyframe_points

        in_segment = False
        segment_start = None

        for i, bk in enumerate(baked_keys):
            frame = bk.co.x
            baked_val = bk.co.y

            base_val = base_fcurve.evaluate(frame)

            diff = abs(baked_val - base_val) > threshold

            if diff and not in_segment:
                in_segment = True
                segment_start = frame

            if not diff and in_segment:
                in_segment = False
                segment_end = baked_keys[i - 1].co.x
                _insert_segment(base_fcurve, baked_fcurve, segment_start, segment_end)
        if in_segment:
            segment_end = baked_keys[-1].co.x
            _insert_segment(base_fcurve, baked_fcurve, segment_start, segment_end)

        base_fcurve.update()

    finally:
        for m, mute in zip(base_fcurve.modifiers, modifiers_state):
            m.mute = mute


def collect_animation_data(rig_object, export_bones):
    scene = bpy.context.scene
    
    print("Collecting animation data...")
    bpy.ops.object.mode_set(mode='OBJECT')
    original_action = rig_object.animation_data.action
    original_slot = rig_object.animation_data.action_slot
    baking_error_threshold = original_action.emote.baking_error_threshold
    
    export_frame_start = 0
    export_frame_end = int(scene.frame_end)
    if original_action.use_frame_range:
        export_frame_start = int(original_action.frame_start)
        export_frame_end = int(original_action.frame_end)

    for stale in list(bpy.data.actions):
        if PAL_TMP_SUFFIX in stale.name and stale.users == 0:
            bpy.data.actions.remove(stale)
            
    work_action = original_action.copy()
    work_action.name = original_action.name + PAL_TMP_SUFFIX
    try:
        rig_object.animation_data.action = work_action
        work_slot = None
        if original_slot is not None:
            for slot in work_action.slots:
                if slot.identifier == original_slot.identifier:
                    work_slot = slot
                    break
        if work_slot is None and rig_object.animation_data.action_suitable_slots:
            work_slot = rig_object.animation_data.action_suitable_slots[0]
        if work_slot is None:
            raise RuntimeError(f'No suitable slot on the copied action "{work_action.name}"')
        rig_object.animation_data.action_slot = work_slot


        bpy.ops.object.select_all(action='DESELECT')
        rig_object.select_set(True)
    #    for bone in rig_object.pose.bones:
    #        for c in ["location", "rotation_euler", "scale"]:
    #            bone.keyframe_insert(c, frame=0)
        for bone in ["left_arm", "right_arm", "left_leg", "right_leg"]:
            export_bones.append(bone+"_vanilla")
        for bone in ["left_arm", "right_arm", "left_leg", "right_leg", "torso", "cape"]:
            export_bones.append(bone+"_bend")
        
        for bone in rig_object.pose.bones:
            if bone.name in export_bones: bone.select = True
        bpy.ops.object.mode_set(mode='POSE')
        bpy.ops.nla.bake(
                only_selected=True,
                frame_start=export_frame_start,
                frame_end= export_frame_end+1,
                step=1,
                visual_keying=True,
                use_current_action=False,
                bake_types={'POSE'},
                channel_types={'LOCATION', 'ROTATION', 'SCALE', 'PROPS'}
        )
        bpy.ops.object.mode_set(mode='OBJECT')
        baked_action = rig_object.animation_data.action

        baked_slot = rig_object.animation_data.action_slot

        baked_channelbag = anim_utils.action_get_channelbag_for_slot(baked_action, baked_slot)

        fcurves = baked_channelbag.fcurves

        baked_animation_data = {}  
        animation_data = {} 

        baked_curve_to_bezier(rig_object, baked_action.name, error_threshold=baking_error_threshold)
        for bone in export_bones:
            if bone not in [b.name for b in rig_object.pose.bones]:
                print(f'You have selected for export a bone that doesn\'t exist:"{bone}"')
                continue
            baked_animation_data[bone] = {
                "position": [],
                "rotation": [],
                "scale": []
            }
            for type in "location", "rotation_euler", "scale":
                    baked_animation_data[bone][blender_type(type)]= [fcurves.find(data_path = f'pose.bones["{bone}"].{type}', index = axis) for axis in [0,1,2]]

        rig_object.animation_data.action = work_action
        rig_object.animation_data.action_slot = work_slot

        channelbag = anim_utils.action_get_channelbag_for_slot(rig_object.animation_data.action, rig_object.animation_data.action_slot)

        fcurves = channelbag.fcurves

        for bone in export_bones:
            if bone not in [b.name for b in rig_object.pose.bones]:
                continue
            animation_data[bone] = {
                "position": [],
                "rotation": [],
                "scale": []
            }        
            for type in "location", "rotation_euler", "scale":
                animation_data[bone][blender_type(type)] = [fcurves.find(data_path = f'pose.bones["{bone}"].{type}', index = axis) for axis in [0,1,2]] 

        for bone in export_bones:
            if bone not in [b.name for b in rig_object.pose.bones]:
                continue
        
            for mode in animation_data[bone]:
                for channel, curve in enumerate(animation_data[bone][mode]):
                    merge_fcurves(curve, baked_animation_data[bone][mode][channel])

        bpy.data.actions.remove(baked_action)
        return animation_data, work_action
    finally:
        rig_object.animation_data.action = original_action
        rig_object.animation_data.action_slot = original_slot
                            
                            
                
                
                
                
            

