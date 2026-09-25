package pro.komaru.tridot.mixin.common;

import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import pro.komaru.tridot.api.interfaces.*;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin{

    @Inject(at = @At("HEAD"), method = "updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z", cancellable = true)
    private static void tridot$updateCustomBlockEntityTag(Level level, Player player, BlockPos pos, ItemStack stack, CallbackInfoReturnable<Boolean> cir){
        if(stack.getItem() instanceof ICustomBlockEntityDataItem customBlockEntityDataItem){
            if(level.getServer() == null){
                cir.setReturnValue(false);
                return;
            }

            CustomData customData = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
            CompoundTag tileNbt = customData.isEmpty() ? new CompoundTag() : customData.copyTag();
            CompoundTag customized = customBlockEntityDataItem.getCustomBlockEntityData(stack, tileNbt);
            if(customized == null || customized.isEmpty()){
                cir.setReturnValue(false);
                return;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity != null){
                if(level.isClientSide || !blockEntity.onlyOpCanSetNbt() || player != null && player.canUseGameMasterBlocks()){
                    cir.setReturnValue(CustomData.of(customized).loadInto(blockEntity, level.registryAccess()));
                    return;
                }
            }

            cir.setReturnValue(false);
        }
    }
}
