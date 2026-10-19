package pro.komaru.tridot.client.render.screenshake;

import pro.komaru.tridot.util.comps.phys.Pos3;
import pro.komaru.tridot.util.math.Interp;
import net.minecraft.client.*;
import org.joml.*;
import pro.komaru.tridot.util.phys.Vec3f;

import java.lang.Math;

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

    public PositionedScreenshakeInstance(int duration, net.minecraft.world.phys.Vec3 position, float falloffDistance, float maxDistance, Interp falloffEasing){
        this(duration, Vec3f.from(position), falloffDistance, maxDistance, falloffEasing);
    }

    public PositionedScreenshakeInstance(int duration, net.minecraft.world.phys.Vec3 position, float falloffDistance, float maxDistance){
        this(duration, Vec3f.from(position), falloffDistance, maxDistance, Interp.linear);
    }

    @Override
    public float updateIntensity(Camera camera){
        float intensity = super.updateIntensity(camera);
        float distance = position.cpy().sub(Vec3f.from(camera.getPosition())).len();
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
        Pos3 directionToScreenshake = position.cpy().sub(Vec3f.from(camera.getPosition())).nor();
        float angle = Math.max(0, lookDirection.dot(new Vector3f(directionToScreenshake.x(), directionToScreenshake.y(), directionToScreenshake.z())));
        return (intensity + (intensity * angle)) * 0.5f * distanceMultiplier;
    }
}