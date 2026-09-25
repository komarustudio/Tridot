package pro.komaru.tridot.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import pro.komaru.tridot.client.tooltip.*;

import java.util.*;

@Mixin(ItemStack.class)
public class ItemStackMixin{

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/util/AttributeUtil;addAttributeTooltips(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;Lnet/neoforged/neoforge/common/util/AttributeTooltipContext;)V", remap = false))
    public void tridot$getTooltip(Item.TooltipContext context, @Nullable Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir, @Local(ordinal = 0) List<Component> list){
        TooltipModifierHandler.attributeTooltipSize = list.size();
    }
}
