package pro.komaru.tridot.common.registry.block.entity;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;

import javax.annotation.*;

public abstract class NameableExposedBlockSimpleInventory extends ExposedBlockSimpleInventory implements MenuProvider, Nameable{

    @Nullable
    public Component name;

    public NameableExposedBlockSimpleInventory(BlockEntityType<?> type, BlockPos pos, BlockState blockState){
        super(type, pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries){
        super.loadAdditional(tag, registries);
        if(tag.contains("CustomName", 8)){
            this.name = Component.Serializer.fromJson(tag.getString("CustomName"), registries);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries){
        super.saveAdditional(tag, registries);
        if(this.name != null){
            tag.putString("CustomName", Component.Serializer.toJson(this.name, registries));
        }
    }

    @Override
    public Component getName(){
        return name;
    }

    public void setCustomName(Component pName){
        this.name = pName;
    }

    public Component getDefaultName(){
        return Component.empty();
    }

    @Override
    public Component getDisplayName(){
        return this.name != null ? this.name : this.getDefaultName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player){
        return null;
    }
}
