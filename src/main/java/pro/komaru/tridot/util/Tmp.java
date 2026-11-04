package pro.komaru.tridot.util;

import net.minecraft.core.BlockPos;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import pro.komaru.tridot.util.comps.phys.Pos3;
import pro.komaru.tridot.util.math.ArcRandom;
import pro.komaru.tridot.util.phys.Vec2f;
import pro.komaru.tridot.util.phys.Vec3f;

public class Tmp {
    private Tmp() {}
    public static ArcRandom rnd = new ArcRandom();

    public static final Col c1 = new Col(0f,0f,0f,0f);
    public static final Col c2 = new Col(0f,0f,0f,0f);
    public static final Col c3 = new Col(0f,0f,0f,0f);

    public static final Vec2f v1 = new Vec2f(0.0F, 0.0F);
    public static final Vec2f v2 = new Vec2f(0.0F, 0.0F);

    public static final Vec3f v3 = new Vec3f(0.0F, 0.0F, 0.0F);
    public static final Vec3f v4 = new Vec3f(0.0F, 0.0F, 0.0F);

    public static final Vector3f v5 = new Vector3f();
    public static final Vector3f v6 = new Vector3f();

    public static final Pos3 p1 = Pos3.init(0.0f, 0.0f, 0.0f);
    public static final Pos3 p2 = Pos3.init(0.0f, 0.0f, 0.0f);

    public static final Matrix4f m1 = new Matrix4f();
    public static final Matrix4f m2 = new Matrix4f();
    public static final Matrix4f m3 = new Matrix4f();

    public static final BlockPos.MutableBlockPos mp1 = new BlockPos.MutableBlockPos();
    public static final BlockPos.MutableBlockPos mp2 = new BlockPos.MutableBlockPos();
}
