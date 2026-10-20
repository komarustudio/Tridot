package pro.komaru.tridot.client.ui.overlay.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import pro.komaru.tridot.client.ui.overlay.OverlayHolder;

/**
 * Overlay renderer interface, which handles the rendering of different overlay holders.
 */
public interface OverlayRenderer {
    OverlayRenderer DEFAULT = OverlayHolder::render;

    void render(OverlayHolder<?> holder, Minecraft mc, GuiGraphics gui, float pt);
}