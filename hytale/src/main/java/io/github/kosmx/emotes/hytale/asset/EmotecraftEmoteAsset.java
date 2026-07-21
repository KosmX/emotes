package io.github.kosmx.emotes.hytale.asset;

import com.hypixel.hytale.server.core.cosmetics.EmoteAsset;

/**
 * An {@link EmoteAsset} built in code rather than decoded from a pack's {@code Server/Emote/*.json}.
 * <p>
 * {@code EmoteAsset} exposes no setters — its fields are {@code protected} and normally filled in by its
 * {@code AssetBuilderCodec} — so a subclass is the supported way to populate them from a different package.
 * {@code AssetExtraInfo.Data} stays unset, which the store tolerates: it null-checks {@code codec.getData(asset)}
 * before collecting contained assets.
 */
public final class EmotecraftEmoteAsset extends EmoteAsset {
    public EmotecraftEmoteAsset(String id, String name, String animationPath, String iconPath, boolean looping) {
        super(id);

        this.name = name;
        this.animationPath = animationPath;
        this.iconPath = iconPath;
        this.isLooping = looping;
        // Keep the item visible: Emotecraft animates held items as first-class bones, and Hytale parents its
        // R-/L-Attachment nodes to the hands, so an item follows the arms without any extra work.
        this.hideItemInHand = false;
    }
}
