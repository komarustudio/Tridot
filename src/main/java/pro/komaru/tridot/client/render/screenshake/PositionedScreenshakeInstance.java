package pro.komaru.tridot.client.render.screenshake;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import pro.komaru.tridot.util.Tmp;
import pro.komaru.tridot.util.comps.phys.Pos3;
import pro.komaru.tridot.util.math.Interp;
import pro.komaru.tridot.util.phys.Vec3f;

public class PositionedScreenshakeInstance extends ScreenshakeInstance{
    public final Vec3f position;
    public final float falloffDistance;
    public final float maxDistance;
    public final Interp falloffEasing;

    public PositionedScreenshakeInstance(int duration, Pos3 position, float falloffDistance, float maxDistance, Interp falloffEasing){
        super(duration);
        this.position = new Vec3f(position);
        this.falloffDistance = falloffDistance;
        this.maxDistance = maxDistance;
        this.falloffEasing = falloffEasing;
    }

    public PositionedScreenshakeInstance(int duration, Pos3 position, float falloffDistance, float maxDistance){
        this(duration, position, falloffDistance, maxDistance, Interp.linear);
    }

    public PositionedScreenshakeInstance(int duration, Vec3 position, float falloffDistance, float maxDistance, Interp falloffEasing){
        this(duration, Vec3f.from(position), falloffDistance, maxDistance, falloffEasing);
    }

    public PositionedScreenshakeInstance(int duration, Vec3 position, float falloffDistance, float maxDistance){
        this(duration, Vec3f.from(position), falloffDistance, maxDistance, Interp.linear);
    }

    public PositionedScreenshakeInstance(int duration, Vec3f position, float falloffDistance, float maxDistance){
        this(duration, position, falloffDistance, maxDistance, Interp.linear);
    }

    @Override
    public float updateIntensity(Camera camera){
        float intensity = super.updateIntensity(camera);

        Tmp.v3.set(position).sub(Tmp.v4.set(camera.getPosition()));
        float distance = Tmp.v3.len();
        if(distance > maxDistance){
            return 0;
        }

        float distanceMultiplier = 1;
        if(distance > falloffDistance){
            float remaining = maxDistance - falloffDistance;
            float current = distance - falloffDistance;
            distanceMultiplier = 1 - current / remaining;
        }

        Vector3f lookDirection = camera.getLookVector();
        Tmp.v3.nor();
        float angle = Math.max(0, lookDirection.dot(Tmp.v3.x(), Tmp.v3.y(), Tmp.v3.z()));
        return (intensity + (intensity * angle)) * 0.5f * distanceMultiplier;
    }
}
