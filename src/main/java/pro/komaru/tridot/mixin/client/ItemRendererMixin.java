package pro.komaru.tridot.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.komaru.tridot.client.gfx.postprocess.bloom.BloomItemModels;
import pro.komaru.tridot.client.gfx.postprocess.bloom.BloomRenderer;
import pro.komaru.tridot.client.render.TridotRenderTypes;
import pro.komaru.tridot.common.config.ClientConfig;

/** Renders inline {@code "tridot_bloom"} textures or {@code models/item/<id>_bloom.json} into the bloom mask. */
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    public abstract void renderModelLists(BakedModel model, ItemStack stack, int light, int overlay, PoseStack poseStack, VertexConsumer buffer);

    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("TAIL"))
    private void tridot$renderBloom(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (!ClientConfig.BLOOM.get() || stack.isEmpty() || context == ItemDisplayContext.GUI || !BloomRenderer.isActive()) return;

        BakedModel standalone = BloomItemModels.get(stack.getItem());
        if (!BloomItemModels.hasInlineBlooms() && standalone == null) return;

        poseStack.pushPose();
        BakedModel activeModel = ForgeHooksClient.handleCameraTransforms(poseStack, model, context, leftHand);
        BakedModel bloom = BloomItemModels.getForModel(activeModel);
        if (bloom == null && activeModel != model) {
            bloom = BloomItemModels.getForModel(model);
        }

        if (bloom == null) {
            if (standalone == null) {
                poseStack.popPose();
                return;
            }

            poseStack.popPose();
            poseStack.pushPose();
            bloom = ForgeHooksClient.handleCameraTransforms(poseStack, standalone, context, leftHand);
        }

        poseStack.translate(-0.5F, -0.5F, -0.5F);
        VertexConsumer consumer = BloomRenderer.bufferSource().getBuffer(TridotRenderTypes.bloomItem());
        for (BakedModel pass : bloom.getRenderPasses(stack, true)) {
            this.renderModelLists(pass, stack, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, consumer);
        }

        poseStack.popPose();
    }
}
