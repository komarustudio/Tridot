package pro.komaru.tridot.client.render;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.util.*;
import net.minecraft.world.phys.*;
import org.joml.*;
import pro.komaru.tridot.util.*;

import java.lang.Math;

public class Draw3D {
    private static final Col currentColor = Col.white.copy();
    private static float currentAlpha = 1.0F;
    private static float currentStroke = 0.08F;
    private static boolean isAdditive = true;
    private static final Vector3f[] QUAD = { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };

    public static void color() {
        currentColor.set(Col.white);
        currentAlpha = 1.0F;
        currentStroke = 0.08F;
        isAdditive = true;
    }

    public static void color(Col c) {
        currentColor.set(c);
        currentAlpha = c.a;
    }

    public static void color(Col c, float alpha) {
        currentColor.set(c);
        currentAlpha = alpha;
    }

    public static void color(Col from, Col to, float progress) {
        currentColor.set(from).lerp(to, Mth.clamp(progress, 0f, 1f));
    }

    public static void alpha(float a) {
        currentAlpha = Mth.clamp(a, 0f, 1f);
    }

    public static void stroke(float s) {
        currentStroke = Math.max(0.005F, s);
    }

    public static void additive(boolean additive) {
        isAdditive = additive;
    }

    private static RenderBuilder builder() {
        return RenderBuilder.create()
                .setRenderType(isAdditive ? TridotRenderTypes.ADDITIVE : TridotRenderTypes.TRANSLUCENT)
                .enableSided()
                .setColor(currentColor)
                .setAlpha(currentAlpha);
    }

    public static void cylinder(PoseStack pose, float x, float y, float z, float radiusBottom, float radiusTop, float height, int sides) {
        if ((radiusBottom <= 0.001F && radiusTop <= 0.001F) || sides < 3) return;
        RenderBuilder rb = builder();
        Matrix4f mat = pose.last().pose();
        float step = Mth.TWO_PI / sides;
        for (int i = 0; i < sides; i++) {
            float a1 = i * step;
            float a2 = (i + 1) * step;
            float cos1 = Mth.cos(a1), sin1 = Mth.sin(a1);
            float cos2 = Mth.cos(a2), sin2 = Mth.sin(a2);

            QUAD[0].set(x + cos1 * radiusBottom, y,          z + sin1 * radiusBottom);
            QUAD[1].set(x + cos1 * radiusTop,    y + height, z + sin1 * radiusTop);
            QUAD[2].set(x + cos2 * radiusTop,    y + height, z + sin2 * radiusTop);
            QUAD[3].set(x + cos2 * radiusBottom, y,          z + sin2 * radiusBottom);
            rb.renderQuad(mat, QUAD);
        }
    }

    public static void poly(PoseStack pose, float x, float y, float z, int sides, float radius, float rotationDeg) {
        if (radius <= 0.001F || sides < 3) return;
        RenderBuilder rb = builder();
        Matrix4f mat = pose.last().pose();

        float halfStroke = currentStroke * 0.5F;
        float rIn = Math.max(0.0F, radius - halfStroke);
        float rOut = radius + halfStroke;
        float rotRad = (float) Math.toRadians(rotationDeg);
        float step = (Mth.TWO_PI) / sides;

        for (int i = 0; i < sides; i++) {
            float a1 = rotRad + i * step;
            float a2 = rotRad + (i + 1) * step;

            float cos1 = Mth.cos(a1), sin1 = Mth.sin(a1);
            float cos2 = Mth.cos(a2), sin2 = Mth.sin(a2);

            QUAD[0].set(x + cos1 * rIn,  y, z + sin1 * rIn);
            QUAD[1].set(x + cos1 * rOut, y, z + sin1 * rOut);
            QUAD[2].set(x + cos2 * rOut, y, z + sin2 * rOut);
            QUAD[3].set(x + cos2 * rIn,  y, z + sin2 * rIn);
            rb.renderQuad(mat, QUAD);
        }
    }

    public static void circle(PoseStack pose, float x, float y, float z, float radius) {
        poly(pose, x, y, z, 28, radius, 0.0F);
    }

    public static void square(PoseStack pose, float x, float y, float z, float radius, float rotationDeg) {
        poly(pose, x, y, z, 4, radius, rotationDeg);
    }

    public static void line(PoseStack pose, Vec3 start, Vec3 end) {
        builder().renderBeam(pose.last().pose(), start, end, currentStroke);
    }

    public static void lineAngle(PoseStack pose, double x, double y, double z, float angleDeg, float length) {
        float rad = (float) Math.toRadians(angleDeg);
        Vec3 start = new Vec3(x, y, z);
        Vec3 end = new Vec3(x + Mth.cos(rad) * length, y, z + Mth.sin(rad) * length);
        line(pose, start, end);
    }

    public static void lineAngleCenter(PoseStack pose, double x, double y, double z, float angleDeg, float length) {
        float rad = (float) Math.toRadians(angleDeg);
        float dx = Mth.cos(rad) * length * 0.5F;
        float dz = Mth.sin(rad) * length * 0.5F;
        line(pose, new Vec3(x - dx, y, z - dz), new Vec3(x + dx, y, z + dz));
    }

    public static void lineAngle3D(PoseStack pose, double x, double y, double z, double dx, double dy, double dz, float length) {
        Vec3 start = new Vec3(x, y, z);
        Vec3 dir = new Vec3(dx, dy, dz);
        Vec3 norm = dir.lengthSqr() > 1e-6 ? dir.normalize().scale(length) : Vec3.ZERO;
        line(pose, start, start.add(norm));
    }

    public static void fillPoly(PoseStack pose, float x, float y, float z, int sides, float radius, float rotationDeg) {
        if (radius <= 0.001F || sides < 3) return;
        RenderBuilder rb = builder();
        Matrix4f mat = pose.last().pose();

        float rotRad = (float) Math.toRadians(rotationDeg);
        float step = (Mth.TWO_PI) / sides;

        for (int i = 0; i < sides; i++) {
            float a1 = rotRad + i * step;
            float a2 = rotRad + (i + 1) * step;

            QUAD[0].set(x, y, z);
            QUAD[1].set(x + Mth.cos(a1) * radius, y, z + Mth.sin(a1) * radius);
            QUAD[2].set(x + Mth.cos(a2) * radius, y, z + Mth.sin(a2) * radius);
            QUAD[3].set(x, y, z);
            rb.renderQuad(mat, QUAD);
        }
    }

    public static void tri(PoseStack pose, float x, float y, float z, float width, float length, float rotationDeg) {
        float rad = (float) Math.toRadians(rotationDeg);
        float cos = Mth.cos(rad);
        float sin = Mth.sin(rad);
        float hw = width * 0.5F;

        QUAD[0].set(x - sin * hw,     y, z + cos * hw);
        QUAD[1].set(x + sin * hw,     y, z - cos * hw);
        QUAD[2].set(x + cos * length, y, z + sin * length);
        QUAD[3].set(x + cos * length, y, z + sin * length);
        builder().renderQuad(pose.last().pose(), QUAD);
    }

    public static void sphere(PoseStack pose, float x, float y, float z, float radius) {
        pose.pushPose();
        pose.translate(x, y, z);
        builder().renderSphere(pose, radius, 16, 16);
        pose.popPose();
    }

    public static void dome(PoseStack pose, float x, float y, float z, float radius) {
        pose.pushPose();
        pose.translate(x, y, z);
        builder().renderSemiSphere(pose, radius, 16, 16);
        pose.popPose();
    }
}
