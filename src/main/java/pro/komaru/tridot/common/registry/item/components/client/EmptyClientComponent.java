package pro.komaru.tridot.common.registry.item.components.client;

import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.neoforged.api.distmarker.*;

@OnlyIn(Dist.CLIENT)
public class EmptyClientComponent implements ClientTooltipComponent{
    public final int height;

    public EmptyClientComponent(int height){
        this.height = height;
    }

    public static ClientTooltipComponent create(int height){
        return new EmptyClientComponent(height);
    }

    @Override
    public int getHeight(){
        return height;
    }

    @Override
    public int getWidth(Font pFont){
        return 0;
    }
}