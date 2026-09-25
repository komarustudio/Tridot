package pro.komaru.tridot.client.render.gui.overlay;

import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.resources.*;
import net.minecraft.util.*;
import net.neoforged.neoforge.client.event.*;
import pro.komaru.tridot.common.config.*;
import pro.komaru.tridot.util.*;

public class ClampedOverlayInstance implements OverlayInstance{
    private ResourceLocation location;
    private float current;
    private float max;

    public void onDraw(RenderGuiLayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics gui = event.getGuiGraphics();
        if (ClientConfig.ABILITY_OVERLAY.get()) {
            if(location == null) {
                Log.error(this + " Location is null");
                location = ResourceLocation.withDefaultNamespace("missingno");
            }

            gui.pose().pushPose();
            gui.pose().translate(0, 0, -200);
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            float alpha = getAlpha();
            float f = 0.1F;

            renderOverlay(gui, f, alpha, width, height);
            gui.pose().popPose();
        }
    }

    private void renderOverlay(GuiGraphics gui, float f, float alpha, int width, int height){
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        gui.setColor(f * alpha, f * alpha, f * alpha, alpha);
        gui.blit(location, 0, 0, 0, 0, 1920, 1080, width, height);
        gui.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        RenderSystem.applyModelViewMatrix();
    }

    private float getAlpha() {
       return Mth.clamp(current / max, 0.0f, 1.0f);
    }

    public ClampedOverlayInstance setTexture(ResourceLocation location) {
        this.location = location;
        return this;
    }

    public void updateInstance(float amountClient, float maxClient){
        if(!OverlayHandler.instanceSeq.contains(this)){
            OverlayHandler.addInstance(this);
        }

        current = amountClient;
        max = maxClient;
    }
}
