package pro.komaru.tridot.client.model.item;

import net.minecraft.client.multiplayer.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import pro.komaru.tridot.common.registry.item.types.*;

import javax.annotation.*;
import java.util.*;

public class CrossbowItemOverrides extends CustomItemOverrides {
    public ArrayList<BakedModel> pullingModels = new ArrayList<>();
    public BakedModel arrowModel;
    public BakedModel fireworkModel;

    @Override
    public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        if (getPulling(stack, level, entity, seed) > 0) {
            float pull = getPull(stack, level, entity, seed);
            BakedModel model = pullingModels.get(0);
            if (pull >= 0.58f) {
                model = pullingModels.get(1);
            }
            if (pull >= 1f) {
                model = pullingModels.get(2);
            }
            return model;
        }

        if (getCharged(stack, level, entity, seed)) {
            boolean firework = getFirework(stack, level, entity, seed);
            BakedModel model = arrowModel;
            if (firework) model = fireworkModel;
            return model;
        }

        return originalModel;
    }

    public float getPull(ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
        if (entity == null) {
            return 0.0F;
        } else {
            int charge = stack.getItem() instanceof ConfigurableCrossbow crossbow ? crossbow.getCustomChargeDuration(stack) : CrossbowItem.getChargeDuration(stack, entity);
            return ConfigurableCrossbow.isCharged(stack) ? 0.0F : (float) (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / (float) charge;
        }
    }

    public float getPulling(ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
        return entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !ConfigurableCrossbow.isCharged(stack) ? 1.0F : 0.0F;
    }

    public boolean getCharged(ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
        return ConfigurableCrossbow.isCharged(stack);
    }

    public boolean getFirework(ItemStack stack, ClientLevel level, LivingEntity entity, int seed) {
        return ConfigurableCrossbow.isCharged(stack) && ConfigurableCrossbow.containsChargedProjectile(stack, Items.FIREWORK_ROCKET);
    }
}