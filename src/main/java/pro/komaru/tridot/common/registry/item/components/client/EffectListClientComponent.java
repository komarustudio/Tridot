package pro.komaru.tridot.common.registry.item.components.client;

import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.client.renderer.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.effect.*;
import org.joml.*;
import pro.komaru.tridot.api.EffectApplier;

import java.util.*;

public class EffectListClientComponent implements ClientTooltipComponent{
    private final List<EffectApplier> effects;
    private final Component component;
    private final int iconSize = 10;
    private final int padding = 0;
    private int textCurrentY = 0;

    public EffectListClientComponent(List<EffectApplier> items, Component component) {
        this.effects = items;
        this.component = component;
    }

    public static EffectListClientComponent create(List<EffectApplier> items, Component component) {
        return new EffectListClientComponent(items, component);
    }

    @Override
    public int getHeight() {
        int offset = 4;
        if(component != null && !component.equals(Component.empty())){
            offset += 10;
        }

        return (iconSize + padding) * effects.size() + offset;
    }

    @Override
    public int getWidth(Font font) {
        int maxWidth = 0;
        if(component != null && !component.equals(Component.empty())){
            maxWidth += font.width(component);
        }

        for (var entry : effects) {
            MutableComponent mutablecomponent = Component.translatable(entry.effect().value().getDescriptionId());
            if(entry.amplifier() > 0){
                mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + entry.amplifier()));
            }

            if(!entry.endsWithin(20)){
                mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, entry.formatDuration());
            }

            int currentWidth = iconSize + padding + font.width(mutablecomponent);
            if (currentWidth > maxWidth) {
                maxWidth = currentWidth + 60;
            }
        }

        return maxWidth;
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        textCurrentY = y;
        if(component != null && !component.equals(Component.empty())){
            font.drawInBatch(component, x, y, -1, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            textCurrentY = y + 10;
        }

        for (var entry : effects) {
            MutableComponent mutablecomponent = Component.translatable(entry.effect().value().getDescriptionId());
            MobEffect mobeffect = entry.effect().value();
            if(entry.amplifier() > 0){
                mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + entry.amplifier()));
            }

            if(!entry.endsWithin(20)){
                mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, entry.formatDuration());
            }

            int nameX = x + iconSize + 2 + padding;
            int nameY = textCurrentY + 2 + (iconSize - font.lineHeight) / 2;
            font.drawInBatch(mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting()), nameX, nameY, -1, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            textCurrentY += iconSize + padding;
        }
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int currentY = y;
        if(component != null && !component.equals(Component.empty())) currentY += 10;
        for (var entry : effects) {
            var sprite = Minecraft.getInstance().getMobEffectTextures().get(entry.effect());
            graphics.blit(x, currentY, 0, iconSize, iconSize, sprite);
            currentY += iconSize + padding;
        }
    }
}