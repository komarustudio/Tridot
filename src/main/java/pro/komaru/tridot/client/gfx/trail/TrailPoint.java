package pro.komaru.tridot.client.gfx.trail;

import org.joml.*;
import pro.komaru.tridot.util.comps.phys.*;
import pro.komaru.tridot.util.phys.*;

public class TrailPoint{
    private final Vec3f position;
    private int timeActive;

    public TrailPoint(Pos3 position, int timeActive){
        this.position = new Vec3f(position);
        this.timeActive = timeActive;
    }

    public TrailPoint(Pos3 position){
        this(position, 0);
    }

    public Vector4f getMatrixPosition(){
        Vec3f position = getPosition();
        return new Vector4f(position.x, position.y, position.z, 1.0f);
    }

    public Vec3f getPosition(){
        return position;
    }

    public int getTimeActive(){
        return timeActive;
    }

    public TrailPoint lerp(TrailPoint trailPoint, float delta){
        Vec3f position = getPosition();
        return new TrailPoint(position.lerp(trailPoint.position, delta), timeActive);
    }

    public void tick(){
        timeActive++;
    }
}