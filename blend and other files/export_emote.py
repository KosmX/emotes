import bpy
import os

emoteName = "name"
emoteDescription = "description"
author = "Your name"

#You can set here the emote's parameters
#SAVE before edit! Blender can crash



#Looping: Don't use capitals
isLoop = "false"
returnTick = "2"


#DON'T EDIT THE SCRIPT BELOW
#unless you know, what are you doing











class static:
    movesText = ''
    first = True

#This is a debug function. is not used.
def Key_Frame_Points(): #Gets the key-frame values as an array.
    KEYFRAME_POINTS_ARRAY = []
    fcurves = bpy.context.active_object.animation_data.action.fcurves

    for curve in fcurves:
        keyframePoints = curve.keyframe_points
        for keyframe in keyframePoints:
            print('KEY FRAME POINTS ARE frame:{} value:{}'.format(keyframe.co[0],keyframe.co[1]))
            KEYFRAME_POINTS_ARRAY.append(keyframe.co[1])
            


def getPartData(name:str, endTick:list[int], isItem:bool = False):
    if str(type(bpy.data.objects[name].animation_data)) == "<class 'NoneType'>":
        return
    if str(type(bpy.data.objects[name].animation_data.action)) == "<class 'NoneType'>":
        return
    fcurves = bpy.data.objects[name].animation_data.action.fcurves
    for fcurve in fcurves:
        isLocation = fcurve.data_path == 'location'
        if (not isLocation and not fcurve.data_path == 'rotation_euler'):
            continue
        if isLocation:
            if fcurve.array_index == 0:
                typ = 'x'
            elif (fcurve.array_index == 1) != isItem:
                typ = 'z' # in blender, the up coordinate is Z. In MC this is Y
            else :
                typ = 'y'
        else:
            if fcurve.array_index == 0:
                typ = 'pitch'
            elif (fcurve.array_index == 1) != isItem:
                typ = 'roll' 
            else :
                typ = 'yaw'
        for keyframe in fcurve.keyframe_points:
            if int(keyframe.co[0]) == 0:
                continue
            if static.first:
                static.movesText = getTickData(name, typ, keyframe, isLocation, endTick, isItem)
                static.first = False
            else:
                static.movesText = '{},{}'.format(static.movesText, getTickData(name, typ, keyframe, isLocation, endTick, isItem))
    
    if name == "head" or name == "leftItem" or name == "rightItem":
        return #Don't read bend from the head
    
    bend_obj = name + "_bend"
    if str(type(bpy.data.objects[bend_obj].animation_data)) == "<class 'NoneType'>":
        return
    if str(type(bpy.data.objects[bend_obj].animation_data.action)) == "<class 'NoneType'>":
        return
    fcurves = bpy.data.objects[bend_obj].animation_data.action.fcurves
    for fcurve in fcurves:
        if fcurve.data_path == 'rotation_euler' and fcurve.array_index == 0:
            for keyframe in fcurve.keyframe_points:
                if int(keyframe.co[0]) == 0:
                    continue
                if static.first:
                    static.movesText = getTickData(name, "bend", keyframe, False, endTick)
                    static.first = False
                else:
                    static.movesText = '{},{}'.format(static.movesText, getTickData(name, "bend", keyframe, False, endTick))
                    
    
    
    
def getTickData(name:str, typ:str, keyframe, isL:bool, endTick:list[int], isItem:bool = False):
    turn = 0
    tick = int(keyframe.co[0])
    value = keyframe.co[1] #calculate correct
    if (tick > endTick[0]):
        endTick[0] = tick

    ## Find easing
    if(keyframe.easing == "AUTO"):
        easing = "EASEINOUT"
    else:
        easing = ''.join(keyframe.easing.split('_'))
    if(keyframe.interpolation == "BEZIER"):
        easing = easing + "QUAD"
    else:
        if(not (keyframe.interpolation == 'CONSTANT' or keyframe.interpolation == 'LINEAR')):
            easing = easing + str(keyframe.interpolation)
        else:
            easing = str(keyframe.interpolation)
       
    ## Location correction 
    #Head y correction
    if(name == 'head' and typ == 'y'):
        value -= 3
    
    if(isL):
        if(not name == 'torso'):
            value = value * 4
            if (not isItem):
                value = value * -1
        else:
            value = value * 0.25
            if(typ == 'z'):
                value = value * -1
    elif isItem:
        pass
    elif(not (name == 'torso' and not (typ == 'roll' or typ == 'bend'))): # rotation correction (*-1) except for torzo roll/bend
        value = value * -1
    
    if(typ == 'y'):
        if(name == "rightLeg" or name == "leftLeg"):
            value += 12
    if(typ == 'z' or typ == 'x'):
        if(name == "rightLeg"):
            value += 0.1
        elif(name == "leftLeg"):
            value -= 0.1
    
    if(name == 'rightArm' or name == 'leftArm'):
        if typ == 'y':
            value += 12
    
    text = '''
            {{
                "tick":{:d},
                "easing": "{}",
                "turn": {:d},
                "{}":{{
                    "{}":{}
                }}
            }}'''.format(tick, easing, turn, name, typ, value)
    return text

endTick = [0]

getPartData("head", endTick)
getPartData("torso", endTick)
getPartData("rightArm", endTick)
getPartData("leftArm", endTick)
getPartData("rightLeg", endTick)
getPartData("leftLeg", endTick)
getPartData("rightItem", endTick, True)
getPartData("leftItem", endTick, True)

emoteString = '''{{
    "name": "{}",
    "author": "{}",
    "description": "{}",
    "emote":{{
        "isLoop": "{}",
        "returnTick": {},
        "beginTick":{},
        "endTick":{},
        "stopTick":{},
        "degrees":false,
        "moves":[
            {}
        ]
    }}
}}'''.format(emoteName, author, emoteDescription, isLoop, returnTick, bpy.context.scene.frame_start, endTick[0], bpy.context.scene.frame_end + 1, static.movesText)

x = open(os.path.join(os.path.dirname(bpy.data.filepath), "emote.json"), "w")
x.write(emoteString)
x.close()
