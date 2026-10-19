package pro.komaru.tridot.client.render.trail;

import pro.komaru.tridot.util.Col;
import pro.komaru.tridot.util.math.Interp;

public class TrailSpec {
    public enum Type { ARC, THRUST }

    private final Type type;
    private float roll;
    private float from;
    private float to;
    private float slashTime = 0.75f;
    private float radius = 2;
    private float width = 0.65F;
    private float sideOffset = 0.0F;
    private float heightOffset = -0.3F;
    private int delay = 0;
    private int lifetime;
    private int color = 0xFFFFFF;
    private int tailColor = 0xFFFFFF;
    private int segment = 24;
    private boolean crescent = true;
    private boolean followOwner = true;
    private Interp interp = Interp.swing;

    public TrailSpec(Type type, float roll, float from, float to, int lifetime) {
        this.type = type;
        this.roll = roll;
        this.from = from;
        this.to = to;
        this.lifetime = lifetime;
    }

    public TrailSpec(Type type, float roll, float from, float to, float radius, float width, float sideOffset, float heightOffset, int lifetime, int color, int tailColor, boolean crescent, boolean followOwner, Interp interp) {
        this.type = type;
        this.roll = roll;
        this.from = from;
        this.to = to;
        this.radius = radius;
        this.width = width;
        this.sideOffset = sideOffset;
        this.heightOffset = heightOffset;
        this.lifetime = lifetime;
        this.color = color;
        this.tailColor = tailColor;
        this.crescent = crescent;
        this.followOwner = followOwner;
        this.interp = interp != null ? interp : Interp.smooth;
    }

    public static TrailSpec arc(float roll, float from, float to, int lifetime) {
        return new TrailSpec(Type.ARC, roll, from, to, lifetime);
    }

    public static TrailSpec thrust(float length, int lifetime) {
        return new TrailSpec(Type.THRUST, 0.0F, 0.0F, 0.0F, lifetime).radius(length).crescent(false);
    }

    public static TrailSpec spin(float roll, int lifetime) {
        return new TrailSpec(Type.ARC, roll, -170.0F, 190.0F, lifetime);
    }

    public static TrailSpec arc(float roll, float from, float to, float radius, float width, int lifetime, int color) {
        return arc(roll, from, to, lifetime).radius(radius).width(width).color(color);
    }

    public static TrailSpec arc(float roll, float from, float to, float radius, float width, int lifetime, int color, Interp interp) {
        return arc(roll, from, to, lifetime).radius(radius).width(width).color(color).interpolation(interp);
    }

    public static TrailSpec gradientArc(float roll, float from, float to, float radius, float width, int lifetime, int headColor, int tailColor) {
        return arc(roll, from, to, lifetime).radius(radius).width(width).gradient(headColor, tailColor);
    }

    public static TrailSpec gradientArc(float roll, float from, float to, float radius, float width, int lifetime, int headColor, int tailColor, Interp interp) {
        return arc(roll, from, to, lifetime).radius(radius).width(width).gradient(headColor, tailColor).interpolation(interp);
    }

    public static TrailSpec flatArc(float roll, float from, float to, float radius, float width, int lifetime, int color) {
        return arc(roll, from, to, lifetime).radius(radius).width(width).color(color).crescent(false);
    }

    public static TrailSpec flatArc(float roll, float from, float to, float radius, float width, int lifetime, int color, Interp interp) {
        return arc(roll, from, to, lifetime).radius(radius).width(width).color(color).crescent(false).interpolation(interp);
    }

    public static TrailSpec spin(float roll, float radius, float width, int lifetime, int headColor, int tailColor) {
        return spin(roll, lifetime).radius(radius).width(width).gradient(headColor, tailColor);
    }

    public static TrailSpec spin(float roll, float radius, float width, int lifetime, int headColor, int tailColor, Interp interp) {
        return spin(roll, lifetime).radius(radius).width(width).gradient(headColor, tailColor).interpolation(interp);
    }

    public static TrailSpec thrust(float length, float halfWidth, float sideOffset, int lifetime, int color) {
        return thrust(length, lifetime).width(halfWidth).sideOffset(sideOffset).color(color);
    }

    public static TrailSpec thrust(float length, float halfWidth, float sideOffset, int lifetime, int color, Interp interp) {
        return thrust(length, lifetime).width(halfWidth).sideOffset(sideOffset).color(color).interpolation(interp);
    }

    public static TrailSpec gradientThrust(float length, float halfWidth, float sideOffset, int lifetime, int headColor, int tailColor) {
        return thrust(length, lifetime).width(halfWidth).sideOffset(sideOffset).gradient(headColor, tailColor);
    }

    public static TrailSpec gradientThrust(float length, float halfWidth, float sideOffset, int lifetime, int headColor, int tailColor, Interp interp) {
        return thrust(length, lifetime).width(halfWidth).sideOffset(sideOffset).gradient(headColor, tailColor).interpolation(interp);
    }

    public TrailSpec setSlashTime(float time) { this.slashTime = time; return this; }
    public TrailSpec slashTime(float time) { return setSlashTime(time); }

