package pro.komaru.tridot.client.ui.screen;

import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pro.komaru.tridot.client.ui.element.WindowElement;
import pro.komaru.tridot.client.ui.enums.MouseButtonType;
import pro.komaru.tridot.client.ui.model.RecomputeLayoutContext;
import pro.komaru.tridot.client.ui.model.RecomputeMeasurementsContext;

public abstract class UIScreen extends Screen {
    private WindowElement origin;
    private boolean uiDirty = false;

    private boolean renderDefaultBackground = false;

    protected UIScreen() {
        super(Component.empty());
    }

    public void markUIDirty() {
        uiDirty = true;
    }

    protected void recomputeUI(Minecraft mc) {
        float w = width;
        float h = height;

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

        uiDirty = false;
    }

    @Override
    protected void init() {
        origin = new WindowElement();
        initElements(origin);
        uiDirty = true;
    }

    @Override
    public void render(@NonNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        var mc = Minecraft.getInstance();

        if (renderDefaultBackground) this.renderBackground(gui, mouseX, mouseY, partialTick);
        for (Renderable renderable : this.renderables) renderable.render(gui, mouseX, mouseY, partialTick);

        if (uiDirty) recomputeUI(mc);
        renderUI(mc, gui, mouseX, mouseY, partialTick);
    }

    protected void renderUI(Minecraft mc, GuiGraphics gui, int mx, int my, float pt) {
        origin.render(mc, gui, pt, mx, my);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MouseButtonType buttonType = switch (button) {
            case 1 -> MouseButtonType.RIGHT;
            case 2 -> MouseButtonType.WHEEL;
            default -> MouseButtonType.LEFT;
        };
        if (origin.mouseDown((float) mouseX, (float) mouseY, buttonType)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        MouseButtonType buttonType = switch (button) {
            case 1 -> MouseButtonType.RIGHT;
            case 2 -> MouseButtonType.WHEEL;
            default -> MouseButtonType.LEFT;
        };
        origin.mouseUp((float) mouseX, (float) mouseY, buttonType);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    protected abstract void initElements(WindowElement origin);
}
