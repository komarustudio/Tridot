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

    public static RayHitResult getHit(Level level, RayCastContext context, RayHitResult out){
        Vec3f start = context.getStartPos();
        Vec3f end = context.getEndPos();
        int entityCount = context.getEntityCount();
        float size = context.getEntitySize();
        boolean entityEnd = context.getEntityEnd();
        Predicate<BlockPos> blockPosFilter = context.getBlockPosFilter();

        Vec3 s = start.mcVec();
        Vec3 e = end.mcVec();

        out.reset();
        out.setPos(start);
        List<Entity> hitEntities = out.entities;

        BlockHitResult blockHit = level.clip(new ClipContext(s, e, context.getVanillaBlockShape(), context.getVanillaFluidShape(), context.getCollisionContext()));
        double blockDistSqr = blockHit.getType() != HitResult.Type.MISS && blockPosFilter.test(blockHit.getBlockPos())
                ? s.distanceToSqr(blockHit.getLocation())
                : Double.MAX_VALUE;

        try{
            AABB rayBounds = new AABB(s, e).inflate(size + 1.0);
            for(Entity entity : level.getEntitiesOfClass(Entity.class, rayBounds, context.getEntityFilter())){
                Optional<Vec3> clip = entity.getBoundingBox().inflate(size).clip(s, e);
                if(clip.isEmpty()) continue;

                double distSqr = s.distanceToSqr(clip.get());
                if(distSqr <= blockDistSqr) HITS.add(new EntityHit(entity, clip.get(), distSqr));
            }
            HITS.sort(BY_DIST);

            for (EntityHit hit : HITS) {
                hitEntities.add(hit.entity());
                if (entityEnd && entityCount > 0 && hitEntities.size() >= entityCount) {
                    out.getHitPos().set(hit.hitPos);
                    return out;
                }
            }

            if(entityEnd && !HITS.isEmpty()){
                Vec3 p = HITS.get(0).hitPos();
                out.getHitPos().set(p);
                return out;
            }
        }finally{
            HITS.clear();
        }

        if(blockDistSqr < Double.MAX_VALUE){
            Vec3 p = blockHit.getLocation();
            out.getHitPos().set(p);
            return out
                    .setBlockPos(blockHit.getBlockPos())
                    .setDirection(blockHit.getDirection())
                    .setBlock(!level.getBlockState(blockHit.getBlockPos()).isAir());
        }

        out.getHitPos().set(end);
        return out;
    }

    public static RayHitResult getHit(Level level, Vec3 start, Vec3 end){
        return getHit(level, new RayCastContext(start, end));
    }

    public static List<Entity> getHitEntities(Level level, Vec3 start, Vec3 endPos, float distance){
        AABB box = new AABB(start, endPos).inflate(distance);
        List<Entity> candidates = level.getEntitiesOfClass(Entity.class, box);
        List<Entity> list = new ArrayList<>();
        for (Entity entity : candidates) {
            AABB tb = entity.getBoundingBox().inflate(distance);
            if (tb.clip(start, endPos).isPresent()) {
                list.add(entity);
            }
        }

        return list;
    }
}
