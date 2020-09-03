package com.kosmx.emotecraft;

import com.kosmx.bendylib.MutableModelPart;
import net.minecraft.client.model.ModelPart;

public class BendableModelPart extends MutableModelPart {

    public BendableModelPart(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public String modId() {
        return Main.MOD_NAME;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
