package pro.komaru.tridot.util.phys;

import net.minecraft.nbt.*;
import pro.komaru.tridot.util.comps.phys.*;
import pro.komaru.tridot.util.math.*;

import java.io.*;

public class Vec3f implements Pos3, Serializable {

    public float x, y, z;

    public Vec3f(float x, float y, float z) {
        set(x, y, z);
    }
    public Vec3f(double x, double y, double z) {
        set((float) x, (float) y, (float) z);
    }
    public Vec3f(Pos3 pos) {
        set(pos);
    }
    public Vec3f() {
        set(0f, 0f, 0f);
    }

    public static Vec3f from(net.minecraft.world.phys.Vec3 mc) {
        return new Vec3f((float) mc.x, (float) mc.y, (float) mc.z);
    }

    public static Vec3f zero() {
        return new Vec3f();
    }

    public Vec3f cpy() {
        return new Vec3f(this);
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Vec3f x(float value) {
        x = value;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Vec3f y(float value) {
        y = value;
        return this;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public Vec3f z(float value) {
        z = value;
        return this;
    }

    public Vec3f add(Pos3 xyz) {
        return add((X & Y & Z) xyz);
    }
    public <XYZ extends X & Y & Z> Vec3f add(XYZ xyz) {
        return add(xyz, xyz, xyz);
    }
    public Vec3f add(X x, Y y, Z z) {
        return add(x.x(), y.y(), z.z());
    }
    public Vec3f add(float x, float y, float z) {
        set(x() + x, y() + y, z() + z);
        return this;
    }

    public Vec3f sub(Pos3 xyz) {
        return sub((X & Y & Z) xyz);
    }
    public <XYZ extends X & Y & Z> Vec3f sub(XYZ xyz) {
        return sub(xyz, xyz, xyz);
    }
    public Vec3f sub(X x, Y y, Z z) {
        return sub(x.x(), y.y(), z.z());
    }
    public Vec3f sub(float x, float y, float z) {
        return add(-x, -y, -z);
    }

    public Vec3f set(Pos3 other) {
        return set(other.x(), other.y(), other.z());
    }
    public Vec3f set(float x, float y, float z) {
        this.x(x);
        this.y(y);
        z(z);
        return this;
    }

    public Vec3f scale(float scl) {
        return scale(scl, scl, scl);
    }
    public Vec3f scale(Pos3 xyz) {
        return scale((X & Y & Z) xyz);
    }
    public <XYZ extends X & Y & Z> Vec3f scale(XYZ xyz) {
        return scale(xyz, xyz, xyz);
    }
    public Vec3f scale(X x, Y y, Z z) {
        return scale(x.x(), y.y(), z.z());
    }
    public Vec3f scale(float x, float y, float z) {
        set(x() * x, y() * y, z() * z);
        return this;
    }

    public float len2() {
        return x() * x() + y() * y() + z() * z();
    }
    public float len() {
        return (float) Math.sqrt(len2());
    }

    public Vec3f nor() {
        return scale(1 / len());
    }

    public net.minecraft.world.phys.Vec3 mcVec() {
        return new net.minecraft.world.phys.Vec3(x(), y(), z());
    }
    public CompoundTag toNbtVec() {
        var nbt = new CompoundTag();
        nbt.putFloat("x", x());
        nbt.putFloat("y", y());
        nbt.putFloat("z", z());
        return nbt;
    }
    public Vec3f fromNbtVec(CompoundTag nbt) {
        return set(nbt.getFloat("x"), nbt.getFloat("y"), nbt.getFloat("z"));
    }

    public Vec3f lerp(Pos3 pos, float progress) {
        return lerp(pos, progress, Interp.linear);
    }
    public Vec3f lerp(Pos3 pos, float progress, Interp interp) {
        float x = interp.apply(progress, x(), pos.x());
        float y = interp.apply(progress, y(), pos.y());
        float z = interp.apply(progress, z(), pos.z());
        return set(x, y, z);
    }

    public Vec3f rotate(float degX, float degY, float degZ) {
        float radiansX = (float) Math.toRadians(degX);
        float radiansY = (float) Math.toRadians(degY);
        float radiansZ = (float) Math.toRadians(degZ);

        float cosX = (float) Math.cos(radiansX);
        float sinX = (float) Math.sin(radiansX);

        float cosY = (float) Math.cos(radiansY);
        float sinY = (float) Math.sin(radiansY);

        float cosZ = (float) Math.cos(radiansZ);
        float sinZ = (float) Math.sin(radiansZ);

        float x = x();
        float y = y();
        float z = z();

        float tempY = y * cosX - z * sinX;
        float tempZ = y * sinX + z * cosX;

        float tempX = x * cosY + tempZ * sinY;
        tempZ = -x * sinY + tempZ * cosY;

        set(tempX * cosZ - tempY * sinZ, tempX * sinZ + tempY * cosZ, tempZ);

        return this;
    }

    public Vec3f angle() {
        Pos3 normalized = cpy().nor();
        float yaw = (float) Math.atan2(normalized.z(), normalized.x());
        float pitch = (float) Math.asin(normalized.y());
        return set(pitch, yaw, 0);
    }

    public Vec3f angleBetween(Pos3 other) {
        return other.cpypos().vec().sub(this).angle().rotationFix();
    }

    public float cross(Pos3 other) {
        return x() * other.y() - y() * other.x();
    }

    public Vec3f rotationFix() {
        if (x() > 180) this.x(x() - 360);
        if (y() > 180) this.y(y() - 360);
        if (z() > 180) z(z() - 360);
        return this;
    }

    public float dst(Pos3 b) {
        return cpy().sub(b).len();
    }

    public float dst2(Pos3 b) {
        return cpy().sub(b).len2();
    }

    public boolean posEquals(Pos3 b) {
        return b.x() == x() && b.y() == y() && b.z() == z();
    }

    public void constrain(Vec3f target, float targetDist, boolean less, boolean greater) {
        var delta = target.cpy().sub(this);
        float dst = delta.len();

        if (less && dst < targetDist) return;
        if (greater && dst > targetDist) return;
        if (dst == 0) {
            add(targetDist, 0, 0);
            return;
        }

        float scale = targetDist / dst;
        float newX = target.x() + delta.x() * scale;
        float newY = target.y() + delta.y() * scale;
        float newZ = target.z() + delta.z() * scale;

        set(newX, newY, newZ);
    }

    public void constrain2(Vec3f b, float targetDist, boolean less, boolean greater) {
        var delta = b.cpy().sub(this);
        float dst = delta.len();

        if (less && dst < targetDist) return;
        if (greater && dst > targetDist) return;
        if (dst == 0) return;

        float diff = (dst - targetDist) / dst / 2f;

        float offsetX = delta.x() * diff;
        float offsetY = delta.y() * diff;
        float offsetZ = delta.z() * diff;

        add(offsetX, offsetY, offsetZ);
        b.sub(offsetX, offsetY, offsetZ);
    }
}
