package pro.komaru.tridot;

import com.mojang.logging.*;
import net.mehvahdjukaar.dummmmmmy.*;
import net.minecraft.resources.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.event.entity.player.PlayerEvent.*;
import net.minecraftforge.fml.config.ModConfig.*;
import net.minecraftforge.registries.*;
import org.slf4j.*;
import pro.komaru.tridot.api.interfaces.*;
import pro.komaru.tridot.api.level.loot.conditions.LootConditionsRegistry;
import pro.komaru.tridot.api.networking.PacketHandler;
import pro.komaru.tridot.api.render.bossbars.*;
import pro.komaru.tridot.client.ClientTick;
import pro.komaru.tridot.client.cinema.CutsceneEvents;
import pro.komaru.tridot.client.gfx.*;
import pro.komaru.tridot.client.render.gui.overlay.*;
import pro.komaru.tridot.common.Events;
import pro.komaru.tridot.common.commands.*;
import pro.komaru.tridot.common.config.ClientConfig;
import pro.komaru.tridot.common.config.CommonConfig;
import pro.komaru.tridot.common.networking.proxy.ClientProxy;
import pro.komaru.tridot.common.networking.proxy.ISidedProxy;
import pro.komaru.tridot.common.networking.proxy.ServerProxy;
import pro.komaru.tridot.common.registry.EnchantmentsRegistry;
import pro.komaru.tridot.common.registry.TridotLootModifier;
import pro.komaru.tridot.common.registry.block.TridotBlockEntities;
import pro.komaru.tridot.common.registry.block.TridotBlocks;
import pro.komaru.tridot.common.registry.item.AttributeRegistry;
import pro.komaru.tridot.common.registry.item.skins.*;
import pro.komaru.tridot.common.registry.item.types.TestItem;
import net.minecraft.world.entity.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.entity.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.javafmlmod.*;

import java.util.*;

@Mod("tridot")
public class Tridot {
    public static final String ID = "tridot";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static UUID BASE_PROJECTILE_DAMAGE_UUID = UUID.fromString("5334b818-69d4-417e-b4b8-1869d4917e29");
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final RegistryObject<Item> TEST = ITEMS.register("test", () -> new TestItem(new Item.Properties().rarity(Rarity.EPIC)));

    public static final ISidedProxy PROXY = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    public Tridot(){
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EnchantmentsRegistry.register(eventBus);
        AttributeRegistry.register(eventBus);
        TridotBlocks.register(eventBus);
        TridotBlockEntities.register(eventBus);
        TridotParticles.register(eventBus);
        TridotLootModifier.register(eventBus);
        LootConditionsRegistry.init(eventBus);

        ModArgumentTypes.register(eventBus);
        ITEMS.register(eventBus);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> {
            forgeBus.addListener(OverlayHandler::tickInstances);
            forgeBus.addListener(OverlayHandler::renderInstances);
            forgeBus.addListener(OverlayRenderItem::onDrawScreenPost);
            forgeBus.addListener(ClientTick::clientTickEnd);
            TridotLibClient.clientInit();
            return new Object();
        });

        ModLoadingContext.get().registerConfig(Type.COMMON, CommonConfig.SPEC);
        ModLoadingContext.get().registerConfig(Type.CLIENT, ClientConfig.SPEC);
        eventBus.addListener(this::setup);
        eventBus.addListener(TridotLibClient::clientSetup);

        forgeBus.register(this);
        forgeBus.register(new CutsceneEvents());
        forgeBus.register(new Events());
    }

    public static ResourceLocation ofTridot(String path) {
        return new ResourceLocation(ID, path);
    }

    private void setup(final FMLCommonSetupEvent event){
        TridotBlocks.setFireBlock();
        PacketHandler.init();
        for(ItemSkin skin : SkinRegistryManager.getSkins()){
            skin.setupSkinEntries();
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents{

        @SubscribeEvent
        public static void attachAttribute(EntityAttributeModificationEvent event) {
            for(var type : event.getTypes()) {
                event.add(type, AttributeRegistry.PERCENT_ARMOR.get());
            }

            event.add(EntityType.PLAYER, AttributeRegistry.PROJECTILE_DAMAGE.get());
        }
    }
}