package pro.komaru.tridot.common.registry.block.entity;

import pro.komaru.tridot.common.networking.packets.BlockEntityUpdate;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import org.jetbrains.annotations.*;
import pro.komaru.tridot.util.Tmp;
import pro.komaru.tridot.util.math.ArcRandom;

public abstract class BlockEntityBase extends BlockEntity{

    public ArcRandom random = Tmp.rnd;

    public BlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState blockState){
        super(type, pos, blockState);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket(){
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider lookupProvider){
        super.onDataPacket(net, packet, lookupProvider);
        handleUpdateTag(packet.getTag(), lookupProvider);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries){
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void setChanged(){
        super.setChanged();
        if(level != null && !level.isClientSide){
            BlockEntityUpdate.packet(this);
        }
    }
}
