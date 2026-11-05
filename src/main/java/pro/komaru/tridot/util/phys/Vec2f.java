package pro.komaru.tridot.util.phys;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import pro.komaru.tridot.util.comps.phys.Pos2;
import pro.komaru.tridot.util.comps.phys.Pos3;
import pro.komaru.tridot.util.comps.phys.X;
import pro.komaru.tridot.util.comps.phys.Y;
import pro.komaru.tridot.util.math.Interp;

import java.io.Serializable;

public class Vec2f implements Pos2, Serializable {
    public static final Vec2f ZERO = new Vec2f();
    public float x,y;

    public Vec2f(float x, float y) {
        set(x,y);
    }

    public Vec2f(Pos2 pos) {
        set(pos);
    }

    public Vec2f() {
        set(0f,0f);
    }

    public Vec2f cpy() {
        return new Vec2f(this);
    }

    public boolean isZero() {
        return x == 0f && y == 0f;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Vec2f x(float value) {
        x = value;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Vec2f y(float value) {
        y = value;
        return this;
    }

    public <XY extends X & Y> Vec2f add(XY xy) {
        return add(xy,xy);
    }

    public Vec2f add(X x, Y y) {
        return add(x.x(),y.y());
    }

    public Vec2f add(float x, float y) {
        set(x()+x, y()+y);
        return this;
    }

    public <XY extends X & Y> Vec2f sub(XY xy) {
        return sub(xy,xy);
    }

    public Vec2f sub(X x, Y y) {
        return sub(x.x(),y.y());
    }

    public Vec2f sub(float x, float y) {
        return add(-x,-y);
    }

    public Vec2f set(Vec2 vec2) {
        return set(vec2.x, vec2.y);
    }

    public Vec2f set(Pos2 other) {
        return set(other.x(),other.y());
    }

    public Vec2f set(float x, float y) {
        x(x); this.y(y);
        return this;
    }

    public Vec2f scale(float scl) {
        return scale(scl,scl);
    }

    public <XY extends X & Y> Vec2f scale(XY xy) {
        return scale(xy,xy);
    }

    public Vec2f scale(X x, Y y) {
        return scale(x.x(),y.y());
    }

    public Vec2f scale(float x, float y) {
        set(x()*x, y()*y);
        return this;
    }

    public float len2() {
        return x()* x()+ y()* y();
    }

    public float len() {
        return (float) Math.sqrt(len2());
    }

    public Vec2f nor() {
        return scale(1/len());
    }

    public Vec2 mcVec() {
        return new Vec2(x(), y());
    }

    public CompoundTag toNbtVec() {
        var nbt = new CompoundTag();
        nbt.putFloat("x", x());
        nbt.putFloat("y", y());
        return nbt;
    }

    public Vec2f fromNbtVec(CompoundTag nbt) {
        return set(nbt.getFloat("x"),nbt.getFloat("y"));
    }

    public Vec2f lerp(Pos2 pos, float progress, Interp interp) {
        float x = interp.apply(progress, x(),pos.x());
        float y = interp.apply(progress, y(),pos.y());
        return set(x,y);
    }

    public Vec2f rotate(float degrees) {
        float radians = (float) Math.toRadians(degrees);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float x = x(), y = y();
        set(x * cos - y * sin, x * sin + y * cos);
        return this;
    }

    public float angle() {
        return (float) Math.toDegrees(Math.atan2(y(), x()));
    }

    public float angleBetween(Pos2 other) {
        return other.cpypos().vec().sub(this).angle();
    }

    public float cross(Pos2 other) {
        return x() * other.y() - y() * other.x();
    }

    public Vec2f rotationFix() {
        if(x() > 180) x(x()-360);
        if(y() > 180) this.y(y()-360);
        return this;
    }

    public float dst(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float dst(Pos2 b) {
        return this.dst(b.x(), b.y());
    }

    public float dst(Vec2 b) {
        return dst(b.x, b.y);
    }

    public float dst2(float x, float y) {
        float dx = this.x - x;
        float dy = this.y - y;
        return dx * dx + dy * dy;
    }

    public float dst2(Pos2 b) {
        return dst2(b.x(), b.y());
    }

    public float dst2(Vec2 b) {
        return dst2(b.x, b.y);
    }

    public boolean posEquals(Pos2 b) {
        return b.x() == x() && b.y() == y();
    }

    public void constrain(Vec2f target, float targetDist, boolean less, boolean greater) {
        var delta = target.cpy().sub(this);
        float dst = delta.len();

        if(less && dst < targetDist) return;
        if(greater && dst > targetDist) return;
        if (dst == 0) {
            add(targetDist, 0);
            return;
        }

        float scale = targetDist / dst;
        float newX = target.x() + delta.x() * scale;
        float newY = target.y() + delta.y() * scale;

        set(newX, newY);
    }

    public void constrain2(Vec2f b, float targetDist, boolean less, boolean greater) {
        var delta = b.cpy().sub(this);
        float dst = delta.len();

        if(less && dst < targetDist) return;
        if(greater && dst > targetDist) return;
        if (dst == 0) return;

        float diff = (dst - targetDist) / dst / 2f;

        float offsetX = delta.x() * diff;
        float offsetY = delta.y() * diff;

        add(offsetX, offsetY);
        b.sub(offsetX, offsetY);
    }

    public void reset() {
        set(0, 0);
    }
}