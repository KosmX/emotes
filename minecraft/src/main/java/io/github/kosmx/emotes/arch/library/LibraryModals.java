package io.github.kosmx.emotes.arch.library;

import io.github.kosmx.emotes.arch.library.modals.AccountNotLinkedScreen;
import io.github.kosmx.emotes.arch.library.modals.BaseModalScreen;
import io.github.kosmx.emotes.arch.library.modals.DownloadQuotaScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryException;

import java.util.function.Function;

/** Opens the matching modal for an actionable library error, over the current screen, without stacking. */
public final class LibraryModals {
    private LibraryModals() {}

    /**
     * Call once where an error actually occurs (not from a re-rendered display path).
     * @return {@code true} if it was actionable and a modal was queued — the folder can't proceed, so callers loading
     * it can bail out (close the folder). {@code false} for a non-actionable error the caller should surface inline.
     */
    public static boolean show(Throwable throwable) {
        Throwable cause = EmoteLibrary.unwrap(throwable);

        Function<Screen, BaseModalScreen> factory;
        if (cause instanceof EmoteLibraryException.DownloadQuotaExceeded) {
            factory = DownloadQuotaScreen::new;
        } else if (cause instanceof EmoteLibraryException.AccountNotLinked) {
            factory = AccountNotLinkedScreen::new;
        } else {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> { // setScreen must run on the render thread; callers may be off it
            if (!(minecraft.gui.screen() instanceof BaseModalScreen)) { // don't stack a modal over a modal
                minecraft.gui.setScreen(factory.apply(minecraft.gui.screen()));
            }
        });
        return true;
    }
}
