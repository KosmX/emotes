package io.github.kosmx.playerAnim.layered;

import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.common.tools.Vec3f;
import io.github.kosmx.playerAnim.TransformType;

import java.util.ArrayList;
import java.util.List;

/**
 * Player animation stack, can contain multiple active or passive layers, will always be evaluated from the lowest index.
 * Highest index = it can override everything else
 */
public class AnimationStack implements IAnimation {

    private final List<Pair<Integer, IAnimation>> layers = new ArrayList<>();

    /**
     *
     * @return true if exists level what is active.
     */
    @Override
    public boolean isActive() {
        for (Pair<Integer, IAnimation> layer : layers) {
            if (layer.getRight().isActive()) return true;
        }
        return false;
    }

    @Override
    public void tick() {
        for (Pair<Integer, IAnimation> layer : layers) {
            if (layer.getRight().isActive()) {
                layer.getRight().tick();
            }
        }
    }

    @Override
    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value0) {
        for (Pair<Integer, IAnimation> layer : layers) {
            if (layer.getRight().isActive()) {
                value0 = layer.getRight().get3DTransform(modelName, type, tickDelta, value0);
            }
        }
        return value0;
    }

    @Override
    public void setupAnim(float tickDelta) {
        for (Pair<Integer, IAnimation> layer : layers) {
            layer.getRight().setupAnim(tickDelta);
        }
    }


    /**
     * Add an animation layer.
     * If there are multiple with the same priority, the one, added first will have larger priority
     * @param priority priority
     * @param layer    animation layer
     * @apiNote Same priority entries logic is subject to change
     */
    public void addAnimLayer(int priority, IAnimation layer) {
        int search = 0;
        //Insert the layer into the correct slot
        while (layers.size() > search && layers.get(search).getLeft() < search) {
            search++;
        }
        layers.add(new Pair<>(priority, layer));
    }

    /**
     * Remove an animation layer
     * @param layer needle
     * @return true if any elements were removed.
     */
    public boolean removeLayer(IAnimation layer) {
        return layers.removeIf(integerIAnimationPair -> integerIAnimationPair.getRight() == layer);
    }

    /**
     * Remove EVERY layer with priority
     * @param layerLevel search and destroy
     * @return true if any elements were removed.
     */
    public boolean removeLayer(int layerLevel) {
        return layers.removeIf(integerIAnimationPair -> integerIAnimationPair.getLeft() == layerLevel);
    }

}
