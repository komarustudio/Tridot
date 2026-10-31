package pro.komaru.tridot.api.render.text;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record TridotBitmapDefinition(ResourceLocation file, int height, Optional<Integer> customWidth, int ascent, int[][] codepointGrid) implements GlyphProviderDefinition {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Codec<int[][]> CODEPOINT_GRID_CODEC = Codec.STRING.listOf().xmap(list -> {
        int size = list.size();
        int[][] grid = new int[size][];
        for (int i = 0; i < size; ++i) {
            grid[i] = list.get(i).codePoints().toArray();
        }
        return grid;
    }, grid -> {
        List<String> list = new ArrayList<>(grid.length);
        for (int[] row : grid) {
            list.add(new String(row, 0, row.length));
        }
        return list;
    }).validate(TridotBitmapDefinition::validateDimensions);

    public static final MapCodec<TridotBitmapDefinition> CODEC = RecordCodecBuilder.<TridotBitmapDefinition>mapCodec(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("file").forGetter(TridotBitmapDefinition::file),
            Codec.INT.optionalFieldOf("height", 8).forGetter(TridotBitmapDefinition::height),
            Codec.INT.optionalFieldOf("width").forGetter(TridotBitmapDefinition::customWidth),
            Codec.INT.fieldOf("ascent").forGetter(TridotBitmapDefinition::ascent),
            CODEPOINT_GRID_CODEC.fieldOf("chars").forGetter(TridotBitmapDefinition::codepointGrid)
    ).apply(builder, TridotBitmapDefinition::new)).validate(TridotBitmapDefinition::validate);

    private static DataResult<int[][]> validateDimensions(int[][] grid) {
        int rows = grid.length;
        if (rows == 0) {
            return DataResult.error(() -> "Expected to find data in codepoint grid");
        }
        int cols = grid[0].length;
        for (int i = 1; i < rows; ++i) {
            int[] row = grid[i];
            if (row.length != cols) {
                return DataResult.error(() -> "Lines in codepoint grid have to be the same length (found: " + row.length + " codepoints, expected: " + cols + "), pad with \\u0000");
            }
        }
        return DataResult.success(grid);
    }

    private static DataResult<TridotBitmapDefinition> validate(TridotBitmapDefinition def) {
        if (def.ascent > def.height) {
            return DataResult.error(() -> "Ascent " + def.ascent + " higher than height " + def.height);
        }
        return DataResult.success(def);
    }

    @Override
    public GlyphProviderType type() {
        return GlyphProviderType.BITMAP;
    }

    @Override
    public Either<Loader, Reference> unpack() {
        return Either.left(this::load);
    }

    private GlyphProvider load(ResourceManager resourceManager) throws IOException {
        ResourceLocation path = this.file.withPrefix("textures/");
        try (InputStream stream = resourceManager.open(path)) {
            NativeImage nativeImage = NativeImage.read(NativeImage.Format.RGBA, stream);
            int imgWidth = nativeImage.getWidth();
            int imgHeight = nativeImage.getHeight();
            int cellWidth = imgWidth / this.codepointGrid[0].length;
            int cellHeight = imgHeight / this.codepointGrid.length;
            float scale = (float) this.height / (float) cellHeight;
            CodepointMap<TridotGlyph> map = new CodepointMap<>(TridotGlyph[]::new, TridotGlyph[][]::new);

            for (int row = 0; row < this.codepointGrid.length; ++row) {
                int currentRow = row;
                int colIdx = 0;
                for (int codePoint : this.codepointGrid[row]) {
                    int currentCol = colIdx++;
                    if (codePoint != 0) {
                        int actualWidth = this.customWidth.orElseGet(() -> this.getActualGlyphWidth(nativeImage, cellWidth, cellHeight, currentCol, currentRow));
                        TridotGlyph glyph = map.put(
                                codePoint,
                                new TridotGlyph(
                                        scale,
                                        nativeImage,
                                        currentCol * cellWidth,
                                        row * cellHeight,
                                        cellWidth,
                                        cellHeight,
                                        (int) (0.5D + (double) ((float) actualWidth * scale)) + 1,
                                        this.ascent
                                )
                        );
                        if (glyph != null) {
                            LOGGER.warn("Codepoint '{}' declared multiple times in {}", Integer.toHexString(codePoint), path);
                        }
                    }
                }
            }
            return new TridotGlyphProvider(nativeImage, map);
        }
    }

    private int getActualGlyphWidth(NativeImage image, int width, int height, int gridX, int gridY) {
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();
        for (int rightmost = width - 1; rightmost >= 0; --rightmost) {
            int px = gridX * width + rightmost;
            for (int pyOffset = 0; pyOffset < height; ++pyOffset) {
                int py = gridY * height + pyOffset;
                if (px < imgWidth && py < imgHeight) {
                    if (image.getLuminanceOrAlpha(px, py) != 0) {
                        return rightmost + 1;
                    }
                }
            }
        }

        return width;
    }

    public static class TridotGlyphProvider implements GlyphProvider {
        private final NativeImage image;
        private final CodepointMap<TridotGlyph> glyphs;

        public TridotGlyphProvider(NativeImage image, CodepointMap<TridotGlyph> glyphs) {
            this.image = image;
            this.glyphs = glyphs;
        }

        @Override
        public void close() {
            this.image.close();
        }

        @Nullable
        @Override
        public GlyphInfo getGlyph(int character) {
            return this.glyphs.get(character);
        }

        @Override
        public IntSet getSupportedGlyphs() {
            return IntSets.unmodifiable(this.glyphs.keySet());
        }
    }

    public record TridotGlyph(float scale, NativeImage image, int offsetX, int offsetY, int width, int height, int advance, int ascent) implements GlyphInfo {
        @Override
        public float getAdvance() {
            return (float) this.advance;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> provider) {
            return provider.apply(new SheetGlyphInfo() {
                @Override
                public float getOversample() {
                    return 1.0F / TridotGlyph.this.scale;
                }

                @Override
                public int getPixelWidth() {
                    return TridotGlyph.this.width;
                }

                @Override
                public int getPixelHeight() {
                    return TridotGlyph.this.height;
                }

                @Override
                public float getBearingTop() {
                    return (float) TridotGlyph.this.ascent;
                }

                @Override
                public void upload(int xOffset, int yOffset) {
                    TridotGlyph.this.image.upload(0, xOffset, yOffset, TridotGlyph.this.offsetX, TridotGlyph.this.offsetY, TridotGlyph.this.width, TridotGlyph.this.height, false, false);
                }

                @Override
                public boolean isColored() {
                    return TridotGlyph.this.image.format().components() > 1;
                }
            });
        }
    }
}
