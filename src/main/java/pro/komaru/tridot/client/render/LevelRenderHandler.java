package pro.komaru.tridot.client.render;

import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.*;
import org.joml.*;
import org.lwjgl.opengl.*;
import pro.komaru.tridot.client.compatibility.*;
import pro.komaru.tridot.client.gfx.particle.GenericParticle;
import pro.komaru.tridot.client.gfx.particle.ICustomParticleRender;
import pro.komaru.tridot.client.gfx.particle.behavior.ICustomBehaviorParticleRender;

import java.util.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelRenderHandler{
    public static Matrix4f MATRIX4F = null;
    static MultiBufferSource.BufferSource DELAYED_RENDER = null;
    public static RenderTarget DEPTH_CACHE;
    public static float FOG_START = 0;
    public static List<ICustomParticleRender> particleList = new ArrayList<>();
    public static Map<GenericParticle, ICustomBehaviorParticleRender> behaviorParticleList = new HashMap<>();

    public static Matrix4f getCameraMatrix(Camera camera){
        return new Matrix4f()
                .rotation(Axis.XP.rotationDegrees(camera.getXRot()))
                .rotate(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLevelRender(RenderLevelStageEvent event){
        PoseStack stack = event.getPoseStack();
        float partialTicks = event.getPartialTick();
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

            MATRIX4F = new Matrix4f(stack.last().pose());
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
            Matrix4f prevProjection = RenderSystem.getProjectionMatrix();
            RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), VertexSorting.DISTANCE_TO_ORIGIN);

            Matrix4f cameraMatrix = MATRIX4F != null ? MATRIX4F : getCameraMatrix(event.getCamera());
            renderBatches(cameraMatrix);

            RenderSystem.setProjectionMatrix(prevProjection, VertexSorting.DISTANCE_TO_ORIGIN);
        }
    }

    public static void shadersDelayedRender(RenderLevelStageEvent event){
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL){
            RenderSystem.setShaderFogStart(FOG_START);

            Matrix4f prevProjection = RenderSystem.getProjectionMatrix();
            RenderSystem.setProjectionMatrix(event.getProjectionMatrix(), VertexSorting.DISTANCE_TO_ORIGIN);

            Matrix4f cameraMatrix = MATRIX4F != null ? MATRIX4F : getCameraMatrix(event.getCamera());
            renderBatches(cameraMatrix);

            RenderSystem.setProjectionMatrix(prevProjection, VertexSorting.DISTANCE_TO_ORIGIN);
            FogRenderer.setupNoFog();
        }
    }

    private static void renderBatches(Matrix4f cameraMatrix){
        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.applyModelViewMatrix();
        for(RenderType renderType : TridotRenderTypes.translucentRenderTypes) {
            getDelayedRender().endBatch(renderType);
        }
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.getModelViewStack().mulPoseMatrix(cameraMatrix);
        RenderSystem.applyModelViewMatrix();
        for(RenderType renderType : TridotRenderTypes.translucentParticleRenderTypes) {
            getDelayedRender().endBatch(renderType);
        }
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.applyModelViewMatrix();
        for(RenderType renderType : TridotRenderTypes.additiveRenderTypes) {
            getDelayedRender().endBatch(renderType);
        }
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.getModelViewStack().pushPose();
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.getModelViewStack().mulPoseMatrix(cameraMatrix);
        RenderSystem.applyModelViewMatrix();
        for(RenderType renderType : TridotRenderTypes.additiveParticleRenderTypes) {
            getDelayedRender().endBatch(renderType);
        }
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
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
            Map<RenderType, BufferBuilder> buffers = new HashMap<>();
            for(RenderType type : TridotRenderTypes.renderTypes){
                buffers.put(type, new BufferBuilder(ModList.get().isLoaded("embeddium") || ModList.get().isLoaded("rubidium") ? 2097152 : type.bufferSize()));
            }
            DELAYED_RENDER = MultiBufferSource.immediateWithBuffers(buffers, new BufferBuilder(256));
        }
        return DELAYED_RENDER;
    }
}
