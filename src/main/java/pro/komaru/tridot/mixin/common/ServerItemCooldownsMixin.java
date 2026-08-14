package pro.komaru.tridot.mixin.common;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ServerItemCooldowns;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pro.komaru.tridot.common.registry.item.types.CooldownHandler;

@Mixin(ServerItemCooldowns.class)
public class ServerItemCooldownsMixin {

    @Shadow
    @Final
    private ServerPlayer player;

    @Inject(at = @At("TAIL"), method = "onCooldownEnded")
    public void onCooldownEnded(Item pItem, CallbackInfo ci) {
        CooldownHandler.onCooldownEnd(player, pItem);
    }
}