package com.kosmx.bendylib;

import com.kosmx.emotecraft.Main;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.InputUtil;
import org.apache.logging.log4j.Level;
import org.lwjgl.glfw.GLFW;

public class TestClass {
    static KeyBinding testFeature = new KeyBinding("testfeature", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F4, "category.emotecraft.keybinding");

    public static void init(){
        KeyBindingHelper.registerKeyBinding(testFeature);
    }


    public static void featureTest(PlayerEntityModel model){
        if(testFeature.wasPressed()){
            //((IModelBend)model.rightArm).test();
            
            Main.log(Level.INFO, "testing");
        }
    }
}
