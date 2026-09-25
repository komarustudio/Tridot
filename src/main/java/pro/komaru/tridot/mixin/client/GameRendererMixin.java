package pro.komaru.tridot.mixin.client;

import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import org.joml.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import pro.komaru.tridot.client.gfx.postprocess.*;
import pro.komaru.tridot.client.render.*;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin{

    @Inject(at = @At(value = "RETURN"), method = "renderItemInHand")
    private void tridot$renderItemInHand(Camera camera, float partialTick, Matrix4f projectionMatrix, CallbackInfo ci){
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
    public void tridot$renderScreenPostProcess(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci){
        PostProcessHandler.onScreenRender((GameRenderer)(Object)this, deltaTracker.getGameTimeDeltaPartialTick(false), 0L, renderLevel);
    }

    @Inject(at = @At(value = "RETURN"), method = "render")
    public void tridot$renderWindowPostProcess(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci){
        PostProcessHandler.onWindowRender((GameRenderer)(Object)this, deltaTracker.getGameTimeDeltaPartialTick(false), 0L, renderLevel);
    }
}
