package pro.komaru.tridot.client.gfx.text;

import com.google.gson.*;
import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;
import org.jetbrains.annotations.Nullable;
import pro.komaru.tridot.api.render.text.DotStyleEffects;
import pro.komaru.tridot.util.Col;
import pro.komaru.tridot.util.Log;
import pro.komaru.tridot.util.struct.data.Seq;
import pro.komaru.tridot.util.struct.func.Prov;

import java.awt.*;
import java.lang.reflect.Type;

public class DotStyle extends Style {

    public Seq<StyleEffect> effects = Seq.with();

    static {
        Component.Serializer.GSON = Util.make(() -> {
            GsonBuilder gsonbuilder = new GsonBuilder();
            gsonbuilder.disableHtmlEscaping();
            gsonbuilder.registerTypeHierarchyAdapter(Component.class, new Component.Serializer());
            gsonbuilder.registerTypeHierarchyAdapter(Style.class, new DotStyle.DefaultSerializer());
            gsonbuilder.registerTypeHierarchyAdapter(DotStyle.class, new DotStyle.Serializer());
            gsonbuilder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
            return gsonbuilder.create();
        });
    }

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
        for (ResourceLocation effect : effects) fx.add(DotStyleEffects.EFFECTS.get(effect.toString()).get());
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
        return effect(new ResourceLocation(modId,id));
    }

    public Col color() {
        if(color == null) return Col.white;
        return Col.fromARGB(color.getValue());
    }

    public static abstract class StyleEffect {
        public StyleEffect() {}

        public float advance(float advance) {
            return advance;
        }

        public float alpha(float alpha) {
            return alpha;
        }

        public void beforeGlyph(int index, Font.StringRenderOutput self, DotStyle style, int codePoint) {

        }

        public void beforeGlyphEffects(int index, Font.StringRenderOutput self, DotStyle style, int codePoint) {

        }

        public void afterGlyph(int index, Font.StringRenderOutput self, DotStyle style, int codePoint) {
            
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

    public static class Serializer implements JsonDeserializer<DotStyle>, JsonSerializer<DotStyle> {

        @Override
        public DotStyle deserialize(JsonElement obj, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            if (!obj.isJsonObject()) return null;
            JsonObject json = obj.getAsJsonObject();
            if (json == null) return null;

            Boolean obool = getOptionalFlag(json, "bold");
            Boolean obool1 = getOptionalFlag(json, "italic");
            Boolean obool2 = getOptionalFlag(json, "underlined");
            Boolean obool3 = getOptionalFlag(json, "strikethrough");
            Boolean obool4 = getOptionalFlag(json, "obfuscated");
            TextColor textcolor = getTextColor(json);
            String s = getInsertion(json);
            ClickEvent clickevent = getClickEvent(json);
            HoverEvent hoverevent = getHoverEvent(json);
            ResourceLocation resourcelocation = getFont(json);

            DotStyle dot = new DotStyle(textcolor, obool, obool1, obool2, obool3, obool4, clickevent, hoverevent, s, resourcelocation);

            String[] effects;
            if (json.has("tridot_effects")) {
                effects = ctx.deserialize(json.get("tridot_effects"), String[].class);

                for (String effect : effects) {
                    try {
                        CompoundTag tag = TagParser.parseTag(effect);
                        StyleEffect styleEffect = DotStyleEffects.read(tag);
                        if (styleEffect != null) {
                            if (styleEffect.id() == null) continue;
                            dot.effects.add(styleEffect);
                        }
                    } catch (Exception e) {
                        Log.error("Failed to read effect: " + effect);
                    }
                }
            }

            return dot;
        }

        @Override
        public JsonElement serialize(DotStyle style, Type type, JsonSerializationContext ctx) {
            if (style.isEmpty()) return null;

            JsonObject json = new JsonObject();
            if (style.bold != null) json.addProperty("bold", style.bold);
            if (style.italic != null) json.addProperty("italic", style.italic);
            if (style.underlined != null) json.addProperty("underlined", style.underlined);
            if (style.strikethrough != null) json.addProperty("strikethrough", style.strikethrough);
            if (style.obfuscated != null) json.addProperty("obfuscated", style.obfuscated);
            if (style.color != null) json.addProperty("color", style.color.serialize());
            if (style.insertion != null) json.add("insertion", ctx.serialize(style.insertion));
            if (style.clickEvent != null) {
                JsonObject jsonobject1 = new JsonObject();
                jsonobject1.addProperty("action", style.clickEvent.getAction().getName());
                jsonobject1.addProperty("value", style.clickEvent.getValue());
                json.add("clickEvent", jsonobject1);
            }

            if (style.hoverEvent != null) json.add("hoverEvent", style.hoverEvent.serialize());
            if (style.font != null) json.addProperty("font", style.font.toString());

            String[] effects = new String[style.effects.size];
            for (int i = 0; i < style.effects.size; i++) {
                StyleEffect effect = style.effects.get(i);
                CompoundTag tag = new CompoundTag();

                DotStyleEffects.write(effect, tag);

                effects[i] = tag.toString();
            }
            json.addProperty("tridot",true);
            json.add("tridot_effects", ctx.serialize(effects));

            return json;
        }

        @javax.annotation.Nullable
        private static Boolean getOptionalFlag(JsonObject pJson, String pMemberName) {
            return pJson.has(pMemberName) ? pJson.get(pMemberName).getAsBoolean() : null;
        }

        @javax.annotation.Nullable
        private static ResourceLocation getFont(JsonObject pJson) {
            if (pJson.has("font")) {
                String s = GsonHelper.getAsString(pJson, "font");

                try {
                    return new ResourceLocation(s);
                } catch (ResourceLocationException resourcelocationexception) {
                    throw new JsonSyntaxException("Invalid font name: " + s);
                }
            } else {
                return null;
            }
        }

        @javax.annotation.Nullable
        private static HoverEvent getHoverEvent(JsonObject pJson) {
            if (pJson.has("hoverEvent")) {
                JsonObject jsonobject = GsonHelper.getAsJsonObject(pJson, "hoverEvent");
                HoverEvent hoverevent = HoverEvent.deserialize(jsonobject);
                if (hoverevent != null && hoverevent.getAction().isAllowedFromServer()) {
                    return hoverevent;
                }
            }

            return null;
        }

        @javax.annotation.Nullable
        private static ClickEvent getClickEvent(JsonObject pJson) {
            if (pJson.has("clickEvent")) {
                JsonObject jsonobject = GsonHelper.getAsJsonObject(pJson, "clickEvent");
                String s = GsonHelper.getAsString(jsonobject, "action", (String)null);
                ClickEvent.Action clickevent$action = s == null ? null : ClickEvent.Action.getByName(s);
                String s1 = GsonHelper.getAsString(jsonobject, "value", (String)null);
                if (clickevent$action != null && s1 != null && clickevent$action.isAllowedFromServer()) {
                    return new ClickEvent(clickevent$action, s1);
                }
            }

            return null;
        }

        @javax.annotation.Nullable
        private static String getInsertion(JsonObject pJson) {
            return GsonHelper.getAsString(pJson, "insertion", (String)null);
        }

        @javax.annotation.Nullable
        private static TextColor getTextColor(JsonObject pJson) {
            if (pJson.has("color")) {
                String s = GsonHelper.getAsString(pJson, "color");
                return TextColor.parseColor(s);
            } else {
                return null;
            }
        }
    }

    public static class DefaultSerializer implements JsonDeserializer<Style>, JsonSerializer<Style> {
        public DefaultSerializer() {
        }

        @javax.annotation.Nullable
        public Style deserialize(JsonElement el, Type p_131201_, JsonDeserializationContext ctx) throws JsonParseException {
            if (el.isJsonObject()) {
                JsonObject obj = el.getAsJsonObject();
                if (obj == null) {
                    return null;
                } else {

                    if(obj.has("tridot")) {
                        return ctx.deserialize(el, DotStyle.class);
                    }

                    Boolean $$4 = getOptionalFlag(obj, "bold");
                    Boolean $$5 = getOptionalFlag(obj, "italic");
                    Boolean $$6 = getOptionalFlag(obj, "underlined");
                    Boolean $$7 = getOptionalFlag(obj, "strikethrough");
                    Boolean $$8 = getOptionalFlag(obj, "obfuscated");
                    TextColor $$9 = getTextColor(obj);
                    String $$10 = getInsertion(obj);
                    ClickEvent $$11 = getClickEvent(obj);
                    HoverEvent $$12 = getHoverEvent(obj);
                    ResourceLocation $$13 = getFont(obj);
                    return new Style($$9, $$4, $$5, $$6, $$7, $$8, $$11, $$12, $$10, $$13);
                }
            } else {
                return null;
            }
        }

        @javax.annotation.Nullable
        private static ResourceLocation getFont(JsonObject pJson) {
            if (pJson.has("font")) {
                String $$1 = GsonHelper.getAsString(pJson, "font");

                try {
                    return new ResourceLocation($$1);
                } catch (ResourceLocationException var3) {
                    throw new JsonSyntaxException("Invalid font name: " + $$1);
                }
            } else {
                return null;
            }
        }

        @javax.annotation.Nullable
        private static HoverEvent getHoverEvent(JsonObject pJson) {
            if (pJson.has("hoverEvent")) {
                JsonObject $$1 = GsonHelper.getAsJsonObject(pJson, "hoverEvent");
                HoverEvent $$2 = HoverEvent.deserialize($$1);
                if ($$2 != null && $$2.getAction().isAllowedFromServer()) {
                    return $$2;
                }
            }

            return null;
        }

        @javax.annotation.Nullable
        private static ClickEvent getClickEvent(JsonObject pJson) {
            if (pJson.has("clickEvent")) {
                JsonObject $$1 = GsonHelper.getAsJsonObject(pJson, "clickEvent");
                String $$2 = GsonHelper.getAsString($$1, "action", (String)null);
                ClickEvent.Action $$3 = $$2 == null ? null : ClickEvent.Action.getByName($$2);
                String $$4 = GsonHelper.getAsString($$1, "value", (String)null);
                if ($$3 != null && $$4 != null && $$3.isAllowedFromServer()) {
                    return new ClickEvent($$3, $$4);
                }
            }

            return null;
        }

        @javax.annotation.Nullable
        private static String getInsertion(JsonObject pJson) {
            return GsonHelper.getAsString(pJson, "insertion", (String)null);
        }

        @javax.annotation.Nullable
        private static TextColor getTextColor(JsonObject pJson) {
            if (pJson.has("color")) {
                String $$1 = GsonHelper.getAsString(pJson, "color");
                return TextColor.parseColor($$1);
            } else {
                return null;
            }
        }

        @javax.annotation.Nullable
        private static Boolean getOptionalFlag(JsonObject pJson, String pMemberName) {
            return pJson.has(pMemberName) ? pJson.get(pMemberName).getAsBoolean() : null;
        }

        @javax.annotation.Nullable
        public JsonElement serialize(Style style, Type p_131210_, JsonSerializationContext ctx) {
            if (style.isEmpty()) {
                return null;
            } else {

                if(style instanceof DotStyle ds) return ctx.serialize(ds);

                JsonObject $$3 = new JsonObject();
                if (style.bold != null) {
                    $$3.addProperty("bold", style.bold);
                }

                if (style.italic != null) {
                    $$3.addProperty("italic", style.italic);
                }

                if (style.underlined != null) {
                    $$3.addProperty("underlined", style.underlined);
                }

                if (style.strikethrough != null) {
                    $$3.addProperty("strikethrough", style.strikethrough);
                }

                if (style.obfuscated != null) {
                    $$3.addProperty("obfuscated", style.obfuscated);
                }

                if (style.color != null) {
                    $$3.addProperty("color", style.color.serialize());
                }

                if (style.insertion != null) {
                    $$3.add("insertion", ctx.serialize(style.insertion));
                }

                if (style.clickEvent != null) {
                    JsonObject $$4 = new JsonObject();
                    $$4.addProperty("action", style.clickEvent.getAction().getName());
                    $$4.addProperty("value", style.clickEvent.getValue());
                    $$3.add("clickEvent", $$4);
                }

                if (style.hoverEvent != null) {
                    $$3.add("hoverEvent", style.hoverEvent.serialize());
                }

                if (style.font != null) {
                    $$3.addProperty("font", style.font.toString());
                }

                return $$3;
            }
        }
    }
}
