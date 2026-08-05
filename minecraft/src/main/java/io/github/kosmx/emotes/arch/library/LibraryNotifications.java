package io.github.kosmx.emotes.arch.library;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.toast.EmotecraftToast;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryException;
import org.redlance.emotecraftlibrary.sdk.NotificationListener;
import org.redlance.emotecraftlibrary.shared.models.NotificationDto;

import java.util.concurrent.CompletableFuture;

/**
 * Shows the library's notifications as toasts. The stream is open only while the library is
 * {@link LibraryStatus#ENABLED} and the toggle is on, so {@link #refresh} reconciles it with both.
 */
public final class LibraryNotifications implements NotificationListener {
    private static final LibraryNotifications INSTANCE = new LibraryNotifications();

    private static final long DISPLAY_TIME = 8000L; // a notification is worth reading, so it stays a bit longer

    private CompletableFuture<AutoCloseable> connection;
    // Bumped on every open and close. Async callbacks capture it and bail once it changed, so a stale open's
    // failure can't drop the stream that replaced it, and a stream being closed can't still raise a toast.
    private int connectionGeneration;

    private LibraryNotifications() {}

    /**
     * Opens or closes the stream to match the config — call once the client is up, and whenever either
     * setting changes. Not during mod init: the SDK dispatches the stream onto a client that exists only
     * afterwards.
     */
    public static void refresh(Minecraft minecraft) {
        INSTANCE.reconcile(minecraft);
    }

    /** Closes the stream, as the client shuts down. */
    public static void close() {
        INSTANCE.dropConnection();
    }

    private void reconcile(Minecraft minecraft) {
        boolean wanted = PlatformTools.getConfig().cloudLibraryStatus.get() == LibraryStatus.ENABLED
                && PlatformTools.getConfig().libraryNotifications.get();
        if (wanted == (this.connection != null)) {
            return; // already in the wanted state
        }

        if (!wanted) {
            dropConnection();
            return;
        }

        int generation = ++this.connectionGeneration;
        this.connection = EmoteLibrary.executeAuthorized(client -> client.openNotifications(gated(generation), minecraft));
        this.connection.whenCompleteAsync((_, th) -> {
            if (th == null || !isCurrent(generation)) return;

            CommonData.LOGGER.warn("Failed to open EmotecraftLibrary notifications!", th);
            this.connection = null; // so a later refresh retries
        }, minecraft);
    }

    /** @return whether {@code generation} still names the open stream — false once it was closed or replaced. */
    private boolean isCurrent(int generation) {
        return generation == this.connectionGeneration;
    }

    /** Wraps this listener for one stream, dropping its callbacks once that stream is no longer the open one. */
    private NotificationListener gated(int generation) {
        return new NotificationListener() {
            @Override
            public void onNotification(NotificationDto notification) {
                if (isCurrent(generation)) LibraryNotifications.this.onNotification(notification);
            }

            @Override
            public void onReset() {
                if (isCurrent(generation)) LibraryNotifications.this.onReset();
            }

            @Override
            public void onError(EmoteLibraryException error, boolean fatal) {
                if (isCurrent(generation)) LibraryNotifications.this.onError(error, fatal);
            }
        };
    }

    private void dropConnection() {
        this.connectionGeneration++; // mute the stream's callbacks

        if (this.connection != null) {
            this.connection.whenComplete((closeable, _) -> EmoteLibrary.close(closeable));
            this.connection = null;
        }
    }

    @Override
    public void onNotification(NotificationDto notification) {
        if (notification.getRead()) return; // already seen — a reconnect replaying it shouldn't toast again

        ToastManager toastManager = Minecraft.getInstance().gui.toastManager();
        toastManager.addToast(new EmotecraftToast(toastManager, LibraryFolderEntry.EMOTECRAFT_LIBRARY_ICON, DISPLAY_TIME,
                McUtils.fromJson(notification.getTitle()), McUtils.fromJson(notification.getBody())
        ));
    }

    @Override
    public void onReset() {
        // (Re)connected. Nothing to catch up on: only notifications arriving from now on are shown.
    }

    @Override
    public void onError(EmoteLibraryException error, boolean fatal) {
        if (!fatal) {
            return; // Transient (network/5xx): the SDK reconnects itself with backoff.
        }

        // Fatal (session expired/revoked): the SDK stopped reconnecting. Drop the dead stream and open a fresh
        // one — executeAuthorized re-authorizes on the way, which is exactly what the session needs. Should
        // that fail too, the open's own callback clears the field and leaves it to the next refresh.
        CommonData.LOGGER.warn("EmotecraftLibrary notification stream closed", error);
        dropConnection();
        reconcile(Minecraft.getInstance());
    }
}
