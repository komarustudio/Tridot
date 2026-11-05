package pro.komaru.tridot.util.math.raycast;

import net.minecraft.core.*;
import net.minecraft.tags.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import pro.komaru.tridot.util.phys.Vec3f;

import java.util.function.*;

public class RayCastContext{
    public static final Predicate<Entity> NO_ENTITIES = e -> false;

    public Vec3f startPos = Vec3f.zero();
    public Vec3f endPos = Vec3f.zero();
    public Predicate<BlockPos> blockPosFilter = (b) -> true;
    public Predicate<FluidState> fluidFilter = Fluid.NONE;
    public Predicate<Entity> entityFilter = NO_ENTITIES;
    public int entityCount = 0;
    public float entitySize = 0;
    public boolean entityEnd = false;
    public Block blockShape = Block.COLLIDER;
    public CollisionContext collisionContext = CollisionContext.empty();

    public RayCastContext(){
        reset();
    }

    public RayCastContext(Vec3f startPos, Vec3f endPos){
        reset();
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public RayCastContext(Vec3 startPos, Vec3 endPos){
        reset();
        this.startPos.set(startPos);
        this.endPos.set(endPos);
    }

    public RayCastContext setStartPos(Vec3 startPos){
        this.startPos.set(startPos);
        return this;
    }

    public RayCastContext setEndPos(Vec3 endPos){
        this.endPos.set(endPos);
        return this;
    }

    public RayCastContext setBlockPosFilter(Predicate<BlockPos> blockPosFilter){
        this.blockPosFilter = blockPosFilter;
        return this;
    }

    public RayCastContext setFluidFilter(Predicate<FluidState> fluidFilter){
        this.fluidFilter = fluidFilter;
        return this;
    }

    public RayCastContext setEntityFilter(Predicate<Entity> entityFilter){
        this.entityFilter = entityFilter;
        return this;
    }

    public RayCastContext setEntityCount(int entityCount){
        this.entityCount = entityCount;
        return this;
    }

    public RayCastContext setEntitySize(float entitySize){
        this.entitySize = entitySize;
        return this;
    }

    public RayCastContext setEntityEnd(boolean entityEnd){
        this.entityEnd = entityEnd;
        return this;
    }

    public RayCastContext setBlockShape(Block blockShape){
        this.blockShape = blockShape;
        return this;
    }

    public RayCastContext setCollisionContext(CollisionContext collisionContext){
        this.collisionContext = collisionContext;
        return this;
    }

    public Vec3f getStartPos(){
        return startPos;
    }

    public Vec3f getEndPos(){
        return endPos;
    }

    public Predicate<BlockPos> getBlockPosFilter(){
        return blockPosFilter;
    }

    public Predicate<FluidState> getFluidFilter(){
        return fluidFilter;
    }

    public Predicate<Entity> getEntityFilter(){
        return entityFilter;
    }

    public int getEntityCount(){
        return entityCount;
    }

    public float getEntitySize(){
        return entitySize;
    }

    public boolean getEntityEnd(){
        return entityEnd;
    }

    public Block getBlockShape(){
        return blockShape;
    }

    public net.minecraft.world.level.ClipContext.Block getVanillaBlockShape(){
        if(this.blockShape == Block.OUTLINE) return net.minecraft.world.level.ClipContext.Block.OUTLINE;
        if(this.blockShape == Block.VISUAL) return net.minecraft.world.level.ClipContext.Block.VISUAL;
        return net.minecraft.world.level.ClipContext.Block.COLLIDER;
    }

    public net.minecraft.world.level.ClipContext.Fluid getVanillaFluidShape(){
        if(this.fluidFilter == Fluid.SOURCE_ONLY) return net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY;
        if(this.fluidFilter == Fluid.ANY) return net.minecraft.world.level.ClipContext.Fluid.ANY;
        if(this.fluidFilter == Fluid.WATER) return net.minecraft.world.level.ClipContext.Fluid.WATER;
        return net.minecraft.world.level.ClipContext.Fluid.NONE;
    }

    public CollisionContext getCollisionContext(){
        return collisionContext;
    }

    public VoxelShape getBlockShape(BlockState blockState, BlockGetter level, BlockPos pos){
        return blockShape.get(blockState, level, pos, collisionContext);
    }

    public VoxelShape getFluidShape(FluidState state, BlockGetter level, BlockPos pos){
        return fluidFilter.test(state) ? state.getShape(level, pos) : Shapes.empty();
    }

    public void reset() {
        startPos.reset();
        endPos.reset();
        blockPosFilter = b -> true;
        fluidFilter = Fluid.NONE;
        entityFilter = NO_ENTITIES;
        entityCount = 0;
        entitySize = 0;
        entityEnd = false;
        blockShape = Block.COLLIDER;
        collisionContext = CollisionContext.empty();
    }

    public record Block(ShapeGetter shapeGetter) implements ShapeGetter {
            public static Block COLLIDER = new Block(BlockBehaviour.BlockStateBase::getCollisionShape);
            public static Block OUTLINE = new Block(BlockBehaviour.BlockStateBase::getShape);
            public static Block VISUAL = new Block(BlockBehaviour.BlockStateBase::getVisualShape);

        public VoxelShape get(BlockState state, BlockGetter block, BlockPos pos, CollisionContext collisionContext) {
                return this.shapeGetter.get(state, block, pos, collisionContext);
            }
        }

    public static class Fluid{
        public static Predicate<FluidState> NONE = (f) -> false;
        public static Predicate<FluidState> SOURCE_ONLY = (FluidState::isSource);
        public static Predicate<FluidState> ANY = (f) -> !f.isEmpty();
        public static Predicate<FluidState> WATER = (f) -> f.is(FluidTags.WATER);
    }

    public interface ShapeGetter{
        VoxelShape get(BlockState state, BlockGetter block, BlockPos pos, CollisionContext collisionContext);
    }
}
