package pro.komaru.tridot;

import net.minecraft.client.*;
import net.minecraft.client.resources.language.*;
import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.*;
import net.minecraftforge.common.*;
import net.minecraftforge.fml.common.*;
import net.minecraft.resources.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.event.lifecycle.*;
import pro.komaru.tridot.client.*;
import pro.komaru.tridot.client.gfx.*;
import pro.komaru.tridot.client.render.gui.particle.*;
import pro.komaru.tridot.client.tooltip.*;
import pro.komaru.tridot.client.sound.LoopedSoundInstance;
import pro.komaru.tridot.client.sound.TridotSoundInstance;
import pro.komaru.tridot.client.compatibility.ShadersIntegration;
import pro.komaru.tridot.common.config.*;
import pro.komaru.tridot.common.registry.item.*;
import pro.komaru.tridot.common.registry.item.components.*;
import pro.komaru.tridot.common.registry.item.components.client.*;

import static pro.komaru.tridot.Tridot.*;
import static pro.komaru.tridot.common.Events.GUI_ICONS_LOCATION;

public class TridotLibClient{
    public static LoopedSoundInstance BOSS_MUSIC;
    public static TridotSoundInstance COOLDOWN_SOUND;
    public static TridotSoundInstance DUNGEON_MUSIC_INSTANCE;

    public static void clientSetup(final FMLClientSetupEvent event){
        ShadersIntegration.init();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class RegistryEvents {
        private static float lastArmorValue = -1.0F;
        private static String tridot$cachedArmorText = "";

        @SubscribeEvent
        public static void registerComponents(RegisterClientTooltipComponentFactoriesEvent e) {
            e.register(SeparatorComponent.class, c -> SeparatorClientComponent.create(c.component()));
            e.register(LineSeparatorComponent.class, c -> LineSeparatorClientComponent.create());
            e.register(AbilityComponent.class, c -> AbilityClientComponent.create(c.component(), c.icon(), c.paddingTop(), c.iconSize()));
            e.register(TextComponent.class, c -> TextClientComponent.create(c.component()));
            e.register(EffectsListComponent.class, c -> EffectListClientComponent.create(c.list(), c.component()));
            e.register(EmptyComponent.class, c -> EmptyClientComponent.create(c.height()));
        }

        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event){
            event.registerAboveAll("boss_bars", BossBarsOverlay.INSTANCE);
            event.registerAbove(VanillaGuiOverlay.ARMOR_LEVEL.id(), "tridot_armor", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) return;
                float currentArmor = (float) mc.player.getAttributeValue(AttributeRegistry.PERCENT_ARMOR.get());
                if (currentArmor > 0 && CommonConfig.PERCENT_ARMOR.get()){
                    int left = screenWidth / 2 - 91;
                    int top = screenHeight - 39;

                    guiGraphics.blit(GUI_ICONS_LOCATION, left + ClientConfig.PERCENT_ARMOR_X_OFFSET.get(), top + ClientConfig.PERCENT_ARMOR_Y_OFFSET.get(), 34, 9, 9, 9);
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

        @SubscribeEvent
        public static void registerParticleFactory(RegisterParticleProvidersEvent event) {
            TridotScreenParticles.registerParticleFactory(event);
        }

        @SubscribeEvent
        public static void registerAttributeModifiers(FMLClientSetupEvent event){
            TooltipModifierHandler.add(BASE_PROJECTILE_DAMAGE_UUID);
        }
    }
}
