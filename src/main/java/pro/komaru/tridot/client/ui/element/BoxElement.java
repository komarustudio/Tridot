package pro.komaru.tridot.client.ui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import stellar.qrix.neoforge.QrixNeoforge;
import pro.komaru.tridot.client.ui.enums.MouseButtonType;
import pro.komaru.tridot.client.ui.model.TextureSource;

import java.util.function.Consumer;
import java.util.function.Function;

public class BoxElement extends BaseElement<BoxElement> {
    public static final ResourceLocation DEFAULT_ELEMENTS_TEXTURE = QrixNeoforge.location("textures/ui/default_ui_elements.png");
    public static final TextureSource
            BUTTON_DEFAULT = TextureSource.of(DEFAULT_ELEMENTS_TEXTURE, 0, 0,
            200, 22, 256, 256),
            BUTTON_HOVER = TextureSource.of(DEFAULT_ELEMENTS_TEXTURE, 0, 22,
                    200, 22, 256, 256),
            BUTTON_PRESS = TextureSource.of(DEFAULT_ELEMENTS_TEXTURE, 0, 44,
                    200, 22, 256, 256);

    public static final int BUTTON_HEIGHT = 22;

    @Nullable
    private TextureSource textureSource = null;
    @Nullable
    private TextureSource hoverTextureSource = null;
    @Nullable
    private TextureSource pressTextureSource = null;

    @Nullable
    private Integer boxColor = null;

    @Nullable
    private Function<BoxElement, Boolean> onClick = null;

    @Nullable
    private Consumer<BoxElement> onRender = null;

    public static BoxElement button(String text, int width) {
        TextElement textEl;
        return new BoxElement()
                .texture(BUTTON_DEFAULT, BUTTON_HOVER, BUTTON_PRESS)
                .minDims(width, BUTTON_HEIGHT)
                .addChild(textEl = new TextElement(text).alignCenter().noWrap())
                .onRender(b -> {
                    int textOffset = b.isPressed() ? 2 : 0;
                    textEl.renderOffset(0, textOffset - 1);
                });
    }

    public BoxElement onClick(Function<BoxElement, Boolean> callback) {
        this.onClick = callback;
        return this;
    }

    public BoxElement onRender(Consumer<BoxElement> callback) {
        this.onRender = callback;
        return this;
    }


    public BoxElement texture(TextureSource source) {
        this.textureSource = source;
        this.boxColor = null;

        return this;
    }

    public BoxElement hoverTexture(TextureSource source) {
        this.hoverTextureSource = source;
        this.boxColor = null;

        return this;
    }

    public BoxElement pressTexture(TextureSource source) {
        this.pressTextureSource = source;
        this.boxColor = null;

        return this;
    }

    public BoxElement texture(TextureSource def, TextureSource hover, TextureSource press) {
        this.textureSource = def;
        this.hoverTextureSource = hover;
        this.pressTextureSource = press;

        this.boxColor = null;

        return this;
    }

    public BoxElement texture(TextureSource def, TextureSource hover) {
        this.textureSource = def;
        this.hoverTextureSource = hover;
        this.pressTextureSource = null;

        this.boxColor = null;

        return this;
    }

    public BoxElement rectangle(int color) {
        this.textureSource = hoverTextureSource = pressTextureSource = null;
        this.boxColor = color;

        return this;
    }

    @Override
    public void renderElement(Minecraft mc, GuiGraphics gui, float pt) {
        if(onRender != null) onRender.accept(this);

        var source = currentTextureSource();
        if (source != null) {
            gui.blit(source.getTextureLocation(),
                    0, 0,
                    (int) getWidth(), (int) getHeight(),
                    source.getClipX(),
                    source.getClipY(),
                    source.getClipW(),
                    source.getClipH(),
                    source.getTextureWidth(),
                    source.getTextureHeight()
            );
            return;
        }

        if (boxColor != null) {
            int color = boxColor;
            gui.fill(0, 0, (int) getWidth(), (int) getHeight(), color);
            return;
        }
    }

    private TextureSource currentTextureSource() {
        if (textureSource == null) return null;
        if (isPressed() && pressTextureSource != null) return pressTextureSource;
        if (isHovered() && hoverTextureSource != null) return hoverTextureSource;
        return textureSource;
    }

    @Override
    protected boolean onMouseDown(float mouseX, float mouseY, MouseButtonType clickType) {
        return clickType == MouseButtonType.LEFT && onClick != null;
    }

    @Override
    protected void onMouseClicked(float mouseX, float mouseY, MouseButtonType clickType) {
        if (onClick == null) return;
        onClick.apply(this);
    }
}
