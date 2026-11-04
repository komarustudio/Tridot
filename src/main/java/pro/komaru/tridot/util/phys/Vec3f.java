package pro.komaru.tridot.util.phys;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import pro.komaru.tridot.util.comps.phys.Pos3;
import pro.komaru.tridot.util.comps.phys.X;
import pro.komaru.tridot.util.comps.phys.Y;
import pro.komaru.tridot.util.comps.phys.Z;
import pro.komaru.tridot.util.math.Interp;

import java.io.Serializable;

public class Vec3f implements Pos3, Serializable {
    public static final Vec3f ZERO = new Vec3f();
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

    public static Vec3f from(Vec3 mc) {
        return new Vec3f((float) mc.x, (float) mc.y, (float) mc.z);
    }

    @Deprecated
    public static Vec3f zero() {
        return new Vec3f();
    }

    public boolean isZero() {
        return x == 0f && y == 0f && z == 0f;
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
        this.x = value;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Vec3f y(float value) {
        this.y = value;
        return this;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public Vec3f z(float value) {
        this.z = value;
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

    public Vec3f set(Vec3 other) {
        return set((float) other.x(), (float) other.y(), (float) other.z());
    }

    public Vec3f set(Pos3 other) {
        return set(other.x(), other.y(), other.z());
    }

    public Vec3f set(float x, float y, float z) {
        this.x(x);
        this.y(y);
        this.z(z);
        return this;
    }

    public Vec3f clear() {
        this.x(0);
        this.y(0);
        this.z(0);
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

    public Vec3 mcVec() {
        return new Vec3(x(), y(), z());
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

    public Vec3f lerp(float x, float y, float z, float progress) {
        return lerp(x, y, z, progress, Interp.linear);
    }

    public Vec3f lerp(float x, float y, float z, float progress, Interp interp) {
        float lx = interp.apply(progress, x(), x);
        float ly = interp.apply(progress, y(), y);
        float lz = interp.apply(progress, z(), z);
        return set(lx, ly, lz);
    }

    public Vec3f lerp(Vec3 pos, float progress) {
        return lerp((float) pos.x, (float) pos.y, (float) pos.z, progress, Interp.linear);
    }

    public Vec3f lerp(Vec3 pos, float progress, Interp interp) {
        return lerp((float) pos.x, (float) pos.y, (float) pos.z, progress, interp);
    }

    public Vec3f rotX(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float y = y() * cos - z() * sin;
        float z = y() * sin + z() * cos;
        y(y);
        z(z);
        return this;
    }

    public Vec3f rotY(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float x = x() * cos + z() * sin;
        float z = -x() * sin + z() * cos;
        x(x);
        z(z);
        return this;
    }

    public Vec3f rotZ(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float x = x() * cos - y() * sin;
        float y = x() * sin + y() * cos;
        x(x);
        y(y);
        return this;
    }

    public float dst(float x, float y, float z) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float dst(Pos3 b) {
        return this.dst(b.x(), b.y(), b.z());
    }

    public float dst(Vec3 b) {
        return dst((float) b.x, (float) b.y, (float) b.z);
    }

    public float dst2(float x, float y, float z) {
        float dx = this.x - x;
        float dy = this.y - y;
        float dz = this.z - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public float dst2(Pos3 b) {
        return dst2(b.x(), b.y(), b.z());
    }

    public float dst2(Vec3 b) {
        return dst2((float) b.x, (float) b.y, (float) b.z);
    }

    public boolean posEquals(Pos3 b) {
        return b.x() == x() && b.y() == y() && b.z() == z();
    }
}
