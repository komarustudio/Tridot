package pro.komaru.tridot.util.comps.render.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import pro.komaru.tridot.api.Utils;
import pro.komaru.tridot.util.Col;
import pro.komaru.tridot.util.comps.render.RenderStackc;
import pro.komaru.tridot.util.phys.AbsRect;
import pro.komaru.tridot.util.phys.Vec2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public interface IGuiDrawer extends RenderStackc {

    Map<String,Vec2> CACHED_DIMS = new HashMap<>();

    GuiGraphics graphics();

    default IGuiDrawer clipOn(int x, int y, int w, int h) {
        AbsRect r = AbsRect.xywhDef(x,y,w,h).pose(stack());
        graphics().enableScissor((int) r.x, (int) r.y, (int) r.x2, (int) r.y2);
        return this;
    }
    default IGuiDrawer clipOff() {
        graphics().disableScissor();
        return this;
    }

    default IGuiDrawer alpha(float a) {
        return color(getColor(),a);
    }

    default Col getColor() {
        float[] color = RenderSystem.getShaderColor();
        return new Col(color[0],color[1],color[2],color[3]);
    }
    default IGuiDrawer color() {
        return color(1f,1f,1f,1f);
    }
    default IGuiDrawer color(Col rgb, float a) {
        return color(rgb.r,rgb.g,rgb.b,a);
    }
    default IGuiDrawer color(Col color) {
        return color(color.r,color.g,color.b,color.a);
    }
    default IGuiDrawer color(float r, float g, float b, float a) {
        RenderSystem.setShaderColor(r,g,b,a);
        return this;
    }
    default IGuiDrawer rect(String texture, float x, float y) {
        return rect(texture,x,y,0f);
    }
    default IGuiDrawer rect(String texture, float x, float y, float rotation) {
        return rect(texture,x,y,1f,1f,rotation);
    }
    default IGuiDrawer rect(String texture, float x, float y, float sclx, float scly) {
        return rect(texture,x,y,sclx,scly,0f);
    }
    default IGuiDrawer rect(String texture, float x, float y, float sclx, float scly, float rotation) {
        return rect(texture,x,y,sclx,scly,rotation,0,0,-1,-1);
    }
    default IGuiDrawer rect(String texture, float x, float y, float sclx, float scly, float rotation, float clipX, float clipY, float clipW, float clipH) {
        float tw = textureSize(texturePath(texture)).x;
        float th = textureSize(texturePath(texture)).y;
        return rect(texture,x,y,sclx,scly,rotation,(int) (clipX*tw),(int) (clipY*th),(int) (clipW*tw),(int) (clipH*th));
    }
    default IGuiDrawer rect(String texture, float x, float y, float sclx, float scly, float rotation, int clipX, int clipY, int clipW, int clipH) {
        clipW = clipW == -1 ? (int) textureSize(texturePath(texture)).x : clipW;
        clipH = clipH == -1 ? (int) textureSize(texturePath(texture)).y : clipH;
        return rect(texture,x,y,sclx,scly,rotation,clipX,clipY,clipW,clipH,
                (int) textureSize(texturePath(texture)).x,
                (int) textureSize(texturePath(texture)).y);
    }
    default IGuiDrawer rect(String texture, float x, float y, float sclx, float scly, float rotation, int clipX, int clipY, int clipW, int clipH, int tw, int th) {
        return rect(texture,x,y,sclx,scly,rotation,0,0,clipX,clipY,clipW,clipH,tw,th);
    };
    default IGuiDrawer rect(String texture, float x, float y, float sclx, float scly, float rotation, float rotpx, float rotpy, int clipX, int clipY, int clipW, int clipH, int tw, int th) {
        push();

        move(x,y);
        scale(sclx,scly);
        rotate(rotation,tw/2f+rotpx,th/2f+rotpy);

        graphics().blit(texturePath(texture),
                0,0,clipX,clipY,clipW,clipH,tw,th);

        pop();

        return this;
    };

    default ResourceLocation texturePath(String path) {
        if(path.contains(":")) return ResourceLocation.parse(path).withPath(s -> "textures/"+s+".png");
        return ResourceLocation.fromNamespaceAndPath(namespace(), "textures/"+path+".png");
    }
    String namespace();

    private Vec2 textureSize(ResourceLocation location) {
        TextureManager mng = Minecraft.getInstance().getTextureManager();
        if(IGuiDrawer.CACHED_DIMS.containsKey(location.toString())) {
            return IGuiDrawer.CACHED_DIMS.get(location.toString());
        }
        try {
            NativeImage img = NativeImage.read(
                    Minecraft.getInstance().getResourceManager().getResource(location).get().open()
            );
            IGuiDrawer.CACHED_DIMS.put(location.toString(), new Vec2(img.getWidth(), img.getHeight()));
            return new Vec2(img.getWidth(), img.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
            return new Vec2(256, 256);
        }
    }

    default Minecraft mc() {
        return Utils.mc();
    }
    default int screenW() {
        return mc().getWindow().getGuiScaledWidth();
    }
    default float screenCx() {
        return screenW()/2f;
    }
    default float screenCy() {
        return screenH()/2f;
    }
    default int screenH() {
        return mc().getWindow().getGuiScaledHeight();
    }
}
