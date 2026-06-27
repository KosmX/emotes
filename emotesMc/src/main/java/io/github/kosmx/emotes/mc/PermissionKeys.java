package io.github.kosmx.emotes.mc;

import net.minecraft.resources.Identifier;

import java.util.List;

public class PermissionKeys {
    public static final Identifier PLAY_PLAYER = McUtils.newIdentifier("emotes.play.player");
    public static final Identifier STOP_PLAYER = McUtils.newIdentifier("emotes.stop.player");
    public static final Identifier STOP_FORCED = McUtils.newIdentifier("emotes.stop.forced");
    public static final Identifier SHOW_HIDDEN = McUtils.newIdentifier("emotes.play.showhidden");
    public static final Identifier RELOAD = McUtils.newIdentifier("emotes.reload");

    public static final List<Identifier> PERMISSIONS = List.of(
            PLAY_PLAYER, STOP_PLAYER, STOP_FORCED, SHOW_HIDDEN, RELOAD
    );
}
