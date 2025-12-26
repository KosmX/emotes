package org.redlance.dima_dencep.mods.emotecraft.geyser.animator;

@SuppressWarnings("unused") // api
public final class PackedProperty {
    public static final int ID_MIN = -99;
    public static final int ID_MAX =  99;
    public static final int ID_COUNT = ID_MAX - ID_MIN + 1; // 199

    public static final int PROP_MIN = -1_000_000;
    public static final int PROP_RANGE = 2_000_001; // [-1_000_000 .. +1_000_000]
    public static final int SLOT = PROP_RANGE / ID_COUNT;   // 10050
    public static final int CENTER = SLOT / 2;              // 5025

    // 10 => 0.1, 100 => 0.01, 1 => 1.0
    public static final int SCALE = 10;

    public static final float VALUE_MIN = -CENTER / (float) SCALE;
    public static final float VALUE_MAX = (SLOT - 1 - CENTER) / (float) SCALE;

    public static final int PROP_MAX_USED = PROP_MIN + (SLOT * ID_COUNT) - 1;

    public static int pack(int id, float value) {
        id = Math.max(ID_MIN, Math.min(ID_MAX, id));
        value = Math.max(VALUE_MIN, Math.min(VALUE_MAX, value));
        int idx = id - ID_MIN; // 0..198
        int quant = Math.round(value * SCALE);
        int valueIndex = quant + CENTER;
        if (valueIndex < 0) valueIndex = 0;
        if (valueIndex > SLOT - 1) valueIndex = SLOT - 1;
        int raw = idx * SLOT + valueIndex;
        return PROP_MIN + raw;
    }

    public static int unpackId(int packed) {
        int raw = packed - PROP_MIN;
        int idx = raw / SLOT;
        return idx + ID_MIN;
    }

    public static float unpackValue(int packed) {
        int raw = packed - PROP_MIN;
        int valueIndex = raw % SLOT;
        int quant = valueIndex - CENTER;
        return quant / (float) SCALE;
    }
}

