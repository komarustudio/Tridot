package pro.komaru.tridot.api.interfaces;

import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.core.component.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.fml.*;
import net.neoforged.neoforge.client.event.*;

public interface OverlayRenderItem{
    ResourceLocation getTexture();

    @OnlyIn(Dist.CLIENT)
    void render(CompoundTag tag, GuiGraphics gui, int offsetX, int offsetY);

    @OnlyIn(Dist.CLIENT)
    default void render(ItemStack stack, GuiGraphics gui, int offsetX, int offsetY){
        render(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(), gui, offsetX, offsetY);
    }

    default boolean toRender(){
        return !Minecraft.getInstance().player.isSpectator();
    }

    default int mainHandOffset(){
        return 0;
    }

    default int offhandOffset(){
        return 25;
    }

    static boolean isEmbeddiumPlusLoaded(){
        return ModList.get().isLoaded("embeddiumplus");
    }

    @OnlyIn(Dist.CLIENT)
    static void onDrawScreenPost(RenderGuiLayerEvent.Post event){
        Minecraft mc = Minecraft.getInstance();
        GuiGraphics gui = event.getGuiGraphics();
        boolean mainFlag = !mc.player.getMainHandItem().isEmpty() && mc.player.getMainHandItem().getItem() instanceof OverlayRenderItem;
        if(mainFlag && mc.player.getMainHandItem().getItem() instanceof OverlayRenderItem item){
            if(item.toRender()){
                gui.pose().pushPose();
                gui.pose().translate(0, isEmbeddiumPlusLoaded() ? 10 : 0, -200);
                item.render(mc.player.getMainHandItem(), gui, 0, 0);
                gui.pose().popPose();
            }
        }

        if(!mc.player.getOffhandItem().isEmpty() && mc.player.getOffhandItem().getItem() instanceof OverlayRenderItem item){
            if(item.toRender()){
                gui.pose().pushPose();
                gui.pose().translate(0, isEmbeddiumPlusLoaded() ? 10 : 0, -200);
                item.render(mc.player.getOffhandItem(), gui, mainFlag ? item.offhandOffset() : item.mainHandOffset(), 0);
                gui.pose().popPose();
            }
        }
    }
}
