package pro.komaru.tridot.util.math.raycast;

import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import pro.komaru.tridot.util.phys.Vec3f;

import java.util.*;
import java.util.function.*;

public class RayCast{

    private record EntityHit(Entity entity, Vec3 hitPos, double distSqr) {}

    public static RayHitResult getHit(Level level, RayCastContext context){
        return getHit(level, context, new RayHitResult());
    }

    private static final ArrayList<EntityHit> HITS = new ArrayList<>();
    private static final Comparator<EntityHit> BY_DIST = Comparator.comparingDouble(EntityHit::distSqr);

    public static RayHitResult getHit(Level level, RayCastContext ctx, RayHitResult out){
        Vec3f start = ctx.getStartPos();
        Vec3f end = ctx.getEndPos();
        Vec3 s = start.mcVec();
        Vec3 e = end.mcVec();

        out.reset();
        out.setPos(start);

        BlockHitResult blockHit = level.clip(new ClipContext(s, e, ctx.getVanillaBlockShape(), ctx.getVanillaFluidShape(), null));
        double blockDistSqr = blockHit.getType() != HitResult.Type.MISS && ctx.getBlockPosFilter().test(blockHit.getBlockPos())
                ? s.distanceToSqr(blockHit.getLocation())
                : Double.MAX_VALUE;

        if(ctx.getEntityFilter() != RayCastContext.NO_ENTITIES){
            Vec3 entityPos = collectEntities(level, ctx, s, e, blockDistSqr, out);
            if(entityPos != null){
                out.getHitPos().set(entityPos);
                return out;
            }
        }

        if(blockDistSqr < Double.MAX_VALUE){
            out.getHitPos().set(blockHit.getLocation());
            return out.setBlockPos(blockHit.getBlockPos())
                    .setDirection(blockHit.getDirection())
                    .setBlock(!level.getBlockState(blockHit.getBlockPos()).isAir());
        }

        out.getHitPos().set(end);
        return out;
    }

    private static Vec3 collectEntities(Level level, RayCastContext ctx, Vec3 s, Vec3 e, double blockDistSqr, RayHitResult out){
        float size = ctx.getEntitySize();
        int count = ctx.getEntityCount();
        boolean entityEnd = ctx.getEntityEnd();

        try{
            AABB bounds = new AABB(s, e).inflate(size + 1.0);
            for(Entity entity : level.getEntitiesOfClass(Entity.class, bounds, ctx.getEntityFilter())){
                Optional<Vec3> clip = entity.getBoundingBox().inflate(size).clip(s, e);
                if(clip.isEmpty()) continue;

                double distSqr = s.distanceToSqr(clip.get());
                if(distSqr <= blockDistSqr) HITS.add(new EntityHit(entity, clip.get(), distSqr));
            }
            HITS.sort(BY_DIST);

            for(EntityHit hit : HITS){
                out.entities.add(hit.entity());
                if(entityEnd && count > 0 && out.entities.size() >= count) return hit.hitPos();
            }
            return entityEnd && !HITS.isEmpty() ? HITS.get(0).hitPos() : null;
        }finally{
            HITS.clear();
        }
    }

    public static List<Entity> getHitEntities(Level level, Vec3 start, Vec3 end, float distance, List<Entity> out){
        for(Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(start, end).inflate(distance))){
            if(entity.getBoundingBox().inflate(distance).clip(start, end).isPresent()) out.add(entity);
        }
        return out;
    }

    public static List<Entity> getHitEntities(Level level, Vec3 start, Vec3 end, float distance){
        return getHitEntities(level, start, end, distance, new ArrayList<>());
    }
}
