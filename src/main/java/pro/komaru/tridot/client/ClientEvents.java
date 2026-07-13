package pro.komaru.tridot.client;

import com.mojang.datafixers.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.resources.language.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.tooltip.*;
import net.minecraft.world.item.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.*;
import net.minecraftforge.event.*;
import net.minecraftforge.eventbus.api.*;
import org.spongepowered.asm.mixin.*;
import pro.komaru.tridot.client.gfx.postprocess.*;
import pro.komaru.tridot.client.model.render.item.bow.*;
import pro.komaru.tridot.client.render.gui.particle.*;
import pro.komaru.tridot.client.render.screenshake.*;
import pro.komaru.tridot.common.config.*;
import pro.komaru.tridot.common.registry.item.*;
import pro.komaru.tridot.common.registry.item.components.*;
import pro.komaru.tridot.common.registry.item.types.ConfiguredShield;
import pro.komaru.tridot.util.struct.data.*;

import java.util.*;

import static pro.komaru.tridot.common.Events.GUI_ICONS_LOCATION;

public class ClientEvents {

    @SubscribeEvent
    public static void onRenderCrosshair(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        if (player.isUsingItem()) {
            ItemStack useItem = player.getUseItem();
            if (useItem.getItem() instanceof ConfiguredShield shield && shield.canParry) {
                int ticksUsing = player.getTicksUsingItem();
                if (ticksUsing <= shield.parryWindow) {
                    GuiGraphics graphics = event.getGuiGraphics();
                    int screenWidth = event.getWindow().getGuiScaledWidth();
                    int screenHeight = event.getWindow().getGuiScaledHeight();

                    int centerX = screenWidth / 2;
                    int centerY = screenHeight / 2;

                    int barWidth = 15;
                    int currentWidth = (int) (barWidth * (1.0f - ((float)ticksUsing / shield.parryWindow)));

                    graphics.renderFakeItem(useItem, centerX - barWidth, centerY);
                    graphics.fill(centerX - barWidth / 2, centerY + 25, centerX + barWidth / 2, centerY + 27, 0x80000000);
                    graphics.fill(centerX - barWidth / 2, centerY + 25, centerX - barWidth / 2 + currentWidth, centerY + 27, 0xFF00FF00);
                }
            }
        }
    }

    @SubscribeEvent
    public static void handleArmorLevelOverlay(RenderGuiOverlayEvent.Pre e){
        if (e.getOverlay() == VanillaGuiOverlay.ARMOR_LEVEL.type() && CommonConfig.PERCENT_ARMOR.get()) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onTooltipGatherComponents(RenderTooltipEvent.GatherComponents event) {
        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        if (event.getItemStack().getItem() instanceof TooltipComponentItem componentItem) {
            Seq<TooltipComponent> components = componentItem.getTooltips(event.getItemStack());
            int insertIndex = 1;
            int componentsCount = 0;
            for (TooltipComponent component : components){
                if(component instanceof AbilityComponent) componentsCount++;
            }

            if (componentsCount > 2 && !Screen.hasShiftDown()) {
                elements.add(insertIndex, Either.right(new TextComponent(Component.translatable("tooltip.tridot.shift_for_details", Component.translatable("key.keyboard.left.shift").getString()))));
                return;
            }

            for (TooltipComponent component : components) {
                elements.add(insertIndex, Either.right(component));
                insertIndex++;
            }
        }
    }

    @SubscribeEvent
    public void onDisconnect(ClientPlayerNetworkEvent.LoggingOut e){
        BossBarsOverlay.reset();
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event){
        Minecraft minecraft = Minecraft.getInstance();
        ClientTick.clientTickEnd(event);
        if(event.phase == TickEvent.Phase.END){
            if(minecraft.isPaused()) return;
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            ScreenshakeHandler.clientTick(camera);
            PostProcessHandler.tick();
            ScreenParticleHandler.tickParticles();
        }
    }

    /*@SubscribeEvent
    public void render(RenderGuiEvent event) {
        BaseDrawer draw = new BaseDrawer(event.getGuiGraphics(), event.getGuiGraphics().pose(), "tridot");

        draw.color(Col.red);
        draw.rect("particle/skull",100f,100f, 2f, 2f, ClientTick.getTotal());
    }*/

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event){
        ClientTick.renderTick(event);
        ScreenParticleHandler.renderTick(event);
    }

    @SubscribeEvent
    public void getFovModifier(ComputeFovModifierEvent event){
        Player player = event.getPlayer();
        ItemStack itemStack = player.getUseItem();
        if(player.isUsingItem()){
            for(Item item : BowHandler.getBows()){
                if(itemStack.is(item)){
                    float f = event.getFovModifier();
                    if(f != event.getNewFovModifier()) f = event.getNewFovModifier();
                    float f1 = computeFov(player);

                    f *= 1.0F - f1 * 0.15F;
                    event.setNewFovModifier((float)Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0F, f));
                }
            }
        }

        ScreenshakeHandler.fovTick(event);
    }

    private float computeFov(Player player) {
        int i = player.getTicksUsingItem();
        float f1 = (float)i / 20.0F;
        if(f1 > 1.0F){
            f1 = 1.0F;
        }else {
            f1 *= f1;
        }

        return f1;
    }
}
