package com.kosmx.emotecraft.mixinInterface;

import com.kosmx.emotecraft.BendableModelPart;

public interface IMutatedBipedModel {
    BendableModelPart getTorso();

    BendableModelPart getRightArm();

    BendableModelPart getLeftArm();

    BendableModelPart getRightLeg();

    BendableModelPart getLeftLeg();

    void setTorso(BendableModelPart part);

    void setRightArm(BendableModelPart part);

    void setLeftArm(BendableModelPart part);

    void setRightLeg(BendableModelPart part);

    void setLeftLeg(BendableModelPart part);

    void setEmoteSupplier(BendableModelPart.EmoteSupplier emoteSupplier);

    BendableModelPart.EmoteSupplier getEmoteSupplier();
}
