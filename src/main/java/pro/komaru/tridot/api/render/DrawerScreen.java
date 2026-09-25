package pro.komaru.tridot.api.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pro.komaru.tridot.client.ClientTick;
import pro.komaru.tridot.util.comps.render.gui.IGuiDrawer;

import java.util.List;

public abstract class DrawerScreen extends Screen implements IGuiDrawer {

    private GuiGraphics cacheGraphics;
    private PoseStack cacheStack;

    public boolean isPauseScreen = false;
    public int tick = 0;

    public DrawerScreen() {
        super(Component.literal(""));
    }

    @Override
    public boolean isPauseScreen() {
        return isPauseScreen;
    }
    @Override
    public void tick() {
        tick++;
    }
    public float time() {
        return tick + ClientTick.mcPartialTick();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float pt) {
        cacheGraphics = g;
        cacheStack = g.pose();
        renderBefore(g,mouseX,mouseY,pt);
        renderChildren(g,mouseX,mouseY,pt);
        renderAfter(g,mouseX,mouseY,pt);
    }

    public void renderBefore(GuiGraphics g, int mouseX, int mouseY, float partial) {

    }

    public void renderAfter(GuiGraphics g, int mouseX, int mouseY, float partial) {

    }

    public void renderChildren(GuiGraphics g, int mouseX, int mouseY, float partial) {
        for (Renderable renderable : renderables) renderChild(renderable,g,mouseX,mouseY,partial);
    }
    public void renderChild(Renderable r, GuiGraphics g, int mouseX, int mouseY, float partial) {
        r.render(g,mouseX,mouseY,partial);
    }

    public <T> T add(T object) {
        if(object instanceof Renderable r) addRenderableOnly(r);
        if(object instanceof GuiEventListener a) addWidgetOnly(a);
        return object;
    }

    protected <T extends GuiEventListener> T addWidgetOnly(T pListener) {
        ((List<GuiEventListener>) children()).add(pListener);
        return pListener;
    }

    @Override
    public PoseStack stack() {
        return cacheStack;
    }
    @Override
    public GuiGraphics graphics() {
        return cacheGraphics;
    }
}
