package pro.komaru.tridot.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import pro.komaru.tridot.client.gfx.text.DotStyle;

@Mixin(Font.StringRenderOutput.class)
public class StringRenderOutputMixin {
    @Unique DotStyle tridot$style;

    @Inject(method = "accept", at = @At("TAIL"))
    public void accept(int index, Style pStyle, int pCodePoint, CallbackInfoReturnable<Boolean> cir) {
        if (pStyle instanceof DotStyle ds && !ds.effects.isEmpty()) {
            for (DotStyle.StyleEffect effect : ds.effects) {
                effect.afterGlyph(index, tridot$self(), ds, pCodePoint);
            }
        }
    }

    @Inject(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;renderChar(Lnet/minecraft/client/gui/font/glyphs/BakedGlyph;ZZFFFLorg/joml/Matrix4f;Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFI)V"))
    public void acceptBeforeEffects(int index, Style pStyle, int pCodePoint, CallbackInfoReturnable<Boolean> cir) {
        if (!(pStyle instanceof DotStyle ds) || ds.effects.isEmpty()) return;

        for (DotStyle.StyleEffect effect : ds.effects) {
            effect.beforeGlyphEffects(index, tridot$self(), ds, pCodePoint);
        }
    }

    @ModifyExpressionValue(method = "accept", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/Font$StringRenderOutput;a:F", opcode = org.objectweb.asm.Opcodes.GETFIELD))
    public float changeF3(float value) {
        if(tridot$style != null) {
            for (DotStyle.StyleEffect effect : tridot$style.effects)
                value = effect.alpha(value);
        }

        return value;
    }

    @ModifyExpressionValue(method = "accept", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/font/GlyphInfo;getAdvance(Z)F"))
    public float changeF6(float value) {
        if(tridot$style != null) {
            for (DotStyle.StyleEffect effect : tridot$style.effects)
                value = effect.advance(value);
        }

        return value;
    }

    @Inject(method = "accept", at = @At("HEAD"))
    public void acceptBefore(int index, Style pStyle, int pCodePoint, CallbackInfoReturnable<Boolean> cir) {
        tridot$style = null;
        if (pStyle instanceof DotStyle ds && !ds.effects.isEmpty()) {
            tridot$style = ds;
            for (DotStyle.StyleEffect effect : ds.effects) {
                effect.beforeGlyph(index, tridot$self(), ds, pCodePoint);
            }
        }
    }

    @Unique
    Font.StringRenderOutput tridot$self() {
        return (Font.StringRenderOutput) ((Object) this);
    }
}
