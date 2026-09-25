package pro.komaru.tridot;

import com.mojang.logging.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.*;
import net.neoforged.fml.common.*;
import net.neoforged.fml.config.*;
import net.neoforged.fml.event.lifecycle.*;
import net.neoforged.fml.loading.*;
import net.neoforged.neoforge.common.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.registries.*;
import org.slf4j.*;
import pro.komaru.tridot.api.capabilities.Capabilities;
import pro.komaru.tridot.api.level.loot.conditions.LootConditionsRegistry;
import pro.komaru.tridot.api.networking.PacketHandler;
import pro.komaru.tridot.client.cinema.CutsceneHelper;
import pro.komaru.tridot.client.gfx.*;
import pro.komaru.tridot.common.Events;
import pro.komaru.tridot.common.ServerTickHandler;
import pro.komaru.tridot.common.commands.*;
import pro.komaru.tridot.common.config.ClientConfig;
import pro.komaru.tridot.common.config.CommonConfig;
import pro.komaru.tridot.common.networking.proxy.ClientProxy;
import pro.komaru.tridot.common.networking.proxy.ISidedProxy;
import pro.komaru.tridot.common.networking.proxy.ServerProxy;
import pro.komaru.tridot.common.registry.TridotDataComponents;
import pro.komaru.tridot.common.registry.TridotLootModifier;
import pro.komaru.tridot.common.registry.block.TridotBlockEntities;
import pro.komaru.tridot.common.registry.block.TridotBlocks;
import pro.komaru.tridot.common.registry.item.AttributeRegistry;
import pro.komaru.tridot.common.registry.item.skins.*;
import pro.komaru.tridot.common.registry.item.types.TestItem;

@Mod(Tridot.ID)
public class Tridot {
    public static final String ID = "tridot";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation BASE_PROJECTILE_DAMAGE_ID = ofTridot("base_projectile_damage");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, ID);
    public static final DeferredHolder<Item, Item> TEST = ITEMS.register("test", () -> new TestItem(new Item.Properties().rarity(Rarity.EPIC)));

    public static final ISidedProxy PROXY = FMLEnvironment.dist.isClient() ? new ClientProxy() : new ServerProxy();

    public Tridot(IEventBus eventBus, ModContainer container){
        AttributeRegistry.register(eventBus);
        TridotDataComponents.register(eventBus);
        TridotBlocks.register(eventBus);
        TridotBlockEntities.register(eventBus);
        TridotParticles.register(eventBus);
        TridotLootModifier.register(eventBus);
        LootConditionsRegistry.init(eventBus);
        Capabilities.register(eventBus);

        ModArgumentTypes.register(eventBus);
        ITEMS.register(eventBus);

        IEventBus forgeBus = NeoForge.EVENT_BUS;

        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        eventBus.addListener(this::setup);
        eventBus.addListener(PacketHandler::register);

        if(FMLEnvironment.dist.isClient()){
            eventBus.addListener(TridotLibClient::clientSetup);
            TridotLibClient.registerGameBusListeners(forgeBus);
        }

        ServerTickHandler.preInit(forgeBus);
        forgeBus.register(new CutsceneHelper());
        forgeBus.register(new Events());
    }

    public static ResourceLocation ofTridot(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    private void setup(final FMLCommonSetupEvent event){
        TridotBlocks.setFireBlock();
        for(ItemSkin skin : SkinRegistryManager.getSkins()){
            skin.setupSkinEntries();
        }
    }

    @EventBusSubscriber(modid = Tridot.ID, bus = EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents{

        @SubscribeEvent
        public static void attachAttribute(EntityAttributeModificationEvent event) {
            for(var type : event.getTypes()) {
                event.add(type, AttributeRegistry.PERCENT_ARMOR);
            }

            event.add(EntityType.PLAYER, AttributeRegistry.PROJECTILE_DAMAGE);
        }
    }
}