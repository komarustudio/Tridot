package pro.komaru.tridot.common.registry.item.components.client;

import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.client.renderer.*;
import net.minecraft.locale.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;
import net.neoforged.api.distmarker.*;
import org.joml.*;

import java.lang.Math;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class TextClientComponent implements ClientTooltipComponent{
    private final int maxChars = 200;
    private final List<FormattedCharSequence> lines;
    private final int paddingTop;
    private TextClientComponent(MutableComponent text, int paddingTop) {
        this.lines = Language.getInstance().getVisualOrder(Minecraft.getInstance().font.getSplitter().splitLines(text, maxChars, text.getStyle()));
        this.paddingTop = paddingTop;
    }

    public static ClientTooltipComponent create(MutableComponent text) {
        return new TextClientComponent(text, 0);
    }

    @Override
    public int getHeight() {
        return Math.max(0, (12 * lines.size()) + paddingTop);
    }

    @Override
    public int getWidth(Font pFont) {
        int width = 0;
        for (final FormattedCharSequence line : lines) {
            float scale = 1;
            int lineWidth = (int) (pFont.width(line) * scale);
            if (lineWidth > width) {
                width = lineWidth;
            }
        }

        return width;
    }

   public void renderText(Font pFont, int pMouseX, int pMouseY, Matrix4f pMatrix, MultiBufferSource.BufferSource pBufferSource) {
       int y = pMouseY + paddingTop;
       for (final FormattedCharSequence line : lines) {
           float scale = 1;
           Matrix4f scaled = new Matrix4f(pMatrix);
           scaled.scale(scale, scale, 1);
           pFont.drawInBatch(line, pMouseX / scale, (y / scale) + 1, -1, true, scaled, pBufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
           y += 9;
       }
   }
}