package pro.komaru.tridot.client.gfx.trail;

import org.joml.Vector4f;
import pro.komaru.tridot.util.comps.phys.Pos3;
import pro.komaru.tridot.util.phys.Vec3f;

public class TrailPoint{
    private final Vec3f position;
    private final Vector4f matrixPosition;
    private int timeActive;

    public TrailPoint(Pos3 position, int timeActive){
        this.position = new Vec3f(position);
        this.matrixPosition = new Vector4f(this.position.x, this.position.y, this.position.z, 1.0f);
        this.timeActive = timeActive;
    }

    public TrailPoint(Pos3 position){
        this(position, 0);
    }

    public Vector4f getMatrixPosition(){
        return matrixPosition;
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
