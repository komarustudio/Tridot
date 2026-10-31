package pro.komaru.tridot.client.gfx.postprocess.bloom;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.ApiStatus;
import pro.komaru.tridot.client.gfx.TridotShaders;
import pro.komaru.tridot.common.config.ClientConfig;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Dual-filter (Kawase) bloom.
 * <p>
 * All bloom geometry is recorded into a dedicated deferred {@link #bufferSource()} and flushed ONLY after all
 * entities, block entities, and world layers have finished writing to the main depth buffer, so other mobs and
 * players properly occlude bloom behind them.
 */
public final class BloomRenderer {
    public static float strength = 1.0F;
    /** How much over-bright thin cores burn white (0 = off). */
    public static float whiten = 0.0F;
    /** Soft compression of very bright stacked light on the first downsample (0 = off). */
    public static float knee = 0.0F;
    /** Number of downsample levels: more = wider glow. */
    public static int levels = 5;

    private static final Map<RenderType, BufferBuilder> BUFFERS = new LinkedHashMap<>();
    private static final Set<RenderType> ACTIVE_BUFFERS = new LinkedHashSet<>();

    private static final MultiBufferSource BUFFER_SOURCE = renderType -> {
        BufferBuilder builder = BUFFERS.computeIfAbsent(renderType, rt -> new BufferBuilder(rt.bufferSize()));
        if (ACTIVE_BUFFERS.add(renderType)) {
            builder.begin(renderType.mode(), renderType.format());
        }
        return builder;
    };

    private static BloomTarget mask;
    private static BloomTarget[] chain = new BloomTarget[0];

    private BloomRenderer() {}

    public static MultiBufferSource bufferSource() {
        return BUFFER_SOURCE;
    }

    public static boolean hasPendingWork() {
        return !ACTIVE_BUFFERS.isEmpty();
    }

    public static void beginFrame() {
        if (!ClientConfig.BLOOM.get()) {
            discardPending();
            return;
        }

        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        ensureTargets(main.width, main.height);
    }

    public static void flushAndComposite() {
        if (!ClientConfig.BLOOM.get() || ACTIVE_BUFFERS.isEmpty() || mask == null) {
            discardPending();
            return;
        }

        ShaderInstance down = TridotShaders.getBloomDownShader();
        ShaderInstance up = TridotShaders.getBloomUpShader();
        ShaderInstance composite = TridotShaders.getBloomCompositeShader();
        if (down == null || up == null || composite == null || chain.length == 0) {
            discardPending();
            return;
        }

        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        ensureTargets(main.width, main.height);

        mask.shareDepthWith(main);
        mask.clear(Minecraft.ON_OSX);
        main.bindWrite(false);
        for (RenderType renderType : ACTIVE_BUFFERS) {
            BufferBuilder builder = BUFFERS.get(renderType);
            if (builder != null) {
                renderType.end(builder, RenderSystem.getVertexSorting());
            }
        }

        ACTIVE_BUFFERS.clear();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        RenderSystem.disableBlend();
        RenderTarget src = mask;
        for (int i = 0; i < chain.length; i++) {
            BloomTarget dst = chain[i];
            dst.bindWrite(true);
            down.setSampler("DiffuseSampler", src.getColorTextureId());
            down.safeGetUniform("TexelSize").set(1.0F / src.width, 1.0F / src.height);
            down.safeGetUniform("Knee").set(i == 0 ? knee : 0.0F);
            blit(down);
            src = dst;
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        for (int i = chain.length - 1; i > 0; i--) {
            BloomTarget from = chain[i];
            BloomTarget to = chain[i - 1];
            to.bindWrite(true);
            up.setSampler("DiffuseSampler", from.getColorTextureId());
            up.safeGetUniform("TexelSize").set(1.0F / from.width, 1.0F / from.height);
            blit(up);
        }

        main.bindWrite(true);
        composite.setSampler("BloomSampler", chain[0].getColorTextureId());
        composite.setSampler("HdrSampler", mask.getColorTextureId());
        composite.safeGetUniform("TexelSize").set(1.0F / mask.width, 1.0F / mask.height);
        composite.safeGetUniform("Strength").set(strength);
        composite.safeGetUniform("Whiten").set(whiten);
        blit(composite);

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    @ApiStatus.Internal
    public static void bindMask() {
        if (mask == null) return;
        mask.bindWrite(false);
    }

    @ApiStatus.Internal
    public static void unbindMask() {
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    public static boolean isActive() {
        return ClientConfig.BLOOM.get() && mask != null;
    }

    private static void discardPending() {
        for (RenderType renderType : ACTIVE_BUFFERS) {
            BufferBuilder builder = BUFFERS.get(renderType);
            if (builder != null && builder.building()) {
                builder.end().release();
            }
        }

        ACTIVE_BUFFERS.clear();
    }

    private static void ensureTargets(int width, int height) {
        int wanted = Math.max(1, levels);
        if (mask != null && mask.width == width && mask.height == height && chain.length == countLevels(width, height, wanted)) {
            return;
        }

        if (mask == null) {
            mask = new BloomTarget(width, height);
        } else {
            mask.resize(width, height, Minecraft.ON_OSX);
        }

        for (BloomTarget t : chain) t.destroyBuffers();
        chain = new BloomTarget[countLevels(width, height, wanted)];
        int w = width, h = height;
        for (int i = 0; i < chain.length; i++) {
            w = Math.max(1, w >> 1);
            h = Math.max(1, h >> 1);
            chain[i] = new BloomTarget(w, h);
        }
    }

    private static int countLevels(int width, int height, int wanted) {
        int n = 0, w = width, h = height;
        while (n < wanted && (w >> 1) >= 2 && (h >> 1) >= 2) {
            w >>= 1;
            h >>= 1;
            n++;
        }
        return n;
    }

    private static void blit(ShaderInstance shader) {
        RenderSystem.setShader(() -> shader);
        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        b.vertex(0.0D, 0.0D, 0.0D).endVertex();
        b.vertex(1.0D, 0.0D, 0.0D).endVertex();
        b.vertex(1.0D, 1.0D, 0.0D).endVertex();
        b.vertex(0.0D, 1.0D, 0.0D).endVertex();
        BufferUploader.drawWithShader(b.end());
    }
}
