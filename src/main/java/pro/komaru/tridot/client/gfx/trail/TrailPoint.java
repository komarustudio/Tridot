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

    public TrailPoint(Vec3f position, int timeActive){
        this.position = position;
        this.matrixPosition = new Vector4f(this.position.x, this.position.y, this.position.z, 1.0f);
        this.timeActive = timeActive;
    }

    public TrailPoint(Vec3f position){
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

    public void setTimeActive(int timeActive){
        this.timeActive = timeActive;
    }

    public TrailPoint lerp(TrailPoint trailPoint, float delta){
        Vec3f pos = new Vec3f(this.position).lerp(trailPoint.position, delta);
        return new TrailPoint(pos, timeActive);
    }

    public void tick(){
        timeActive++;
    }
}
