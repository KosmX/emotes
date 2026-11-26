package io.github.kosmx.emotes.bukkit;

import io.github.kosmx.emotes.mc.WebsiteLinks;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class WebsiteLinkDecorator implements Listener {
    @EventHandler
    @SuppressWarnings("UnstableApiUsage")
    public void onChatDecorate(AsyncChatDecorateEvent event) {
        event.result(PaperAdventure.asAdventure(WebsiteLinks.decorate(PaperAdventure.asVanilla(event.result()))));
    }
}
