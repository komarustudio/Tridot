package pro.komaru.tridot.client.model.render.item;

import com.mojang.blaze3d.vertex.*;

public class GhostVertexConsumer implements VertexConsumer{
    private final VertexConsumer wrapped;
    private final float alpha;
    private final float time;

    public GhostVertexConsumer(VertexConsumer wrapped, float alpha, float time) {
        this.wrapped = wrapped;
        this.alpha = alpha;
        this.time = time;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        float offset = (float)(Math.sin((y * 10) + time) * 0.02);
        wrapped.addVertex(x + offset, y, z + offset);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        wrapped.setColor(100, 200, 255, (int)(255 * this.alpha));
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        wrapped.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        wrapped.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        wrapped.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        wrapped.setNormal(x, y, z);
        return this;
    }
}