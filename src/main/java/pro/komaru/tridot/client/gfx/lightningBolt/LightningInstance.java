package pro.komaru.tridot.client.gfx.lightningBolt;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.math.*;
import pro.komaru.tridot.util.phys.*;

import java.util.*;

/**
 * Example code usage:
 *
 * <pre>{@code
 * // 1. Create a LightningInstance using the Builder
 * LightningInstance bolt = new LightningInstance.LightningBuilder()
 * .setColor(new Col(0.2f, 0.5f, 0.7f, 1))
 * .setLifetime(20)
 * .setScaleModifier(0.125F)
 * .build(startVec3, endVec3);
 *
 * // 2. In your render loop
 * bolt.tick();
 * if (bolt.age < bolt.getLifetime()) {
 *      bolt.renderTick(poseStack, buffer);
 * } else {
 * // Remove bolt from your list/map
 * }
 * }</pre>
 *
 */
@OnlyIn(Dist.CLIENT)
public class LightningInstance {
    public int age;
    public final Vec3 start;
    public final Vec3 end;
    public final LightningBuilder builder;

    // cached values for optimization purposes
    public final float[] segXOffset;
    public final float[] segZOffset;
    public final float xOffSum;
    public final float zOffSum;
    public final float segHeight;
    public final float relScale;

    public final float rotY;
    public final float rotX;

    private LightningInstance(LightningBuilder builder, Vec3 start, Vec3 end) {
        this.builder = builder;
        this.start = start;
        this.end = end;
        this.age = 0;

        float length = Mathf.distance(start, end);
        Vec3 virtualEndPos = start.cpy().add(0, length, 0);

        Vec3 dirVec = end.cpy().sub(start).nor();
        double dirVecXZDist = Math.sqrt(dirVec.x * dirVec.x + dirVec.z * dirVec.z);

        this.rotY = (float)(Mth.atan2(dirVec.x, dirVec.z) * (180F / Math.PI));
        this.rotX = (float)(Mth.atan2(dirVec.y, dirVecXZDist) * (180F / Math.PI));

        int segCount = builder.segCount;
        this.segXOffset = new float[segCount + 1];
        this.segZOffset = new float[segCount + 1];

        double height = virtualEndPos.y - start.y;
        relScale = builder.autoScale ? (float)height / 128F : 1F;
        segHeight = (float)height / segCount;

        float xOffSum = 0;
        float zOffSum = 0;

        var rnd = new SplittableRandom(Tmp.rnd.nextLong());
        for(int segment = 0; segment < segCount + 1; segment++){
            segXOffset[segment] = xOffSum + start.x;
            segZOffset[segment] = zOffSum + start.z;
            if(segment < segCount){
                float maxDeflect = segHeight * 0.85f;
                xOffSum += Mth.clamp((5 - rnd.nextFloat() * 10) * builder.deflectModifier, -maxDeflect, maxDeflect);
                zOffSum += Mth.clamp((5 - rnd.nextFloat() * 10) * builder.deflectModifier, -maxDeflect, maxDeflect);
            }
        }

        this.xOffSum = xOffSum - (virtualEndPos.x - start.x);
        this.zOffSum = zOffSum - (virtualEndPos.z - start.z);

    }

    /**
     * Should be called inside your renderer class ex TileEntity renderer
     */
    public void renderTick(PoseStack poseStack, MultiBufferSource buffer) {
        LightningEffect.renderLightningP2PRotate(poseStack, buffer, start, end, this);
    }

    public void tick() {
        age++;
        builder.color.a = age / (float) builder.lifetime;
    }

    public Col getColor() {
        return builder.color;
    }

    public int getLifetime() {
        return builder.lifetime;
    }

    public static class LightningBuilder {
        public Col color;
        public int lifetime;

        public int segCount = 8;
        public float scaleModifier = 1;
        public float deflectModifier = 1;
        public float segTaper = 0;

        public boolean autoScale = false;

        /**
         * @param color The colour of the arc.
         */
        public LightningBuilder setColor(Col color){
            this.color = color;
            return this;
        }

        /**
         * @param lifetime The lifetime of the arc.
         */
        public LightningBuilder setLifetime(int lifetime){
            this.lifetime = lifetime;
            return this;
        }

        /**
         * @param count The number of arc segments. (Default 8)
         */
        public LightningBuilder setSegCount(int count){
            this.segCount = count;
            return this;
        }

        /**
         * @param mod Modifier that applies to the diameter of the arc. (Default 1)
         */
        public LightningBuilder setScaleModifier(float mod){
            this.scaleModifier = mod;
            return this;
        }

        /**
         * @param mod Modifies the segment offsets (Makes it more zigzaggy) (Default 1)
         */
        public LightningBuilder setDeflectModifier(float mod){
            this.deflectModifier = mod;
            return this;
        }

        /**
         * @param tap Allows you to apply a positive or negative taper to each arc segment. (Default 0)
         */
        public LightningBuilder setSegTaper(float tap){
            this.segTaper = tap;
            return this;
        }

        /**
         * @param value If true automatically adjusts the overall scale based on the length of the arc.
         */
        public LightningBuilder setAutoScale(boolean value){
            this.autoScale = value;
            return this;
        }

        public LightningInstance build(Vec3 start, Vec3 end) {
            return new LightningInstance(this, start, end);
        }
    }
}