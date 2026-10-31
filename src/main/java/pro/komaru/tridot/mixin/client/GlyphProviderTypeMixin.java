package pro.komaru.tridot.mixin.client;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pro.komaru.tridot.api.render.text.TridotBitmapDefinition;

@Mixin(GlyphProviderType.class)
public class GlyphProviderTypeMixin {

    @Inject(method = "mapCodec", at = @At("HEAD"), cancellable = true)
    private void tridot$overrideBitmapCodec(CallbackInfoReturnable<MapCodec<? extends GlyphProviderDefinition>> cir) {
        GlyphProviderType self = (GlyphProviderType) (Object) this;
        if (self == GlyphProviderType.BITMAP) {
            cir.setReturnValue(TridotBitmapDefinition.CODEC);
        }
    }
}
