package io.github.kosmx.emotes.arch.mixin.chat;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.kosmx.emotes.mc.WebsiteLinks;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {
    // Servers without Emotecraft pass the links on as plain text
    @ModifyExpressionValue(
            method = "handlePlayerChatMessage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/PlayerChatMessage;decoratedContent()Lnet/minecraft/network/chat/Component;")
    )
    private Component emotecraft$linkWebsite(Component content) {
        return WebsiteLinks.decorate(content);
    }
}