    public TrailSpec setSegments(int seg) { this.segment = seg; return this; }
    public TrailSpec segments(int seg) { return setSegments(seg); }

    public TrailSpec setRadius(float radius) { this.radius = radius; return this; }
    public TrailSpec radius(float radius) { return setRadius(radius); }

    public TrailSpec setWidth(float width) { this.width = width; return this; }
    public TrailSpec width(float width) { return setWidth(width); }

    public TrailSpec setXOffset(float xOffset) { this.sideOffset = xOffset; return this; }
    public TrailSpec xOffset(float xOffset) { return setXOffset(xOffset); }
    public TrailSpec setSideOffset(float sideOffset) { this.sideOffset = sideOffset; return this; }
    public TrailSpec sideOffset(float sideOffset) { return setSideOffset(sideOffset); }

    public TrailSpec setYOffset(float yOffset) { this.heightOffset = yOffset; return this; }
    public TrailSpec yOffset(float yOffset) { return setYOffset(yOffset); }
    public TrailSpec setHeightOffset(float heightOffset) { this.heightOffset = heightOffset; return this; }
    public TrailSpec heightOffset(float heightOffset) { return setHeightOffset(heightOffset); }

    public TrailSpec setColor(int color) {
        this.color = color;
        this.tailColor = color;
        return this;
    }
    public TrailSpec color(int color) { return setColor(color); }

    public TrailSpec setColor(int r, int g, int b) {
        return setColor(((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    public TrailSpec setColor(Col col) {
        return col != null ? setColor(col.argb8888()) : this;
    }
    public TrailSpec color(Col col) { return setColor(col); }

    public TrailSpec setTailColor(int tailColor) { this.tailColor = tailColor; return this; }
    public TrailSpec tailColor(int tailColor) { return setTailColor(tailColor); }

    public TrailSpec setTailColor(int r, int g, int b) {
        return setTailColor(((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    public TrailSpec setTailColor(Col col) {
        return col != null ? setTailColor(col.argb8888()) : this;
    }
    public TrailSpec tailColor(Col col) { return setTailColor(col); }

    public TrailSpec gradient(int headColor, int tailColor) {
        this.color = headColor;
        this.tailColor = tailColor;
        return this;
    }

    public TrailSpec gradient(Col head, Col tail) {
        return setColor(head).setTailColor(tail);
    }

    public TrailSpec crescent(boolean crescent) { this.crescent = crescent; return this; }
    public TrailSpec setCrescent(boolean crescent) { this.crescent = crescent; return this; }

    public TrailSpec setLifetime(int lifetime) { this.lifetime = lifetime; return this; }
    public TrailSpec lifetime(int lifetime) { return setLifetime(lifetime); }

    public TrailSpec setDelay(int delay) { this.delay = Math.max(0, delay); return this; }
    public TrailSpec delay(int delay) { return setDelay(delay); }

    public TrailSpec followsOwner(boolean follows) { this.followOwner = follows; return this; }
    public TrailSpec followOwner(boolean follows) { return followsOwner(follows); }
    public TrailSpec setFollowOwner(boolean follows) { return followsOwner(follows); }
    public TrailSpec detached() { this.followOwner = false; return this; }

    public TrailSpec setInterpolation(Interp interp) { this.interp = interp != null ? interp : Interp.smooth; return this; }
    public TrailSpec interpolation(Interp interp) { return setInterpolation(interp); }
    public TrailSpec interp(Interp interp) { return setInterpolation(interp); }

    public TrailSpec copy() {
        TrailSpec copy = new TrailSpec(this.type, this.roll, this.from, this.to, this.lifetime);
        copy.delay = this.delay;
        copy.radius = this.radius;
        copy.width = this.width;
        copy.sideOffset = this.sideOffset;
        copy.heightOffset = this.heightOffset;
        copy.color = this.color;
        copy.tailColor = this.tailColor;
        copy.crescent = this.crescent;
        copy.followOwner = this.followOwner;
        copy.interp = this.interp;
        copy.segment = this.segment;
        copy.slashTime = this.slashTime;
        return copy;
    }

    public TrailSpec mirrored() {
        TrailSpec copy = this.copy();
        copy.roll = -this.roll;
        copy.from = -this.from;
        copy.to = -this.to;
        copy.sideOffset = -this.sideOffset;
        return copy;
    }

    public TrailSpec withCrescent(boolean crescent) {
        TrailSpec copy = this.copy();
        copy.crescent = crescent;
        return copy;
    }

    public Type type() { return type; }
    public float roll() { return roll; }
    public float from() { return from; }
    public float to() { return to; }
    public float radius() { return radius; }
    public float width() { return width; }
    public float slashTime() { return slashTime; }
    public float sideOffset() { return sideOffset; }
    public float heightOffset() { return heightOffset; }
    public int delay() { return delay; }
    public int lifetime() { return lifetime; }
    public int segment() { return segment; }
    public int color() { return color; }
    public int tailColor() { return tailColor; }
    public boolean crescent() { return crescent; }
    public boolean followOwner() { return followOwner; }
    public Interp interp() { return interp != null ? interp : Interp.smooth; }
}
