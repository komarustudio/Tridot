package pro.komaru.tridot.client.ui.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.minecraft.resources.ResourceLocation;

@Value
@AllArgsConstructor(staticName = "of")
public class TextureSource {
    ResourceLocation textureLocation;
    float clipX, clipY;
    int clipW, clipH;
    int textureWidth, textureHeight;

    public static TextureSource of(ResourceLocation location, int tw, int th) {
        return of(location, 0, 0, tw, th, tw, th);
    }
}
