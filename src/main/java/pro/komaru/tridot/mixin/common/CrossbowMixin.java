package pro.komaru.tridot.mixin.common;

import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import pro.komaru.tridot.common.registry.item.types.*;

@Mixin(CrossbowItem.class)
public class CrossbowMixin{

    @Inject(method = "getChargeDuration", at = @At("RETURN"), cancellable = true)
    private static void getChargeDuration(ItemStack pCrossbowStack, LivingEntity shooter, CallbackInfoReturnable<Integer> cir){
        if(pCrossbowStack.getItem() instanceof ConfigurableCrossbow crossbow) cir.setReturnValue(crossbow.getCustomChargeDuration(pCrossbowStack));
    }
}
