package pro.komaru.tridot.client.render;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.*;
import net.neoforged.fml.common.*;
import net.neoforged.neoforge.client.event.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import pro.komaru.tridot.client.*;
import pro.komaru.tridot.client.compatibility.*;
import pro.komaru.tridot.client.gfx.particle.*;
import pro.komaru.tridot.client.gfx.particle.behavior.*;

import java.util.*;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class LevelRenderHandler{
    public static Matrix4f MATRIX4F = null;
    static MultiBufferSource.BufferSource DELAYED_RENDER = null;
    public static RenderTarget DEPTH_CACHE;
    public static float FOG_START = 0;
    public static List<ICustomParticleRender> particleList = new ArrayList<>();
    public static Map<GenericParticle, ICustomBehaviorParticleRender> behaviorParticleList = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLevelRender(RenderLevelStageEvent event){
        PoseStack stack = event.getPoseStack();
        float partialTicks = ClientTick.mcPartialTick();
        MultiBufferSource bufferSource = LevelRenderHandler.getDelayedRender();
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES){
            Vec3 pos = event.getCamera().getPosition();
            stack.pushPose();
            stack.translate(-pos.x, -pos.y, -pos.z);
            for(ICustomParticleRender particle : particleList){
                particle.render(stack, bufferSource, partialTicks);
            }

            for(GenericParticle particle : behaviorParticleList.keySet()){
                behaviorParticleList.get(particle).render(particle, stack, bufferSource, partialTicks);
            }

            stack.popPose();
            particleList.clear();
            behaviorParticleList.clear();
            MATRIX4F = new Matrix4f(event.getModelViewMatrix());
            FOG_START = RenderSystem.getShaderFogStart();
        }

        if(!ShadersIntegration.shouldApply()){
            standardDelayedRender(event);
        }else{
            shadersDelayedRender(event);
        }
    }

    public static void standardDelayedRender(RenderLevelStageEvent event){
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER){
            Matrix4f last = new Matrix4f(RenderSystem.getModelViewMatrix());
            if(MATRIX4F != null) RenderSystem.getModelViewMatrix().set(MATRIX4F);
            for(RenderType renderType : TridotRenderTypes.translucentRenderTypes) getDelayedRender().endBatch(renderType);
            RenderSystem.getModelViewMatrix().set(last);
            for(RenderType renderType : TridotRenderTypes.translucentParticleRenderTypes) getDelayedRender().endBatch(renderType);
            if(MATRIX4F != null) RenderSystem.getModelViewMatrix().set(MATRIX4F);
            for(RenderType renderType : TridotRenderTypes.additiveRenderTypes) getDelayedRender().endBatch(renderType);
            RenderSystem.getModelViewMatrix().set(last);
            for(RenderType renderType : TridotRenderTypes.additiveParticleRenderTypes) getDelayedRender().endBatch(renderType);
        }
    }

    public static void shadersDelayedRender(RenderLevelStageEvent event){
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL){
            RenderSystem.setShaderFogStart(FOG_START);
            Matrix4fStack modelView = RenderSystem.getModelViewStack();
            modelView.pushMatrix();
            modelView.set(Objects.requireNonNullElseGet(MATRIX4F, event::getModelViewMatrix));
            RenderSystem.applyModelViewMatrix();
            for(RenderType renderType : TridotRenderTypes.translucentParticleRenderTypes) getDelayedRender().endBatch(renderType);
            for(RenderType renderType : TridotRenderTypes.translucentRenderTypes) getDelayedRender().endBatch(renderType);
            for(RenderType renderType : TridotRenderTypes.additiveParticleRenderTypes) getDelayedRender().endBatch(renderType);
            for(RenderType renderType : TridotRenderTypes.additiveRenderTypes) getDelayedRender().endBatch(renderType);
            modelView.popMatrix();
            RenderSystem.applyModelViewMatrix();
            FogRenderer.setupNoFog();
        }
    }

    public static void copyDepthBuffer(RenderTarget tempRenderTarget){
        setupDepthBuffer();
        enableStencil();
        if(tempRenderTarget == null) return;
        RenderTarget mainRenderTarget = Minecraft.getInstance().getMainRenderTarget();
        tempRenderTarget.copyDepthFrom(mainRenderTarget);
        GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, mainRenderTarget.frameBufferId);
    }

    public static void setupDepthBuffer(){
        if(DEPTH_CACHE == null){
            DEPTH_CACHE = new TextureTarget(Minecraft.getInstance().getMainRenderTarget().width, Minecraft.getInstance().getMainRenderTarget().height, true, Minecraft.ON_OSX);
        }
    }

    public static void enableStencil(){
        if(Minecraft.getInstance().getMainRenderTarget().isStencilEnabled()){
            DEPTH_CACHE.enableStencil();
        }
    }

    public static void resize(int width, int height){
        if(DEPTH_CACHE != null){
            DEPTH_CACHE.resize(width, height, Minecraft.ON_OSX);
        }
    }

    public static MultiBufferSource.BufferSource getDelayedRender(){
        if(DELAYED_RENDER == null){
            SequencedMap<RenderType, ByteBufferBuilder> buffers = new LinkedHashMap<>();
            for(RenderType type : TridotRenderTypes.renderTypes){
                buffers.put(type, new ByteBufferBuilder(ModList.get().isLoaded("embeddium") || ModList.get().isLoaded("rubidium") || ModList.get().isLoaded("sodium") ? 2097152 : type.bufferSize()));
            }
            DELAYED_RENDER = MultiBufferSource.immediateWithBuffers(buffers, new ByteBufferBuilder(256));
        }
        return DELAYED_RENDER;
    }
}
