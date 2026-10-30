package pro.komaru.tridot.mixin.client;

import net.minecraft.client.*;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import pro.komaru.tridot.api.interfaces.*;
import pro.komaru.tridot.client.networking.*;

@Mixin(ItemCooldowns.class)
public class ItemCooldownsMixin {

    @Inject(at = @At("TAIL"), method = "onCooldownEnded")
    private void tridot$onCooldownEnded(Item item, CallbackInfo ci) {
        if (item instanceof CooldownNotifyItem notifyItem) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getCooldowns() == (Object) this) {
                ClientPacketHandlers.playCooldownSound(notifyItem.getSoundEvent());
            }
        }
    }
}
