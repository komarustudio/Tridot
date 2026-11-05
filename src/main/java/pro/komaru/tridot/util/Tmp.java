package pro.komaru.tridot.util;

import net.minecraft.core.BlockPos;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import pro.komaru.tridot.util.comps.phys.Pos3;
import pro.komaru.tridot.util.math.ArcRandom;
import pro.komaru.tridot.util.math.raycast.RayCastContext;
import pro.komaru.tridot.util.math.raycast.RayHitResult;
import pro.komaru.tridot.util.phys.Vec2f;
import pro.komaru.tridot.util.phys.Vec3f;
import pro.komaru.tridot.util.struct.TempPool;

public class Tmp {
    private Tmp() {}
    public static ArcRandom rnd = new ArcRandom();

    public static final TempPool<Col> col = new TempPool<>(8, Col::new, Col::reset);
    public static final TempPool<Vec2f> vec2f = new TempPool<>(16, Vec2f::new, Vec2f::reset);
    public static final TempPool<Vec3f> vec3f = new TempPool<>(64, Vec3f::new, Vec3f::reset);
    public static final TempPool<Vector3f> vector3f = new TempPool<>(16, Vector3f::new, Vector3f::zero);
    public static final TempPool<Matrix4f> matrix4f = new TempPool<>(4, Matrix4f::new, Matrix4f::zero);
    public static final TempPool<BlockPos.MutableBlockPos> blockPos = new TempPool<>(
            2,
            BlockPos.MutableBlockPos::new,
            bp -> bp.set(0, 0, 0)
    );
    public static final TempPool<RayCastContext> rayCastContext = new TempPool<>(64, RayCastContext::new, RayCastContext::reset);
    public static final TempPool<RayHitResult> rayHitResult = new TempPool<>(64, RayHitResult::new, RayHitResult::reset);
    
    @Deprecated
    public static final Col
            c1 = new Col(),
            c2 = new Col(),
            c3 = new Col();

    @Deprecated
    public static final Vec2f
            v1 = new Vec2f(),
            v2 = new Vec2f();

    @Deprecated
    public static final Vec3f
            v3 = new Vec3f(),
            v4 = new Vec3f();

    @Deprecated
    public static final Vector3f
            v5 = new Vector3f(),
            v6 = new Vector3f();

    @Deprecated
    public static final Pos3
            p1 = Pos3.init(0.0f, 0.0f, 0.0f),
            p2 = Pos3.init(0.0f, 0.0f, 0.0f);

    @Deprecated
    public static final Matrix4f
            m1 = new Matrix4f(),
            m2 = new Matrix4f(),
            m3 = new Matrix4f();

    @Deprecated
    public static final BlockPos.MutableBlockPos
            mp1 = new BlockPos.MutableBlockPos(),
            mp2 = new BlockPos.MutableBlockPos();
}
