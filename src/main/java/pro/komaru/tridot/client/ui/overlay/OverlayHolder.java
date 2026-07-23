package pro.komaru.tridot.client.ui.overlay;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import pro.komaru.tridot.client.ui.overlay.interfaces.Overlay;

import java.util.function.Consumer;

/**
 * Holds a single {@link Overlay} instance and manages its lifecycle —
 * visibility state and render/show/hide callbacks.
 *
 * <p>Recursive calls to holder methods from within overlay callbacks
 * (e.g. calling {@link #show()} inside {@link Overlay#onShow}) are not permitted
 * and will throw {@link IllegalStateException}.
 *
 * @param <T> the overlay type
 */
@RequiredArgsConstructor
public class OverlayHolder<T extends Overlay> {
    private final Class<T> type;
    /**
     * The managed overlay instance.
     */
    @Getter
    private final T instance;
    /**
     * -- GETTER --
     * Returns whether this overlay is currently visible.
     *
     * @return {@code true} if visible
     */
    @Getter
    private boolean visible = false;

    /**
     * Guards against recursive invocation of holder methods from overlay callbacks.
     */
    private boolean lockInnerMethods = false;

    /**
     * Executes the provided body with overlay instance
     *
     * @param body the execution body
     */
    public void apply(Consumer<T> body) {
        body.accept(instance);
    }

    /**
     * Shows the overlay by invoking {@link Overlay#onShow(OverlayHolder)}
     * and marking it as visible.
     *
     * @throws IllegalStateException if called recursively from within an overlay callback
     */
    public void show() {
        if (lockInnerMethods)
            throw new IllegalStateException("Cannot invoke holder method while overlay is being called");

        if (isVisible()) return;

        lockInnerMethods = true;

        try {
            instance.onShow(this);
            visible = true;
        } finally {
            lockInnerMethods = false;
        }
    }

    /**
     * Hides the overlay by invoking {@link Overlay#onHide(OverlayHolder)}
     * and marking it as not visible.
     *
     * @throws IllegalStateException if called recursively from within an overlay callback
     */
    public void hide() {
        if (lockInnerMethods)
            throw new IllegalStateException("Cannot invoke holder method while overlay is being called");

        if (!isVisible()) return;

        lockInnerMethods = true;

        try {
            instance.onHide(this);
            visible = false;
        } finally {
            lockInnerMethods = false;
        }
    }

    /**
     * Renders the overlay by invoking {@link Overlay#onRender(OverlayHolder, Minecraft, GuiGraphics)}.
     * Does nothing if the overlay is not visible.
     *
     * @param mc  the Minecraft client instance
     * @param gui the gui graphics
     * @throws IllegalStateException if called recursively from within an overlay callback
     */
    public void render(Minecraft mc, GuiGraphics gui, float pt) {
        if (lockInnerMethods)
            throw new IllegalStateException("Cannot invoke holder method while overlay is being called");

        if (!isVisible()) return;

        lockInnerMethods = true;

        try {
            instance.onRender(this, mc, gui, pt);
        } finally {
            lockInnerMethods = false;
        }
    }

    /**
     * Returns whether this holder's overlay type is assignable from the given type.
     * Useful for type-safe casting when retrieving holders from {@link Overlays}.
     *
     * @param type the type to check against
     * @param <O>  the overlay type
     * @return {@code true} if this holder's type matches or is a subtype of the given type
     */
    public <O extends Overlay> boolean matchesType(Class<O> type) {
        return type.isAssignableFrom(this.type);
    }
}
