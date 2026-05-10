package pro.komaru.tridot.client.ui.overlay.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import pro.komaru.tridot.client.ui.overlay.OverlayHolder;

/**
 * Represents a renderable overlay managed by an {@link OverlayHolder}.
 *
 * <p>Implement this interface to define overlay behavior on show, hide, and render.
 *
 * <p>Note: holder methods such as {@link OverlayHolder#show()} and {@link OverlayHolder#hide()}
 * must not be called from within these callbacks as they are guarded against recursive invocation.
 */
public interface Overlay {
    /**
     * Called when the overlay becomes visible.
     *
     * @param holder the holder managing this overlay
     */
    void onShow(OverlayHolder<?> holder);

    /**
     * Called each render frame while the overlay is visible.
     *
     * @param holder the holder managing this overlay
     * @param mc     the Minecraft client instance
     * @param gui    the gui graphics
     */
    void onRender(OverlayHolder<?> holder, Minecraft mc, GuiGraphics gui, float pt);

    /**
     * Called when the overlay is hidden.
     *
     * @param holder the holder managing this overlay
     */
    void onHide(OverlayHolder<?> holder);
}
