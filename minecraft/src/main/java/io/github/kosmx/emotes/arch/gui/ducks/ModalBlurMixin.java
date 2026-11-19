package io.github.kosmx.emotes.arch.gui.ducks;

public interface ModalBlurMixin {
    /** @return whether a modal blurs after the screen's own blur; the next traversal after the blur then starts from the modal's */
    default boolean emotecraft$passScreenBlur() {
        throw new AssertionError();
    }
}
