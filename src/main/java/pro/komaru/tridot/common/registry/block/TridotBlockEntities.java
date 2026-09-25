package pro.komaru.tridot.common.registry.block;

import net.minecraft.core.registries.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType.*;
import pro.komaru.tridot.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.*;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.registries.*;
import pro.komaru.tridot.client.model.render.entity.*;
import pro.komaru.tridot.common.registry.block.chest.TridotChestBlock;
import pro.komaru.tridot.common.registry.block.chest.TridotTrappedChestBlock;
import pro.komaru.tridot.common.registry.block.entity.TridotChestBlockEntity;
import pro.komaru.tridot.common.registry.block.entity.TridotTrappedChestBlockEntity;
import pro.komaru.tridot.common.registry.block.sign.*;

import java.util.*;

public class TridotBlockEntities{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Tridot.ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CustomSignBlockEntity>> SIGN = BLOCK_ENTITIES.register("sign", () -> BlockEntityType.Builder.of(CustomSignBlockEntity::new, TridotBlocks.getBlocks(CustomStandingSignBlock.class, CustomWallSignBlock.class)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CustomHangingSignBlockEntity>> HANGING_SIGN = BLOCK_ENTITIES.register("hanging_sign", () -> BlockEntityType.Builder.of(CustomHangingSignBlockEntity::new, TridotBlocks.getBlocks(CustomCeilingHangingSignBlock.class, CustomWallHangingSignBlock.class)).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TridotChestBlockEntity>> CHEST_BLOCK_ENTITY = BLOCK_ENTITIES.register("mod_chest", () -> {
        ArrayList<Block> chestBlocks = new ArrayList<>();
        for(var block : BuiltInRegistries.BLOCK.entrySet()) {
            if(block.getValue() instanceof TridotChestBlock chestBlock && chestBlock.autoReg) {
                chestBlocks.add(chestBlock) ;
            }
        }

        return Builder.of(TridotChestBlockEntity::new, chestBlocks.toArray(new Block[0])).build(null);
    });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TridotTrappedChestBlockEntity>> TRAPPED_CHEST_BLOCK_ENTITY = BLOCK_ENTITIES.register("mod_trapped_chest", () -> {
        ArrayList<Block> chestBlocks = new ArrayList<>();
        for(var block : BuiltInRegistries.BLOCK.entrySet()) {
            if(block.getValue() instanceof TridotTrappedChestBlock chestBlock && chestBlock.autoReg) {
                chestBlocks.add(chestBlock) ;
            }
        }

        return Builder.of(TridotTrappedChestBlockEntity::new, chestBlocks.toArray(new Block[0])).build(null);
    });


    public static void register(IEventBus eventBus){
        BLOCK_ENTITIES.register(eventBus);
    }

    @EventBusSubscriber(modid = Tridot.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientRegistryEvents{
        @SubscribeEvent
        public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event){
            event.registerBlockEntityRenderer(SIGN.get(), SignRenderer::new);
            event.registerBlockEntityRenderer(HANGING_SIGN.get(), HangingSignRenderer::new);
            event.registerBlockEntityRenderer(CHEST_BLOCK_ENTITY.get(), TridotChestRender::new);
            event.registerBlockEntityRenderer(TRAPPED_CHEST_BLOCK_ENTITY.get(), TridotTrappedChestRender::new);
        }
    }
}
