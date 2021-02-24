package com.kosmx.emotes.fabric.executor.types;

import com.kosmx.emotes.executor.dataTypes.InputKey;
import com.kosmx.emotes.executor.dataTypes.Text;
import net.minecraft.client.util.InputUtil;

public class Key implements InputKey {

    final InputUtil.Key storedKey;

    public Key(InputUtil.Key key) {
        this.storedKey = key;
    }

    @Override
    public boolean equals(InputKey key) {
        return this.storedKey.equals(((Key)key).storedKey);
    }

    @Override
    public String getTranslationKey() {
        return storedKey.getTranslationKey();
    }

    @Override
    public Text getLocalizedText() {
        return new TextImpl(storedKey.getLocalizedText().copy());
    }
}
