package pro.komaru.tridot.common.registry.item.armor;

import net.minecraft.client.model.*;
import net.minecraft.core.*;
import net.minecraft.resources.*;
import net.minecraft.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.neoforge.client.extensions.common.*;
import pro.komaru.tridot.client.ClientTick;
import pro.komaru.tridot.client.model.armor.*;
import pro.komaru.tridot.common.registry.item.skins.*;

public class SkinableArmorItem extends PercentageArmorItem{
    public SkinableArmorItem(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties){
        super(pMaterial, pType, pProperties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel){
        ItemSkin skin = ItemSkin.itemSkin(stack);
        if(skin == null) return super.getArmorTexture(stack, entity, slot, layer, innerModel);
        return ResourceLocation.parse(skin.getArmorTexture(stack, entity, slot, innerModel ? "inner" : null));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void initializeClient(java.util.function.Consumer<IClientItemExtensions> consumer){
        consumer.accept(new IClientItemExtensions(){
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> original){
                ArmorModel model = getArmorModel(entity, itemStack, armorSlot, original);
                if(model != null) return model;

                return original;
            }
        });
    }

    public ArmorModel getArmorModel(LivingEntity entity, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> original){
        float partialTicks = ClientTick.mcPartialTick();
        float f = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float f1 = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
        float netHeadYaw = f1 - f;
        float netHeadPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        ArmorModel model;
        ItemSkin skin = ItemSkin.itemSkin(itemStack);
        if(skin != null){
            model = skin.getArmorModel(entity, itemStack, armorSlot, original);
            model.slot = type.getSlot();
            model.copyFromDefault(original);
            model.setupAnim(entity, entity.walkAnimation.position(partialTicks), entity.walkAnimation.speed(partialTicks), entity.tickCount + partialTicks, netHeadYaw, netHeadPitch);
            return model;
        }
        return null;
    }
}
