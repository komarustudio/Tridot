package pro.komaru.tridot.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pro.komaru.tridot.common.registry.item.types.ConfiguredShield;

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

                    graphics.renderFakeItem(useItem, centerX - barWidth, centerY + 25);
                    graphics.fill(centerX - barWidth / 2, centerY + 25, centerX + barWidth / 2, centerY + 27, 0x80000000);
                    graphics.fill(centerX - barWidth / 2, centerY + 25, centerX - barWidth / 2 + currentWidth, centerY + 27, 0xFF00FF00);
                }
            }
        }
    }
}
