package pro.komaru.tridot.util.math;

import pro.komaru.tridot.util.phys.Vec2f;

public class Direction2 {
    public static Vec2f[] d4 = new Vec2f[] {
            new Vec2f(1, 0),
            new Vec2f(0, 1),
            new Vec2f(-1, 0),
            new Vec2f(0, -1)
    };
    public static Vec2f[] d8 = new Vec2f[] {
            new Vec2f(0, 1),
            new Vec2f(1, 1),
            new Vec2f(1, 0),
            new Vec2f(1, -1),
            new Vec2f(0, -1),
            new Vec2f(-1, -1),
            new Vec2f(-1, 0),
            new Vec2f(-1, 1),
    };
}
