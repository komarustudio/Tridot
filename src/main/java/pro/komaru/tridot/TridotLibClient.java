package pro.komaru.tridot;

import net.minecraft.client.*;
import net.minecraft.client.resources.language.*;
import net.minecraft.resources.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.*;
import net.neoforged.fml.event.lifecycle.*;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.*;
import pro.komaru.tridot.api.interfaces.OverlayRenderItem;
import pro.komaru.tridot.client.*;
import pro.komaru.tridot.client.compatibility.ShadersIntegration;
import pro.komaru.tridot.client.gfx.*;
import pro.komaru.tridot.client.render.gui.overlay.OverlayHandler;
import pro.komaru.tridot.client.render.gui.particle.*;
import pro.komaru.tridot.client.sound.LoopedSoundInstance;
import pro.komaru.tridot.client.sound.TridotSoundInstance;
import pro.komaru.tridot.client.tooltip.*;
import pro.komaru.tridot.common.config.*;
import pro.komaru.tridot.common.registry.item.*;
import pro.komaru.tridot.common.registry.item.components.*;
import pro.komaru.tridot.common.registry.item.components.client.*;

import static pro.komaru.tridot.Tridot.*;

public class TridotLibClient{
    public static LoopedSoundInstance BOSS_MUSIC;
    public static TridotSoundInstance COOLDOWN_SOUND;
    public static TridotSoundInstance DUNGEON_MUSIC_INSTANCE;
    public static final ResourceLocation ARMOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_full");

    public static void clientSetup(final FMLClientSetupEvent event){
        ShadersIntegration.init();
    }

    public static void registerGameBusListeners(IEventBus gameBus){
        gameBus.addListener(OverlayHandler::tickInstances);
        gameBus.addListener(OverlayHandler::renderInstances);
        gameBus.addListener(OverlayRenderItem::onDrawScreenPost);
        gameBus.addListener(ClientTick::clientTickEnd);
        gameBus.register(new ClientEvents());
    }

    @EventBusSubscriber(modid = Tridot.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class RegistryEvents {
        private static float lastArmorValue = -1.0F;
        private static String tridot$cachedArmorText = "";

        @SubscribeEvent
        public static void registerComponents(RegisterClientTooltipComponentFactoriesEvent e) {
            e.register(SeparatorComponent.class, c -> SeparatorClientComponent.create(c.component()));
            e.register(LineSeparatorComponent.class, c -> LineSeparatorClientComponent.create());
            e.register(AbilityComponent.class, c -> AbilityClientComponent.create(c.component(), c.icon(), c.paddingTop(), c.textPaddingTop(), c.iconSize()));
            e.register(ObjectComponent.class, c -> ObjectClientComponent.create(c.component(), c.icon(), c.paddingTop(), c.textPaddingTop()));
            e.register(TextComponent.class, c -> TextClientComponent.create(c.component()));
            e.register(EffectsListComponent.class, c -> EffectListClientComponent.create(c.list(), c.component()));
            e.register(EmptyComponent.class, c -> EmptyClientComponent.create(c.height()));
        }

        @SubscribeEvent
        public static void registerOverlays(RegisterGuiLayersEvent event){
            event.registerAboveAll(Tridot.ofTridot("boss_bars"), BossBarsOverlay.INSTANCE);
            event.registerAbove(VanillaGuiLayers.ARMOR_LEVEL, Tridot.ofTridot("tridot_armor"), (guiGraphics, deltaTracker) -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) return;
                float currentArmor = (float) mc.player.getAttributeValue(AttributeRegistry.PERCENT_ARMOR);
                if (currentArmor > 0 && CommonConfig.PERCENT_ARMOR.get()){
                    int screenWidth = guiGraphics.guiWidth();
                    int screenHeight = guiGraphics.guiHeight();
                    int left = screenWidth / 2 - 91;
                    int top = screenHeight - 39;

                    guiGraphics.blitSprite(ARMOR_FULL_SPRITE, left + ClientConfig.PERCENT_ARMOR_X_OFFSET.get(), top + ClientConfig.PERCENT_ARMOR_Y_OFFSET.get(), 9, 9);
                    if(Math.abs(currentArmor - lastArmorValue) > 0.01F){
                        String formattedValue = String.format("%.1f%%", currentArmor);
                        tridot$cachedArmorText = I18n.get("tooltip.tridot.value", formattedValue);
                        lastArmorValue = currentArmor;
                    }

                    guiGraphics.drawString(mc.font, tridot$cachedArmorText, left + ClientConfig.PERCENT_ARMOR_X_OFFSET.get() + 10, top + ClientConfig.PERCENT_ARMOR_Y_OFFSET.get(), 0xFFFFFF);
                }
            });
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            ParticleEmitterHandler.registerEmitters(event);
        }

        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
        public static void registerParticleFactory(RegisterParticleProvidersEvent event) {
            TridotScreenParticles.registerParticleFactory(event);
        }

        @SubscribeEvent
        public static void registerAttributeModifiers(FMLClientSetupEvent event){
            TooltipModifierHandler.add(BASE_PROJECTILE_DAMAGE_ID);
        }
    }
}
