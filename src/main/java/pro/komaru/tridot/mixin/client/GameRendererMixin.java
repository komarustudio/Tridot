package pro.komaru.tridot.mixin.client;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import pro.komaru.tridot.client.gfx.postprocess.*;
import pro.komaru.tridot.client.gfx.postprocess.bloom.BloomRenderer;
import pro.komaru.tridot.client.render.*;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin{

    @Shadow
    public abstract void renderLevel(float pPartialTicks, long pFinishTimeNano, PoseStack pPoseStack);

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void tridot$bloomBegin(float partialTick, long finishNanos, PoseStack poseStack, CallbackInfo ci) {
        BloomRenderer.beginFrame();
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void tridot$bloomAfterWorld(float partialTick, long finishNanos, PoseStack poseStack, CallbackInfo ci) {
        if (BloomRenderer.hasPendingWork()) {
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            BloomRenderer.flushAndComposite();
        }
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void tridot$bloomAfterHand(float partialTick, long finishNanos, PoseStack poseStack, CallbackInfo ci) {
        if (BloomRenderer.hasPendingWork()) {
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            BloomRenderer.flushAndComposite();
        }
    }

    @Inject(at = @At(value = "RETURN"), method = "renderItemInHand")
    private void tridot$renderItemInHand(PoseStack pPoseStack, Camera pActiveRenderInfo, float pPartialTicks, CallbackInfo ci){
        for(RenderBuilder builder : TridotRenderTypes.customItemRenderBuilderFirst){
            builder.endBatch();
        }
        TridotRenderTypes.customItemRenderBuilderFirst.clear();
    }

    @Inject(method = "resize", at = @At(value = "HEAD"))
    public void tridot$injectionResizeListener(int width, int height, CallbackInfo ci){
        LevelRenderHandler.resize(width, height);
        PostProcessHandler.resize(width, height);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;bindWrite(Z)V"), method = "render")
    public void tridot$renderScreenPostProcess(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci){
        PostProcessHandler.onScreenRender((GameRenderer)(Object)this, partialTicks, nanoTime, renderLevel);
    }

    @Inject(at = @At(value = "RETURN"), method = "render")
    public void tridot$renderWindowPostProcess(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci){
        PostProcessHandler.onWindowRender((GameRenderer)(Object)this, partialTicks, nanoTime, renderLevel);
    }
}
