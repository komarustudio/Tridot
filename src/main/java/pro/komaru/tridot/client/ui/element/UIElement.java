package pro.komaru.tridot.client.ui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import pro.komaru.tridot.client.ui.enums.MouseButtonType;
import pro.komaru.tridot.client.ui.model.RecomputeLayoutContext;
import pro.komaru.tridot.client.ui.model.RecomputeMeasurementsContext;

import java.util.List;

public interface UIElement {
    float getMarginLeft();

    float getMarginTop();

    float getMarginRight();

    float getMarginBottom();

    float getRelativeX();

    float getRelativeY();

    float getWidth();

    float getHeight();

    List<UIElement> getChildren();

    UIElement getParent();

    void setParent(UIElement parent);

    void recomputeMeasurements(RecomputeMeasurementsContext context);

    void recomputeLayout(RecomputeLayoutContext context);

    default void render(Minecraft mc, GuiGraphics gui, float pt) {
        render(mc, gui, pt, -1f, -1f);
    }

    void render(Minecraft mc, GuiGraphics gui, float pt, float mx, float my);

    boolean isMouseOver(float mouseX, float mouseY);

    boolean mouseDown(float mouseX, float mouseY, MouseButtonType mouseButtonType);

    void mouseUp(float mouseX, float mouseY, MouseButtonType mouseButtonType);
}
