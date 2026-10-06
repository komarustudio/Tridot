package pro.komaru.tridot.client.ui.overlay.render;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import pro.komaru.tridot.client.ui.overlay.OverlayHolder;
import pro.komaru.tridot.client.ui.overlay.Overlays;

/**
 * Handles rendering all of overlays in provided registry by given overlay renderer.
 */
@RequiredArgsConstructor
public class OverlayRenderHook {
    private final OverlayRenderer renderer;
    private final Overlays overlayRegistry;

    /**
     * Render gui event listener, calls render on every OverlayRenderer in registry
     *
     * @param event the event object
     */
    public void onRenderGuiPost(RenderGuiEvent.Post event) {
        var mc = Minecraft.getInstance();
        var gui = event.getGuiGraphics();
        float pt = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        for (OverlayHolder<?> holder : overlayRegistry.getAll().values())
            renderer.render(holder, mc, gui, pt);
    }
}
