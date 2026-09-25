package pro.komaru.tridot.client.render.gui.overlay;

import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.resources.*;
import net.minecraft.util.*;
import net.neoforged.neoforge.client.event.*;
import pro.komaru.tridot.client.ClientTick;
import pro.komaru.tridot.common.config.*;
import pro.komaru.tridot.util.*;

public class TimedOverlayInstance implements OverlayInstance{
    private ResourceLocation location;
    private int showTick;
    private int showTime;

    public float fadeIn = 20;
    public float fadeOut = 20;
    public float opacity = 1;

    public void tick(ClientTickEvent.Post event) {
        int totalDuration = (int) (fadeIn + showTime + fadeOut);
        if (ClientConfig.ABILITY_OVERLAY.get()) {
            if (showTick < totalDuration) {
                showTick++;
            } else {
                OverlayHandler.killInstance(this);
            }
        }
    }

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
            float alpha = getAlpha(event);

            renderOverlay(gui, alpha, width, height);
            gui.pose().popPose();
        }
    }

    private void renderOverlay(GuiGraphics gui, float alpha, int width, int height){
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        gui.setColor(opacity * alpha, opacity * alpha, opacity * alpha, alpha);
        gui.blit(location, 0, 0, 0, 0, 1920, 1080, width, height);
        gui.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        RenderSystem.applyModelViewMatrix();
    }

    private float getAlpha(RenderGuiLayerEvent.Post event) {
        float ticks = showTick + ClientTick.mcPartialTick();
        float alpha;

        float totalDuration = fadeIn + showTime + fadeOut;
        if (ticks < fadeIn) {
            alpha = Mth.clamp(ticks / fadeIn, 0.0F, 1.0F);
        }else if (ticks < fadeIn + showTime) {
            alpha = 1.0F;
        }else if (ticks < totalDuration) {
            float fadeOutProgress = (ticks - (fadeIn + showTime)) / fadeOut;
            alpha = 1.0F - Mth.clamp(fadeOutProgress, 0.0F, 1.0F);
        }else {
            alpha = 0.0F;
        }

        return alpha;
    }

    public TimedOverlayInstance setTexture(ResourceLocation location) {
        this.location = location;
        return this;
    }

    public TimedOverlayInstance setShowTime(int time) {
        this.showTime = time;
        return this;
    }

    public TimedOverlayInstance setOpacity(float opacity) {
        this.opacity = opacity;
        return this;
    }

    public TimedOverlayInstance setFadeIn(float time) {
        this.fadeIn = time;
        return this;
    }

    public TimedOverlayInstance setFadeOut(float time) {
        this.fadeOut = time;
        return this;
    }
}
