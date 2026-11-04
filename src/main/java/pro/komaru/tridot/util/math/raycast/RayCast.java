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
        Vec3 start = context.getStartPos().mcVec();
        Vec3 end = context.getEndPos().mcVec();
        int entityCount = context.getEntityCount();
        float size = context.getEntitySize();
        boolean entityEnd = context.getEntityEnd();
        Predicate<Entity> entityFilter = context.getEntityFilter();
        Predicate<BlockPos> blockPosFilter = context.getBlockPosFilter();

        ClipContext clipContext = new ClipContext(start, end, context.getVanillaBlockShape(), context.getVanillaFluidShape(), null);
        BlockHitResult blockHit = level.clip(clipContext);
        double blockDistSqr = (blockHit.getType() != HitResult.Type.MISS && blockPosFilter.test(blockHit.getBlockPos())) ? start.distanceToSqr(blockHit.getLocation()) : Double.MAX_VALUE;

        List<Entity> hitEntities = new ArrayList<>();
        AABB rayBounds = new AABB(start, end).inflate(size + 1.0);
        List<Entity> candidates = level.getEntitiesOfClass(Entity.class, rayBounds, entityFilter);
        List<EntityHit> clippedHits = new ArrayList<>();
        for (Entity entity : candidates) {
            AABB targetBox = entity.getBoundingBox().inflate(size);
            Optional<Vec3> clip = targetBox.clip(start, end);
            if (clip.isEmpty()) continue;

            double distSqr = start.distanceToSqr(clip.get());
            if (distSqr <= blockDistSqr) {
                clippedHits.add(new EntityHit(entity, clip.get(), distSqr));
            }
        }

        clippedHits.sort(Comparator.comparingDouble(EntityHit::distSqr));
        for (EntityHit hit : clippedHits) {
            hitEntities.add(hit.entity());
            if (entityEnd && entityCount > 0 && hitEntities.size() >= entityCount) {
                return new RayHitResult(start).setHitPos(hit.hitPos()).setEntities(hitEntities);
            }
        }

        if (entityEnd && !clippedHits.isEmpty()) {
            return new RayHitResult(start).setHitPos(clippedHits.get(0).hitPos()).setEntities(hitEntities);
        }

        if (blockDistSqr < Double.MAX_VALUE) {
            boolean isBlock = !level.getBlockState(blockHit.getBlockPos()).isAir();
            return new RayHitResult(start)
                    .setHitPos(blockHit.getLocation())
                    .setBlockPos(blockHit.getBlockPos())
                    .setDirection(blockHit.getDirection())
                    .setBlock(isBlock)
                    .setEntities(hitEntities);
        }

        return new RayHitResult(start).setHitPos(end).setEntities(hitEntities);
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
