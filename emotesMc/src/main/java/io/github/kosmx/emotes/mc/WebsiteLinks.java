package io.github.kosmx.emotes.mc;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebsiteLinks {
    public static final URI WEBSITE = URI.create("https://emotes.redlance.org/");
    private static final Pattern WEBSITE_LINK = Pattern.compile(Pattern.quote(WEBSITE.toString()) + "[\\w/-]*");

    public static MutableComponent link(MutableComponent text, URI uri) {
        return text.withStyle(style -> style
                .withUnderlined(true)
                .withColor(ChatFormatting.BLUE)
                .withClickEvent(new ClickEvent.OpenUrl(uri))
        );
    }

    public static Component decorate(Component message) {
        return WEBSITE_LINK.matcher(message.getString()).find() ? linkify(message) : message;
    }

    private static MutableComponent linkify(Component component) {
        MutableComponent linked = component.getContents() instanceof PlainTextContents.LiteralContents(String text)
                ? linkify(text) : MutableComponent.create(component.getContents());
        linked.setStyle(component.getStyle());
        for (Component sibling : component.getSiblings()) {
            linked.append(linkify(sibling));
        }
        return linked;
    }

    private static MutableComponent linkify(String text) {
        MutableComponent linked = Component.empty();
        Matcher matcher = WEBSITE_LINK.matcher(text);
        int end = 0;
        while (matcher.find()) {
            linked.append(text.substring(end, matcher.start()));
            linked.append(link(Component.literal(matcher.group()), URI.create(matcher.group())));
            end = matcher.end();
        }
        return linked.append(text.substring(end));
    }
}
