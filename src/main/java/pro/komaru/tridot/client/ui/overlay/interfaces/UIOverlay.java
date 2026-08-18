package pro.komaru.tridot.client.ui.overlay.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import pro.komaru.tridot.client.ui.element.WindowElement;
import pro.komaru.tridot.client.ui.model.RecomputeLayoutContext;
import pro.komaru.tridot.client.ui.model.RecomputeMeasurementsContext;
import pro.komaru.tridot.client.ui.overlay.OverlayHolder;

public abstract class UIOverlay implements Overlay {
    private WindowElement origin;
    private boolean uiDirty = false;

    private float lastW, lastH = 0f;

    @Override
    public void onShow(OverlayHolder<?> holder) {
        origin = new WindowElement();
        initElements(origin);
        uiDirty = true;
    }

    @Override
    public void onRender(OverlayHolder<?> holder, Minecraft mc, GuiGraphics gui, float pt) {
        if (uiDirty || hasWindowChanged(mc)) recomputeUI(mc);
        renderUI(mc, gui, pt);
    }

    protected void renderUI(Minecraft mc, GuiGraphics gui, float pt) {
        origin.render(mc, gui, pt);
    }

    protected boolean hasWindowChanged(Minecraft mc) {
        float w = mc.getWindow().getGuiScaledWidth();
        float h = mc.getWindow().getGuiScaledHeight();

        return w != lastW || h != lastH;
    }

    protected void recomputeUI(Minecraft mc) {
        float w = mc.getWindow().getGuiScaledWidth();
        float h = mc.getWindow().getGuiScaledHeight();

        var measureContext = RecomputeMeasurementsContext.builder()
                .availableWidth(w)
                .availableHeight(h)
                .build();

        var recomputeContext = RecomputeLayoutContext.builder()
                .availableWidth(w)
                .availableHeight(h)
                .build();

        origin.recomputeMeasurements(measureContext);
        origin.recomputeLayout(recomputeContext);

        lastW = w;
        lastH = h;

        uiDirty = false;
    }

    public void markUIDirty() {
        uiDirty = true;
    }

    protected abstract void initElements(WindowElement origin);
}
