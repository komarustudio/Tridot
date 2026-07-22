package pro.komaru.tridot.common.registry.item.components.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import org.joml.Matrix4f;
import pro.komaru.tridot.Tridot;

import java.util.List;

public class ObjectClientComponent implements ClientTooltipComponent{
    public final ResourceLocation bg = Tridot.ofTridot("textures/gui/tooltips/background.png");
    public final Item icon;
    public final int maxChars = 200;
    public final int iconMargin = 6;

    public final int paddingTop;
    public final int textPaddingTop;

    public final List<FormattedCharSequence> lines;
    public ObjectClientComponent(MutableComponent text, Item item, int paddingTop, int textPaddingTop) {
        this.lines = Language.getInstance().getVisualOrder(Minecraft.getInstance().font.getSplitter().splitLines(text, maxChars, text.getStyle()));
        this.paddingTop = paddingTop;
        this.textPaddingTop = textPaddingTop;
        this.icon = item;
    }

    public static ClientTooltipComponent create(MutableComponent text, Item icon, int paddingTop, int textPaddingTop) {
        return new ObjectClientComponent(text, icon, paddingTop, textPaddingTop);
    }

    @Override
    public int getHeight() {
        return Math.max(18 + iconMargin, (10 * lines.size())) + paddingTop + textPaddingTop;
    }

    @Override
    public int getWidth(Font pFont) {
        int width = 0;
        for (final FormattedCharSequence line : lines) {
            float scale = 1;
            int lineWidth = 18 + iconMargin + (int) (pFont.width(line) * scale);
            if (lineWidth > width) {
                width = lineWidth;
            }
        }

        return width;
    }

    @Override
    public void renderText(Font pFont, int pMouseX, int pMouseY, Matrix4f pMatrix, MultiBufferSource.BufferSource pBufferSource) {
        final int x = pMouseX + 18 + 4;
        int y = pMouseY + textPaddingTop + paddingTop;
        for (final FormattedCharSequence line : lines) {
            float scale = 1;
            Matrix4f scaled = new Matrix4f(pMatrix);
            scaled.scale(scale, scale, 1);
            pFont.drawInBatch(line, x / scale, (y / scale) + 1, -1, true, scaled, pBufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
            y += 9;
        }
    }

    @Override
    public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
        RenderSystem.enableBlend();
        pGuiGraphics.blit(bg, pX, pY + (paddingTop) - 1, 0, 0, 18, 18, 18, 18);
        pGuiGraphics.renderFakeItem(icon.getDefaultInstance(), pX + 1, pY + 1 + (paddingTop) - 1);
        RenderSystem.disableBlend();
    }
}