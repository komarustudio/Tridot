package pro.komaru.tridot.api.render.animation;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.*;
import net.minecraft.client.player.*;
import net.minecraft.client.renderer.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.*;

public class ItemAnimation{
    @OnlyIn(Dist.CLIENT)
    public void setupAnim(HumanoidModel model, Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){

    }

    @OnlyIn(Dist.CLIENT)
    public void setupAnimRight(HumanoidModel model, Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){

    }

    @OnlyIn(Dist.CLIENT)
    public void setupAnimLeft(HumanoidModel model, Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){

    }

    @OnlyIn(Dist.CLIENT)
    public void renderArmWithItem(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight){

    }

    @OnlyIn(Dist.CLIENT)
    public void renderArmWithItem(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight){

    }

    public boolean isOnlyItemUse(){
        return true;
    }

    public static boolean isRightHand(Player player, InteractionHand hand){
        if(player.getMainArm() == HumanoidArm.LEFT){
            return hand != InteractionHand.MAIN_HAND;
        }
        return hand == InteractionHand.MAIN_HAND;
    }

    public static boolean isSameHand(HumanoidArm mainArm, HumanoidArm arm, InteractionHand hand){
        if(mainArm == arm){
            return hand == InteractionHand.MAIN_HAND;
        }
        return hand == InteractionHand.OFF_HAND;
    }

    public static boolean isItemUse(LivingEntity player){
        return (player.isUsingItem() && player.getUseItemRemainingTicks() > 0);
    }
}
