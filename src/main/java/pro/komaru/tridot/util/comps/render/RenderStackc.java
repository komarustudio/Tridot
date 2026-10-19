package pro.komaru.tridot.util.comps.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import pro.komaru.tridot.util.comps.phys.*;
import pro.komaru.tridot.util.phys.Vec3f;
import pro.komaru.tridot.util.struct.func.Cons;

public interface RenderStackc {
    PoseStack stack();

    default RenderStackc push() {
        return pose(PoseStack::pushPose);
    }
    default RenderStackc pop() {
        return pose(PoseStack::popPose);
    }
    default RenderStackc reset() {
        return pop().push();
    }
    default RenderStackc pose(Cons<PoseStack> cons) {
        cons.get(stack());
        return this;
    }

    default RenderStackc trnsZ(float layer) {
        return move(0, 0, layer);
    }
    default RenderStackc layer(float layer) {
        return trnsZ(-trns().z + layer);
    }
    default <XY extends X & Y> RenderStackc move2(XY vec) {
        return move(vec.x(),vec.y());
    }
    default RenderStackc move(Pos2 vec) {
        return move(vec.x(),vec.y());
    }
    default RenderStackc move(float x, float y) {
        return move(x,y,0);
    }
    default <XYZ extends X & Y & Z> RenderStackc move3(XYZ vec) {
        return move(vec.x(), vec.y(), vec.z());
    }
    default RenderStackc move(Pos3 vec) {
        return move(vec.x(),vec.y(),vec.z());
    }
    default RenderStackc move(float x, float y, float z) {
        return pose(s -> s.translate(x, y, z));
    }

    default RenderStackc scale(float xy) {
        return scale(xy, xy);
    }
    default <XY extends X & Y> RenderStackc scale2(XY xy) {
        return scale(xy.x(), xy.y());
    }
    default <XYZ extends X & Y & Z> RenderStackc scale3(XYZ xy) {
        return scale(xy.x(), xy.y(), xy.z());
    }
    default RenderStackc scale(Pos2 vec) {
        return scale(vec.x(), vec.y());
    }
    default RenderStackc scale(Pos3 vec) {
        return scale(vec.x(), vec.y(), vec.z());
    }
    default RenderStackc scale(float x, float y) {
        return scale(x,y,1f);
    }
    default RenderStackc scale(float x, float y, float z) {
        return pose(s -> s.scale(x, y, z));
    }

    default RenderStackc scale(Pos3 scl, Pos2 point) {
        return scale(scl.x(),scl.y(),scl.z(),point.x(),point.y());
    }
    default RenderStackc scale(Pos2 scl, Pos2 point) {
        return scale(scl.x(),scl.y(),point.x(),point.y());
    }
    default RenderStackc scale(float sclx, float scly, float px, float py) {
        move(px, py).scale(sclx, scly).move(-px, -py);
        return this;
    }
    default RenderStackc scale(float sclx, float scly, float sclz, float px, float py) {
        move(px, py).scale(sclx, scly, sclz).move(-px, -py);
        return this;
    }

    default RenderStackc rotateXYZ(Pos3 rot, Pos3 point) {
        return rotateXYZ(rot.x(), rot.y(), rot.z(), point.x(), point.y(), point.z());
    }
    default RenderStackc rotateXYZ(float x, float y, float z, float px, float py, float pz) {
        return move(px, py, pz)
                .rotateXYZ(x, y, z)
                .move(-px, -py, -pz);
    }
    default RenderStackc rotateXYZ(float x, float y, float z) {
        return pose(s -> s.mulPose(Axis.XP.rotationDegrees(x)))
                .pose(s -> s.mulPose(Axis.YP.rotationDegrees(y)))
                .pose(s -> s.mulPose(Axis.ZP.rotationDegrees(z)));
    }
    default RenderStackc rotate(float angle, Pos2 point) {
        return rotate(angle, point.x(), point.y());
    }
    default RenderStackc rotate(float angle, float px, float py) {
        move(px, py).rotate(angle).move(-px, -py);
        return this;
    }
    default RenderStackc rotate(float angle) {
        return pose(s -> s.mulPose(Axis.ZP.rotationDegrees(angle)));
    }

    default Vec3f trns() {
        var mat = matrix();
        return new Vec3f(mat.m30(), mat.m31(), mat.m32());
    }

    default Vec3f scl() {
        var mat = matrix();
        float sclX = (float) Math.sqrt(mat.m00() * mat.m00() + mat.m01() * mat.m01() + mat.m02() * mat.m02());
        float sclY = (float) Math.sqrt(mat.m10() * mat.m10() + mat.m11() * mat.m11() + mat.m12() * mat.m12());
        float sclZ = (float) Math.sqrt(mat.m20() * mat.m20() + mat.m21() * mat.m21() + mat.m22() * mat.m22());
        return new Vec3f(sclX, sclY, sclZ);
    }

    default Vec3f rot() {
        var mat = matrix();
        var scale = scl();

        float xYaw = mat.m00() / scale.x;
        float yYaw = mat.m10() / scale.x;
        float zYaw = mat.m20() / scale.x;

        float yaw = (float) Math.atan2(xYaw, zYaw);
        float pitch = (float) Math.asin(-yYaw);
        float roll = (float) Math.atan2(mat.m12() / scale.z, mat.m11() / scale.y);

        return new Vec3f(pitch, yaw, roll);
    }

    default Matrix4f matrix() {
        return stack().last().pose();
    }
}
