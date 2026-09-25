package pro.komaru.tridot.api.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import pro.komaru.tridot.util.struct.func.Cons;

public class GuiDraw {
    private PoseStack localPose;
    private GuiGraphics localG;

    public GuiDraw(GuiGraphics g) {
        this.localG = g;
        this.localPose = g.pose();
    }

    public void push() {
        localPose.pushPose();
    }
    public void pop() {
        localPose.popPose();
    }

    public void scissorsOn(int x, int y, int w, int h) {
        localG.enableScissor(x,y,x+w,y+h);
    }
    public void scissorsOff() {
        localG.disableScissor();
    }

    public void blit(String texture,int x,int y,int cutx,int cuty,int cutw,int cuth,int tw, int th) {
        ResourceLocation location = ResourceLocation.parse(texture);
        localG.blit(location,
                x,y,cutx,cuty,cutw,cuth,tw,th);
    }
    public void blit(String texture,int x,int y,int tw,int ty) {
        blit(texture,x,y,0,0,tw,ty,tw,ty);
    }
    public void blit(String texture,int x,int y,int cutx,int cuty,int cutw,int cuth) {
        blit(texture,x,y,cutx,cuty,cutw,cuth,256,256);
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
