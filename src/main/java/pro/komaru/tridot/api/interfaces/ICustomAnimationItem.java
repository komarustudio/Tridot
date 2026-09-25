package pro.komaru.tridot.api.interfaces;

import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.api.render.animation.*;

public interface ICustomAnimationItem{
    @OnlyIn(Dist.CLIENT)
    ItemAnimation getAnimation(ItemStack stack);
}
