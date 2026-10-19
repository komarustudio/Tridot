package pro.komaru.tridot.client.render.trail;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.*;
import net.neoforged.neoforge.client.event.*;
import org.joml.*;
import pro.komaru.tridot.*;
import pro.komaru.tridot.client.*;
import pro.komaru.tridot.client.render.*;

import java.lang.Math;
import java.util.*;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, modid = Tridot.ID, bus = EventBusSubscriber.Bus.GAME)
public final class SlashTrailRenderer {
    private SlashTrailRenderer() {}
    private static final List<Slash> ACTIVE = new ArrayList<>();

    private static final class Slash {
        final Entity owner;
        final TrailSpec spec;
        float yaw;
        float pitch;
        Vec3 anchor;
        int age;

        Slash(Entity owner, TrailSpec spec, float yaw, float pitch, Vec3 anchor) {
            this.owner = owner;
            this.spec = spec;
            this.yaw = yaw;
            this.pitch = pitch;
            this.anchor = anchor;
        }
    }

    public static void spawn(Entity owner, TrailSpec spec) {
        if (owner == null || spec == null) return;
        float pitch = switch (spec.type()) {
            case ARC -> Mth.clamp(owner.getXRot(), -30.0F, 30.0F);
            case THRUST -> spec.followOwner() ? owner.getXRot() : 0.0F;
        };

        ACTIVE.add(new Slash(owner, spec, owner.getYRot(), pitch, owner.position()));
    }

    public static void spawn(Entity owner, TrailSpec spec, int delay) {
        if (spec != null) {
            spec.delay(delay);
        }

        spawn(owner, spec);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().level == null) {
            ACTIVE.clear();
            return;
        }

        if (Minecraft.getInstance().isPaused()) return;
        ACTIVE.removeIf(s -> {
            s.age++;
            if (!s.spec.followOwner() && s.age <= s.spec.delay()) {
                s.anchor = s.owner.position();
                s.yaw = s.owner.getYRot();
                s.pitch = switch (s.spec.type()) {
                    case ARC -> Mth.clamp(s.owner.getXRot(), -30.0F, 30.0F);
                    case THRUST -> s.owner.getXRot();
                };
            }

            return s.age >= s.spec.delay() + s.spec.lifetime() || (s.spec.followOwner() && s.owner.isRemoved());
        });
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        var frustum = event.getFrustum();
        Vec3 cam = event.getCamera().getPosition();
        float partialTick = ClientTick.mcPartialTick();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(TridotRenderTypes.WEAPON_TRAIL);

        for (Slash s : ACTIVE) {
            if (s.age < s.spec.delay()) {
                continue;
            }
            float activeAge = (s.age - s.spec.delay()) + partialTick;
            float life = activeAge / (float) s.spec.lifetime();
            if (life >= 1.0F) continue;
            Vec3 base = s.spec.followOwner() ? s.owner.getPosition(partialTick) : s.anchor;

            float clip = 32;
            double r = 8.0;
            if (cam.distanceToSqr(base.x, base.y, base.z) <= clip * clip && frustum != null && frustum.isVisible(new AABB(base.x - r, base.y - r, base.z - r, base.x + r, base.y + r, base.z + r))) {
                boolean isFirstPerson = (s.owner == mc.player && mc.options.getCameraType().isFirstPerson());
                poseStack.pushPose();
                poseStack.translate(base.x - cam.x, base.y + s.owner.getEyeHeight() + s.spec.heightOffset() - cam.y, base.z - cam.z);

                poseStack.mulPose(Axis.YP.rotationDegrees(-s.yaw));
                poseStack.mulPose(Axis.XP.rotationDegrees(s.pitch));
                poseStack.mulPose(Axis.ZP.rotationDegrees(s.spec.roll()));

                if (isFirstPerson) {
                    poseStack.translate(s.spec.sideOffset(), -0.15F, 0.0F);
                    poseStack.mulPose(Axis.XP.rotationDegrees(16.0F));
                } else {
                    poseStack.translate(s.spec.sideOffset(), 0.0F, 0.0F);
                }

                Matrix4f pose = poseStack.last().pose();
                switch (s.spec.type()) {
                    case ARC -> renderArc(consumer, pose, s.spec, life);
                    case THRUST -> renderThrust(consumer, pose, s.spec, life);
                }

                poseStack.popPose();
            }
        }

