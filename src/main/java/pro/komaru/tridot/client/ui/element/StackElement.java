package pro.komaru.tridot.client.ui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import pro.komaru.tridot.client.ui.enums.AlignmentDirection;

public class StackElement extends BaseElement<StackElement> {
    public StackElement(AlignmentDirection childrenAlignment) {
        this.childrenAlignment = childrenAlignment;
    }

    @Override
    public void renderElement(Minecraft mc, GuiGraphics gui, float pt) {
        
    }
}
