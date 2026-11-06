package pro.komaru.tridot.client.ui.model;

import net.minecraft.resources.ResourceLocation;

public record TextureSource(ResourceLocation textureLocation, float clipX, float clipY, int clipW, int clipH, int textureWidth, int textureHeight) {
    public static TextureSource of(ResourceLocation textureLocation, float clipX, float clipY, int clipW, int clipH, int textureWidth, int textureHeight) {
        return new TextureSource(textureLocation, clipX, clipY, clipW, clipH, textureWidth, textureHeight);
    }

    public static TextureSource of(ResourceLocation location, int tw, int th) {
        return of(location, 0, 0, tw, th, tw, th);
    }

    public TextureSource(ResourceLocation location, int tw, int th) {
        this(location, 0, 0, tw, th, tw, th);
    }
}