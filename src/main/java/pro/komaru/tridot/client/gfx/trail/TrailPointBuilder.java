package pro.komaru.tridot.client.gfx.trail;

import org.joml.*;
import pro.komaru.tridot.util.comps.phys.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class TrailPointBuilder{

    private final List<TrailPoint> trailPoints = new ArrayList<>();
    public final Supplier<Integer> trailLength;

    public TrailPointBuilder(Supplier<Integer> trailLength){
        this.trailLength = trailLength;
    }

    public static TrailPointBuilder create(int trailLength){
        return create(() -> trailLength);
    }

    public static TrailPointBuilder create(Supplier<Integer> trailLength){
        return new TrailPointBuilder(trailLength);
    }

    public List<TrailPoint> points(){
        return trailPoints;
    }

    public List<TrailPoint> points(float lerp){
        List<TrailPoint> lerpedTrailPoints = new ArrayList<>();
        final int size = trailPoints.size();
        if(size > 1){
            for(int i = 0; i < size - 2; i++){
                lerpedTrailPoints.add(trailPoints.get(i).lerp(trailPoints.get(i + 1), lerp));
            }
        }
        return lerpedTrailPoints;
    }

    public TrailPointBuilder add(Pos3 point){
        return add(new TrailPoint(point, 0));
    }

    public TrailPointBuilder add(TrailPoint point){
        trailPoints.add(point);
        return this;
    }

    public TrailPointBuilder tick(){
        int trailLength = this.trailLength.get();
        trailPoints.forEach(TrailPoint::tick);
        trailPoints.removeIf(p -> p.getTimeActive() > trailLength);
        return this;
    }

    public List<Vector4f> build(){
        return trailPoints.stream().map(TrailPoint::getMatrixPosition).collect(Collectors.toList());
    }
}