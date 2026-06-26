package pro.komaru.tridot.api.render;

import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.*;
import pro.komaru.tridot.api.*;
import pro.komaru.tridot.util.Col;
import pro.komaru.tridot.util.phys.AbsRect;
import pro.komaru.tridot.util.struct.func.*;

import java.util.*;

public abstract class DotScreen extends Screen {
    public boolean pause = false;
    public int tick = 0;

    public PoseStack localPose;
    public GuiGraphics localG;
    public String assetsId;

    public DotScreen() {
        super(Component.literal(""));
    }

    public void update(GuiGraphics g) {
        localPose = g.pose();
        localG = g;
    }

    public void push() {
        localPose.pushPose();
    }

    public void pop() {
        localPose.popPose();
    }

    public void br() {pop();push();}

    public void scissorsOn(int x, int y, int w, int h) {
        AbsRect r = AbsRect.xywhDef(x,y,w,h).pose(localPose);
        localG.enableScissor((int) r.x, (int) r.y, (int) r.x2, (int) r.y2);
    }

    public void scissorsOff() {
        localG.disableScissor();
    }

    public void blit(String texture,int x,int y,int cutx,int cuty,int cutw,int cuth,int tw, int th) {
        ResourceLocation location = texture.contains(":") ? new ResourceLocation(texture) : new ResourceLocation(assetsId,texture);
        localG.blit(location,
                x,y,cutx,cuty,cutw,cuth,tw,th);
    }

    public void blit(String texture,int x,int y,int tw,int ty) {
        blit(texture,x,y,0,0,tw,ty,tw,ty);
    }

    public void blit(String texture,int x,int y,int cutx,int cuty,int cutw,int cuth) {
        blit(texture,x,y,cutx,cuty,cutw,cuth,256,256);
    }

    public void color(Col col) {
        color(col.r,col.g,col.b,col.a);
    }

    public void color(float r, float g, float b, float a) {
        if(localG != null) localG.setColor(r,g,b,a);
        else RenderSystem.setShaderColor(r,g,b,a);
    }

    public void color(float r,float g,float b) {
        color(r,g,b,color(3));
    }

    public void color(float a) {
        color(color(0),color(1),color(2),a);
    }

    public void color() {
        color(1f,1f,1f,1f);
    }

    public float color(int idx) {
        return RenderSystem.getShaderColor()[idx];
    }

    @Override
    public boolean isPauseScreen() {
        return pause;
    }

    @Override
    public void tick() {
        tick++;
    }

    public float time() {
        return tick + mc().getPartialTick();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);
        update(g);
        renderBefore(g,mouseX,mouseY,partial);
        renderChildren(g,mouseX,mouseY,partial);
        renderAfter(g,mouseX,mouseY,partial);
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

    public Minecraft mc() {
        return Utils.mc();
    }

    public int w() {
        return mc().getWindow().getGuiScaledWidth();
    }

    public float cx() {
        return w()/2f;
    }

    public float cy() {
        return h()/2f;
    }

    public int h() {
        return mc().getWindow().getGuiScaledHeight();
    }

    protected <T extends GuiEventListener> T addWidgetOnly(T pListener) {
        ((List<GuiEventListener>) children()).add(pListener);
        return pListener;
    }

    public void move(float x, float y, float z) {
        pose(p -> p.translate(x,y,z));
    }

    public void move(float x, float y) {
        move(x,y,0);
    }

    public void rotate(float angle) {
        localPose.mulPose(Axis.ZP.rotationDegrees(angle));
    }

    public void rotate(float px, float py, float angle) {
        move(px,py);
        rotate(angle);
        move(-px,-py);
    }

    public void scale(float x, float y, float px, float py) {
        move(px,py);
        scale(x,y);
        move(-px,-py);
    }

    public void scale(float x, float y, float z) {
        pose(p -> p.scale(x,y,z));
    }

    public void scale(float x, float y) {
        scale(x,y,1f);
    }

    public void scale(float xy) {
        scale(xy,xy);
    }

    public void layer(float z) {
        move(0,0,z);
    }

    public void pose(Cons<PoseStack> p) {
        if(localPose != null) p.get(localPose);
    }
}
