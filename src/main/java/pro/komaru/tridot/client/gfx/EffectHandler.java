package pro.komaru.tridot.client.gfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pro.komaru.tridot.Tridot;
import pro.komaru.tridot.api.fx.Effect;
import pro.komaru.tridot.api.fx.EffectContainer;
import pro.komaru.tridot.client.gfx.lightningBolt.LightningInstance;
import pro.komaru.tridot.client.render.Draw3D;
import pro.komaru.tridot.client.render.TridotRenderTypes;
import pro.komaru.tridot.util.Col;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Tridot.ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EffectHandler {
    private static final List<EffectContainer> ACTIVE = new ArrayList<>();
    private static final List<LightningInstance> LIGHTNINGS = new ArrayList<>();

    public static void addLighting(LightningInstance lightningInstance) {
        LIGHTNINGS.add(lightningInstance);
    }

    public static void spawn(Effect effect, Level level, double x, double y, double z, float yaw, float pitch, Col color, Object data, int seed) {
        if (effect == null || level == null) return;

        EffectContainer container = new EffectContainer();
        container.effect = effect;
        container.level = level;
        container.x = x;
        container.y = y;
        container.z = z;
        container.rotation = yaw;
        container.pitch = pitch;
        container.color = color != null ? color : Col.white;
        container.data = data;
        container.id = seed;
        container.time = 0.0F;
        container.partialTicks = 0.0F;
        container.lifetime = effect.lifetime;

        effect.renderer.render(container);
        if (effect.lifetime > 1.0F) {
            ACTIVE.add(container);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;

        for (int i = ACTIVE.size() - 1; i >= 0; i--) {
            EffectContainer e = ACTIVE.get(i);
            e.partialTicks = 0.0F;
            e.time += 1.0F;
            if (e.time >= e.lifetime) {
                ACTIVE.remove(i);
            } else {
                e.effect.renderer.render(e);
            }
        }

        for (int i = LIGHTNINGS.size() - 1; i >= 0; i--) {
            LightningInstance lightningInstance = LIGHTNINGS.get(i);
            lightningInstance.tick();
            if (lightningInstance.age >= lightningInstance.getLifetime()) {
                LIGHTNINGS.remove(i);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        var frustum = event.getFrustum();
        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        PoseStack pose = event.getPoseStack();
        Vec3 cam = event.getCamera().getPosition();
        float partialTick = mc.isPaused() ? 0.0F : event.getPartialTick();

        for (int i = ACTIVE.size() - 1; i >= 0; i--) {
            EffectContainer e = ACTIVE.get(i);
            if (e.effect.drawer != null) {
                float clip = e.effect.clip;
                double r = 8.0;
                if (cam.distanceToSqr(e.x, e.y, e.z) <= clip * clip && frustum.isVisible(new AABB(e.x - r, e.y - r, e.z - r, e.x + r, e.y + r, e.z + r))) {
                    e.partialTicks = partialTick;
                    pose.pushPose();
                    pose.translate(e.x - cam.x, e.y - cam.y, e.z - cam.z);
                    Draw3D.color();
                    e.effect.drawer.draw(e, pose);
                    pose.popPose();
                    e.partialTicks = 0.0F;
                }
            }
        }

        TridotRenderTypes.getDelayedRender().endBatch(TridotRenderTypes.TRANSLUCENT);
        TridotRenderTypes.getDelayedRender().endBatch(TridotRenderTypes.ADDITIVE);

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        for (LightningInstance lightningInstance : LIGHTNINGS) {
            float clip = 64.0f;
            var s = lightningInstance.start;
            var end = lightningInstance.end;
            if (cam.distanceToSqr(s.x, s.y, s.z) <= clip * clip && frustum.isVisible(new AABB(s.x, s.y, s.z, end.x, end.y, end.z).inflate(2.0))) {
                lightningInstance.renderTick(pose, buffers);
            }
        }
        pose.popPose();
        buffers.endBatch();
    }
}
