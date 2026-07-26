import bpy
from bpy.types import Panel, PropertyGroup
from bpy.props import (
    StringProperty,
    FloatProperty,
    IntProperty,
    BoolProperty,
    PointerProperty,
    CollectionProperty,
    FloatVectorProperty,
)
from bl_ui.space_dopesheet import DOPESHEET_PT_action
from pathlib import Path

class ActionBadge(PropertyGroup):
    text: StringProperty(
        name="",
        default="Badge",
    )

    color: FloatVectorProperty(
        name="",
        subtype='COLOR',
        size=3,
        min=0.0,
        max=1.0,
        default=(1.0, 1.0, 1.0),
    )
class PivotBone(PropertyGroup):
    name: StringProperty(
        name="",
        description="Custom pivot bone name, the same as the name of the bone you created"
    )

class ActionMetadata(PropertyGroup):
    
    hold_on_last_frame: BoolProperty(
        name="Hold on last frame",
        default=False,
        description = "Keep the animation on the last frame after it was played"
    )
    
    name: StringProperty(
        name="Name",
        default="Name"
    )

    description: StringProperty(
        name="Description",
        default="Description"
    )

    author: StringProperty(
        name="Author",
        default="Author"
    )
    
    badges: CollectionProperty(
        type=ActionBadge
    )
    
    pivot_bones: CollectionProperty(
        type=PivotBone
    )

    baking_error_threshold: FloatProperty(
        name="Baking Error Threshold",
        default=0.001,
        precision=3,
        description="How much error is fine when converting baked animation to bezier keyframes\n0: just keep it fully baked\n>0: curve is allowed to be off by this much"
    )

    value_precision: IntProperty(
        name="Value Precision",
        default=3,
        min=0,
        max=8,
        description="How many digits after the dot to keep in values"
    )
    emote_save_path: StringProperty(
        name="",
        default=str(Path(bpy.data.filepath).parent),
        subtype='DIR_PATH',
        description="Directory to save the emote"
    )


# ------------------------------------------------------------
# UI
# ------------------------------------------------------------

def draw_emote_settings(self, context):
    layout = self.layout

    action = context.object.animation_data.action
    data = action.emote
    
    if action.use_cyclic:
        layout.prop(data, "hold_on_last_frame")
        layout.separator()

    layout.prop(data, "name")
    layout.prop(data, "description")
    layout.prop(data, "author")
    
    box = layout.box()
    row = box.row()
    
    row.label(text="Badges")
    row.operator("action.add_badge", text="", icon='ADD')

    for i, badge in enumerate(data.badges):
        
        badge_box = box.box()
        row = badge_box.row()
        row.prop(badge, "text")
        row.prop(badge, "color")
        op = row.operator("action.remove_badge", text="", icon='X')
        op.index = i
    
    layout.separator()
    
    layout.label(text="Export")
    
    box = layout.box()
    row = box.row()
    
    row.label(text="Pivot Bones")
    row.operator("action.add_pivot_bone", text="", icon='ADD')

    for i, pivot_bone in enumerate(data.pivot_bones):
        
        pivot_bone_box = box.box()
        row = pivot_bone_box.row()
        row.prop(pivot_bone, "name")
        op = row.operator("action.remove_pivot_bone", text="", icon='X')
        op.index = i
    
    layout.prop(data, "baking_error_threshold")
    layout.prop(data, "value_precision")
    
    layout.separator()
    
    layout.prop(data, "emote_save_path")
    layout.operator("action.export_animation", icon='EXPORT')

class ACTION_OT_add_badge(bpy.types.Operator):
    bl_idname = "action.add_badge"
    bl_label = "Add Badge"

    def execute(self, context):
        action = context.object.animation_data.action
        badge = action.emote.badges.add()

        badge.text = ""
        badge.color = (1.0, 1.0, 1.0)

        return {'FINISHED'}
    
class ACTION_OT_remove_badge(bpy.types.Operator):
    bl_idname = "action.remove_badge"
    bl_label = "Remove Badge"

    index: IntProperty()

    def execute(self, context):
        action = context.object.animation_data.action
        action.emote.badges.remove(self.index)
        return {'FINISHED'}

class ACTION_OT_export(bpy.types.Operator):
    bl_idname = "action.export_animation"
    bl_label = "Export"
    bl_description = "Exports the current action as an emote"

    def execute(self, context):
        text = bpy.data.texts.get("export.py")

        if text is None:
            self.report({'ERROR'}, "Text 'export.py' not found")
            return {'CANCELLED'}

        override = context.copy()
        override["edit_text"] = text

        with context.temp_override(**override):
            bpy.ops.text.run_script()

        return {'FINISHED'}
class ACTION_OT_add_pivot_bone(bpy.types.Operator):
    bl_idname = "action.add_pivot_bone"
    bl_label = "Add Pivot Bone"

    def execute(self, context):
        action = context.object.animation_data.action
        pivot_bone = action.emote.pivot_bones.add()

        pivot_bone.bone_name = ""

        return {'FINISHED'}
    
class ACTION_OT_remove_pivot_bone(bpy.types.Operator):
    bl_idname = "action.remove_pivot_bone"
    bl_label = "Remove Pivot Bone"

    index: IntProperty()

    def execute(self, context):
        action = context.object.animation_data.action
        action.emote.pivot_bones.remove(self.index)
        return {'FINISHED'}
# ------------------------------------------------------------
# Registration
# ------------------------------------------------------------

classes = (
    PivotBone,
    ActionBadge,
    ActionMetadata,
    ACTION_OT_export,
#    DOPESHEET_PT_emote,
    ACTION_OT_add_badge,
    ACTION_OT_remove_badge,
    ACTION_OT_add_pivot_bone,
    ACTION_OT_remove_pivot_bone,
)


def register():
    for cls in classes:
        bpy.utils.register_class(cls)

    bpy.types.Action.emote = PointerProperty(
        type=ActionMetadata
    )
    DOPESHEET_PT_action.append(draw_emote_settings)


def unregister():
    DOPESHEET_PT_action.remove(draw_emote_settings)
    del bpy.types.Action.emote

    for cls in reversed(classes):
        bpy.utils.unregister_class(cls)


if __name__ == "__main__":
    register()