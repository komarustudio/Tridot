package pro.komaru.tridot.client.cinema;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import pro.komaru.tridot.client.render.screenshake.ScreenshakeInstance;
import pro.komaru.tridot.util.math.Interp;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CutsceneNode{
    public Vec3 pos;
    public Interp easing;

    public float pitch;
    public float yaw;
    public int duration;
    public int fov = 90;
    @Nullable public Component component;
    @Nullable public SoundEvent event;
    @Nullable public ScreenshakeInstance screenshakeInstance;
    public Runnable onReach = () -> {};
    public Runnable onPlay = () -> {};
    public Runnable onEnd = () -> {};
    public Map<Integer, Consumer<CutsceneNode>> timedEvents = new HashMap<>();

    public CutsceneNode(Vec3 pos, Interp easing, float pitch, float yaw, int duration){
        this.pos = pos;
        this.easing = easing;
        this.pitch = pitch;
        this.yaw = yaw;
        this.duration = duration;
    }

    public CutsceneNode(Vec3 pos, Interp easing, int duration){
        this.pos = pos;
        this.easing = easing;
        this.pitch = 0;
        this.yaw = 0;
        this.duration = duration;
    }

    public CutsceneNode fade(float targetAlpha, int durationTicks) {
        return this.onReach(() -> CutsceneManager.fade(targetAlpha, durationTicks));
    }

    public CutsceneNode fadeOut(float targetAlpha, int durationTicks) {
        return this.onEnd(() -> CutsceneManager.fade(targetAlpha, durationTicks));
    }

    public CutsceneNode setTargetGlow(Entity target, boolean glowing) {
        return this.onPlay(() -> target.setGlowingTag(glowing));
    }

    public CutsceneNode disableGlow(Entity target) {
        return this.onEnd(() -> target.setGlowingTag(false));
    }

    public CutsceneNode onPlay(Runnable action) {
        this.onPlay = action;
        return this;
    }

    public CutsceneNode onEnd(Runnable action) {
        this.onEnd = action;
        return this;
    }

    public CutsceneNode onReach(Runnable action) {
        this.onReach = action;
        return this;
    }

    public CutsceneNode runAt(int tick, Consumer<CutsceneNode> action) {
        this.timedEvents.compute(tick, (k, existingAction) ->
                existingAction == null ? action : (node) -> { existingAction.accept(node); action.accept(node); }
        );

        return this;
    }

    public CutsceneNode copy(int duration) {
        CutsceneNode node = new CutsceneNode(this.pos, this.easing, this.pitch, this.yaw, duration);
        node.fov = this.fov;
        return node;
    }

    public CutsceneNode playSound(SoundEvent event) {
        this.event = event;
        return this;
    }

    public CutsceneNode addSubtitle(Component component) {
        this.component = component;
        return this;
    }

    public CutsceneNode addScreenShake(ScreenshakeInstance screenshakeInstance) {
        this.screenshakeInstance = screenshakeInstance;
        return this;
    }

    public CutsceneNode setFov(int fov) {
        this.fov = fov;
        return this;
    }

    public CutsceneNode yawToTarget(Vec3 target) {
        double dx = target.x - pos.x;
        double dz = target.z - pos.z;
        yaw = (float)(Math.atan2(dz, dx) * (180 / Math.PI)) - 90.0F;
        return this;
    }

    public CutsceneNode yaw(float yaw) {
        this.yaw = yaw;
        return this;
    }

    public CutsceneNode pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    public CutsceneNode pitchToTarget(Vec3 target) {
        double dx = target.x - pos.x;
        double dy = target.y - pos.y;
        double dz = target.z - pos.z;
        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        pitch = (float)(-(Math.atan2(dy, distanceXZ) * (180 / Math.PI)));
        return this;
    }

    public CutsceneNode keepPreviousLook(CutsceneNode previousNode) {
        this.pitch = previousNode.pitch;
        this.yaw = previousNode.yaw;
        this.fov = previousNode.fov;
        return this;
    }
}
