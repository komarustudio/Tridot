package pro.komaru.tridot.common.registry.block.sign;

import net.minecraft.core.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;

import javax.annotation.*;

public class CustomStandingSignBlock extends StandingSignBlock{
    public CustomStandingSignBlock(Properties properties, WoodType type){
        super(type, properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
        return new CustomSignBlockEntity(pos, state);
    }
}
