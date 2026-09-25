package pro.komaru.tridot.client.gfx.text;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import pro.komaru.tridot.api.render.text.DotStyleEffects;
import pro.komaru.tridot.util.Col;
import pro.komaru.tridot.util.Log;
import pro.komaru.tridot.util.struct.data.Seq;
import pro.komaru.tridot.util.struct.func.Prov;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DotStyle extends Style {

    public Seq<StyleEffect> effects = Seq.with();

    public DotStyle(@Nullable TextColor pColor, @Nullable Boolean pBold, @Nullable Boolean pItalic, @Nullable Boolean pUnderlined, @Nullable Boolean pStrikethrough, @Nullable Boolean pObfuscated, @Nullable ClickEvent pClickEvent, @Nullable HoverEvent pHoverEvent, @Nullable String pInsertion, @Nullable ResourceLocation pFont) {
        super(pColor, pBold, pItalic, pUnderlined, pStrikethrough, pObfuscated, pClickEvent, pHoverEvent, pInsertion, pFont);
    }
    public DotStyle() {
        super(null,null,null,null,null,null,null,null,null,null);
    }

    public DotStyle from(Style style) {
        color = style.color;
        bold = style.bold;
        italic = style.italic;
        underlined = style.underlined;
        strikethrough = style.strikethrough;
        obfuscated = style.obfuscated;
        clickEvent = style.clickEvent;
        hoverEvent = style.hoverEvent;
        insertion = style.insertion;
        font = style.font;
        if (style instanceof DotStyle dot) effects.addAll(dot.effects);
        return this;
    }

    public static DotStyle of() {
        return new DotStyle();
    }

    @Override
    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else if (!(pOther instanceof DotStyle)) {
            return false;
        }
        DotStyle dot = (DotStyle) pOther;
        if(!dot.effects.equals(this.effects)) return false;
        return super.equals(pOther);
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + effects.hashCode();
    }

    public DotStyle color(Color color) {
        return this.color(new Col(color.getRGB()));
    }
    public DotStyle color(Col color) {
        return color(color.toTextColor());
    }
    public DotStyle color(TextColor color) {
        this.color = color;
        return this;
    }
    public DotStyle bold(boolean value) {
        this.bold = value;
        return this;
    }
    public DotStyle italic(boolean value) {
        this.italic = value;
        return this;
    }
    public DotStyle underlined(boolean value) {
        this.underlined = value;
        return this;
    }
    public DotStyle strikethrough(boolean value) {
        this.strikethrough = value;
        return this;
    }
    public DotStyle obfuscated(boolean value) {
        this.obfuscated = value;
        return this;
    }
    public DotStyle insertion(String value) {
        this.insertion = value;
        return this;
    }
    public DotStyle click(ClickEvent event) {
        this.clickEvent = event;
        return this;
    }
    public DotStyle hover(HoverEvent event) {
        this.hoverEvent = event;
        return this;
    }
    public DotStyle font(ResourceLocation font) {
        this.font = font;
        return this;
    }
    public DotStyle effects() {
        this.effects.clear();
        return this;
    }
    public DotStyle effects(StyleEffect...effects) {
        this.effects.addAll(effects);
        return this;
    }
    public DotStyle effects(ResourceLocation ...effects) {
        Seq<StyleEffect> fx = Seq.with();
        for (ResourceLocation effect : effects)
            fx.add(DotStyleEffects.EFFECTS.get(effect.toString()).get());
        return effects(fx.toArray());
    }
    public DotStyle effect(StyleEffect effect) {
        effects.add(effect);
        return this;
    }
    public DotStyle effect(ResourceLocation location) {
        return effect(DotStyleEffects.EFFECTS.get(location.toString()).get());
    }
    public DotStyle effect(String modId, String id) {
        return effect(ResourceLocation.fromNamespaceAndPath(modId,id));
    }

    public Col color() {
        if(color == null) return Col.white;
        return Col.fromARGB(color.getValue());
    }

    public List<String> effectStrings() {
        List<String> effects = new ArrayList<>(this.effects.size);
        for (int i = 0; i < this.effects.size; i++) {
            StyleEffect effect = this.effects.get(i);
            CompoundTag tag = new CompoundTag();
            DotStyleEffects.write(effect, tag);
            effects.add(tag.toString());
        }
        return effects;
    }

    public DotStyle readEffect(String effect) {
        try {
            CompoundTag tag = TagParser.parseTag(effect);
            StyleEffect styleEffect = DotStyleEffects.read(tag);
            if (styleEffect != null && styleEffect.id() != null) {
                effects.add(styleEffect);
            }
        } catch (Exception e) {
            Log.error("Failed to read effect: " + effect);
        }
        return this;
    }

    public static abstract class StyleEffect {
        public StyleEffect() {}

        public float advance(float advance) {
            return advance;
        }
        public float alpha(float alpha) {
            return alpha;
        }

        public void beforeGlyph(Font.StringRenderOutput self, DotStyle style, int index) {

        }
        public void beforeGlyphEffects(Font.StringRenderOutput self, DotStyle style, int index, FontSet fontset, GlyphInfo glyphinfo, BakedGlyph bakedglyph, TextColor textcolor, float f, float f1, float f2, float f3, float f6, float f7) {

        }
        public void afterGlyph(Font.StringRenderOutput self, DotStyle style, int index, FontSet fontset, GlyphInfo glyphinfo, BakedGlyph bakedglyph, TextColor textcolor, float f, float f1, float f2, float f3, float f6, float f7) {

        }

        public abstract ResourceLocation id();
        public abstract void write(CompoundTag tag);
        public abstract void read(CompoundTag tag);
    }

    public static class EffectEntry implements Prov<StyleEffect> {
        public Prov<StyleEffect> initializer;
        public ResourceLocation identifier;

        public EffectEntry(Prov<StyleEffect> initializer, ResourceLocation location) {
            this.initializer = initializer;
            this.identifier = location;
        }

        @Override
        public StyleEffect get() {
            return initializer.get();
        }
    }

    public static class Codecs extends MapCodec<Style> {
        public static final String FLAG_KEY = "tridot";
        public static final String EFFECTS_KEY = "tridot_effects";
        private static final Codec<List<String>> EFFECTS_CODEC = Codec.STRING.listOf();

        private final MapCodec<Style> vanilla;

        public Codecs(MapCodec<Style> vanilla) {
            this.vanilla = vanilla;
        }

        public static MapCodec<Style> wrap(MapCodec<Style> vanilla) {
            return vanilla instanceof Codecs ? vanilla : new Codecs(vanilla);
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.concat(vanilla.keys(ops), Stream.of(ops.createString(FLAG_KEY), ops.createString(EFFECTS_KEY)));
        }

        @Override
        public <T> DataResult<Style> decode(DynamicOps<T> ops, MapLike<T> input) {
            return vanilla.decode(ops, input).map(style -> {
                T flag = input.get(FLAG_KEY);
                if (flag == null) return style;
                DotStyle dot = DotStyle.of().from(style);
                T effects = input.get(EFFECTS_KEY);
                if (effects != null) {
                    EFFECTS_CODEC.parse(ops, effects).result().ifPresent(list -> list.forEach(dot::readEffect));
                }
                return dot;
            });
        }

        @Override
        public <T> RecordBuilder<T> encode(Style input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            RecordBuilder<T> builder = vanilla.encode(input, ops, prefix);
            if (input instanceof DotStyle dot) {
                builder.add(FLAG_KEY, ops.createBoolean(true));
                builder.add(EFFECTS_KEY, EFFECTS_CODEC.encodeStart(ops, dot.effectStrings()));
            }
            return builder;
        }

        @Override
        public String toString() {
            return "DotStyle[" + vanilla + "]";
        }
    }
}