        buffers.endBatch(TridotRenderTypes.WEAPON_TRAIL);
    }

    private static void renderArc(VertexConsumer vc, Matrix4f m, TrailSpec spec, float life) {
        float sweep = Mth.clamp(life / spec.slashTime(), 0.0F, 1.0F);
        float head = spec.interp().apply(sweep);
        float tailT = Mth.clamp((life - 0.2F) / 0.8F, 0.0F, 1.0F);
        float tail = tailT * tailT;

        float a0 = Mth.lerp(tail, spec.from(), spec.to());
        float a1 = Mth.lerp(head, spec.from(), spec.to());
        if (Math.abs(a1 - a0) < 0.5F) return;

        float fade = life < 0.7F ? 1.0F : 1.0F - (life - 0.7F) / 0.3F;

        int hr = (spec.color() >> 16) & 0xFF, hg = (spec.color() >> 8) & 0xFF, hb = spec.color() & 0xFF;
        int tr = (spec.tailColor() >> 16) & 0xFF, tg = (spec.tailColor() >> 8) & 0xFF, tb = spec.tailColor() & 0xFF;
        for (int i = 0; i < spec.segment(); i++) {
            float t0 = (float) i / spec.segment();
            float t1 = (float) (i + 1) / spec.segment();
            float ang0 = Mth.lerp(t0, a0, a1) * Mth.DEG_TO_RAD;
            float ang1 = Mth.lerp(t1, a0, a1) * Mth.DEG_TO_RAD;

            int r0 = (int) Mth.lerp(t0, tr, hr);
            int g0 = (int) Mth.lerp(t0, tg, hg);
            int b0 = (int) Mth.lerp(t0, tb, hb);

            int r1 = (int) Mth.lerp(t1, tr, hr);
            int g1 = (int) Mth.lerp(t1, tg, hg);
            int b1 = (int) Mth.lerp(t1, tb, hb);

            int cr0 = (r0 + 255) / 2, cg0 = (g0 + 255) / 2, cb0 = (b0 + 255) / 2;
            int cr1 = (r1 + 255) / 2, cg1 = (g1 + 255) / 2, cb1 = (b1 + 255) / 2;

            int al0 = (int) (255 * fade * (0.15F + 0.85F * t0 * t0));
            int al1 = (int) (255 * fade * (0.15F + 0.85F * t1 * t1));

            float w0 = spec.width() * (spec.crescent() ? (0.08F + 0.92F * Mth.sin(t0 * (float) Math.PI)) : 1.0F);
            float w1 = spec.width() * (spec.crescent() ? (0.08F + 0.92F * Mth.sin(t1 * (float) Math.PI)) : 1.0F);

            float halfH0 = w0 * 0.35F;
            float halfH1 = w1 * 0.35F;

            float rOut0 = spec.radius();
            float rMid0 = spec.radius() - w0 * 0.3F;
            float rIn0 = Math.max(0.05F, spec.radius() - w0);

            float rOut1 = spec.radius();
            float rMid1 = spec.radius() - w1 * 0.3F;
            float rIn1 = Math.max(0.05F, spec.radius() - w1);

            float s0 = Mth.sin(ang0), c0 = Mth.cos(ang0);
            float s1 = Mth.sin(ang1), c1 = Mth.cos(ang1);

            vc.addVertex(m, s0 * rIn0, +halfH0, c0 * rIn0).setColor(r0, g0, b0, 0);
            vc.addVertex(m, s0 * rMid0, 0, c0 * rMid0).setColor(cr0, cg0, cb0, al0);
            vc.addVertex(m, s1 * rMid1, 0, c1 * rMid1).setColor(cr1, cg1, cb1, al1);
            vc.addVertex(m, s1 * rIn1, +halfH1, c1 * rIn1).setColor(r1, g1, b1, 0);

            vc.addVertex(m, s0 * rMid0, 0, c0 * rMid0).setColor(cr0, cg0, cb0, al0);
            vc.addVertex(m, s0 * rIn0, -halfH0, c0 * rIn0).setColor(r0, g0, b0, 0);
            vc.addVertex(m, s1 * rIn1, -halfH1, c1 * rIn1).setColor(r1, g1, b1, 0);
            vc.addVertex(m, s1 * rMid1, 0, c1 * rMid1).setColor(cr1, cg1, cb1, al1);

            vc.addVertex(m, s0 * rMid0, 0, c0 * rMid0).setColor(cr0, cg0, cb0, al0);
            vc.addVertex(m, s0 * rOut0, 0, c0 * rOut0).setColor(r0, g0, b0, 0);
            vc.addVertex(m, s1 * rOut1, 0, c1 * rOut1).setColor(r1, g1, b1, 0);
            vc.addVertex(m, s1 * rMid1, 0, c1 * rMid1).setColor(cr1, cg1, cb1, al1);
        }
    }

    private static void renderThrust(VertexConsumer vc, Matrix4f m, TrailSpec spec, float life) {
        float extendT = Mth.clamp(life / 0.3F, 0.0F, 1.0F);
        float extend = 1.0F - (1.0F - extendT) * (1.0F - extendT) * (1.0F - extendT);
        float tailT = Mth.clamp((life - 0.15F) / 0.85F, 0.0F, 1.0F);

        float start = 0.4F;
        float zTip = start + (spec.radius() - start) * extend;
        float zTail = start + (zTip - start) * tailT * tailT;
        if (zTip - zTail < 0.05F) return;
        float zMid = zTail + (zTip - zTail) * 0.75F;

        float fade = life < 0.6F ? 1.0F : 1.0F - (life - 0.6F) / 0.4F;
        int alpha = (int) (255 * fade);

        int hr = (spec.color() >> 16) & 0xFF, hg = (spec.color() >> 8) & 0xFF, hb = spec.color() & 0xFF;
        int tr = (spec.tailColor() >> 16) & 0xFF, tg = (spec.tailColor() >> 8) & 0xFF, tb = spec.tailColor() & 0xFF;

        float w = spec.width();
        for (int plane = 0; plane < 2; plane++) {
            float wx = plane == 0 ? w : 0.0F;
            float wy = plane == 0 ? 0.0F : w;

            vc.addVertex(m, 0, 0, zTail).setColor(tr, tg, tb, 0);
            vc.addVertex(m, wx, wy, zMid).setColor(hr, hg, hb, alpha);
            vc.addVertex(m, -wx, -wy, zMid).setColor(hr, hg, hb, alpha);
            vc.addVertex(m, 0, 0, zTail).setColor(tr, tg, tb, 0);

            vc.addVertex(m, -wx, -wy, zMid).setColor(hr, hg, hb, alpha);
            vc.addVertex(m, wx, wy, zMid).setColor(hr, hg, hb, alpha);
            vc.addVertex(m, 0, 0, zTip).setColor(255, 255, 255, alpha);
            vc.addVertex(m, 0, 0, zTip).setColor(255, 255, 255, alpha);
        }
    }
}
