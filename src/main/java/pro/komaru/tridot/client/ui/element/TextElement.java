package pro.komaru.tridot.client.ui.element;

import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import pro.komaru.tridot.client.ui.model.RecomputeLayoutContext;
import pro.komaru.tridot.client.ui.model.RecomputeMeasurementsContext;

import java.util.List;

@NoArgsConstructor
public class TextElement extends BaseElement<TextElement> {
    Component text = Component.empty();
    boolean textWrap = false;

    public TextElement(Component text) {
        this.text = text;
    }

    public TextElement(String text) {
        this.text = Component.literal(text);
    }

    private List<FormattedCharSequence> lines = List.of();

    @Setter
    public boolean linesCentered = false;

    public TextElement centerLines() {
        setLinesCentered(true);
        return this;
    }

    public TextElement noCenterLines() {
        setLinesCentered(false);
        return this;
    }

    public TextElement text(String text) {
        return text(Component.literal(text));
    }

    public TextElement text(Component text) {
        this.text = text;
        return this;
    }

    public TextElement wrap() {
        this.textWrap = true;
        return this;
    }

    public TextElement noWrap() {
        this.textWrap = false;
        return this;
    }

    @Override
    public void renderElement(Minecraft mc, GuiGraphics gui, float pt) {
        var p = gui.pose();
        float yOff = 0f;
        for (FormattedCharSequence line : lines) {
            float xOff = 0f;

            if (linesCentered) {
                float lineWidth = mc.font.width(line);
                float widthDiff = width - lineWidth;
                xOff = widthDiff / 2f;
            }

            p.pushPose();
            p.translate(xOff, yOff, 0f);
            gui.drawString(mc.font, line, 0, 0, 0xFFFFFFFF);
            p.popPose();

            yOff += mc.font.lineHeight;
        }
    }

    @Override
    protected void recomputeInternalDimensions(RecomputeMeasurementsContext context) {
        Minecraft mc = Minecraft.getInstance();
        int maxWidth = (int) Math.min(context.getAvailableWidth(), getMaxWidth() < 0f ? Float.MAX_VALUE : getMaxWidth());
        lines = splitText(mc, text, maxWidth);
        width = textWidth(mc, lines);
        height = textHeight(mc, lines);
        super.recomputeInternalDimensions(context);
    }

    @Override
    protected void recomputeOuterDimensions(RecomputeLayoutContext context) {
        super.recomputeOuterDimensions(context);
    }

    private int textHeight(Minecraft mc, List<FormattedCharSequence> lines) {
        return lines.size() * mc.font.lineHeight;
    }

    private int textWidth(Minecraft mc, List<FormattedCharSequence> lines) {
        int max = 0;
        for (FormattedCharSequence line : lines)
            max = Math.max(max, mc.font.width(line));
        return max;
    }

    private List<FormattedCharSequence> splitText(Minecraft mc, Component component, int maxWidth) {
        if (!textWrap || maxWidth <= 0) return List.of(component.getVisualOrderText());
        return mc.font.split(component, maxWidth);
    }
}
