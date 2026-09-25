package pro.komaru.tridot.client.gfx.particle;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.util.*;
import net.neoforged.api.distmarker.*;
import pro.komaru.tridot.client.gfx.particle.data.*;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.*;
import net.minecraft.world.phys.*;
import pro.komaru.tridot.client.render.TridotRenderTypes;
import pro.komaru.tridot.client.gfx.particle.behavior.ParticleBehavior;
import pro.komaru.tridot.client.gfx.particle.behavior.component.ParticleBehaviorComponent;
import pro.komaru.tridot.client.gfx.particle.options.GenericParticleOptions;
import pro.komaru.tridot.util.*;

import java.awt.*;
import java.util.*;
import java.util.function.*;

public class GenericParticle extends TextureSheetParticle{

    public static final Random random = new Random();

    public RenderType renderType;
    public ParticleRenderType particleRenderType;

    public ParticleBehavior behavior;
    public ParticleBehaviorComponent behaviorComponent;

    public ColorParticleData colorData;
    public GenericParticleData transparencyData;
    public GenericParticleData scaleData;
    public SpinParticleData spinData;
    public LightParticleData lightData;
    public SpriteParticleData spriteData;

    public Collection<Consumer<GenericParticle>> tickActors;
    public Collection<Consumer<GenericParticle>> renderActors;

    public GenericParticleOptions.DiscardFunctionType discardFunctionType;
    public boolean reachedPositiveAlpha;
    public boolean reachedPositiveScale;

    public boolean shouldCull;
    public boolean shouldRenderTraits;

    public ParticleEngine.MutableSpriteSet spriteSet;

    public float st;
    public float mt;
    public float et;
    public float ss;
    public float ms;
    public float es;
    public float sr;
    public float mr;
    public float er;

    public float randomSpin;

    public float uo;
    public float vo;

    float[] hsv1 = new float[3], hsv2 = new float[3];

    public GenericParticle(ClientLevel level, GenericParticleOptions options, ParticleEngine.MutableSpriteSet spriteSet, double x, double y, double z, double vx, double vy, double vz){
        super(level, x, y, z, vx, vy, vz);
        this.setPos(x, y, z);
        this.renderType = options.renderType;
        this.particleRenderType = options.particleRenderType;
        this.colorData = options.colorData;
        this.transparencyData = GenericParticleData.constrictTransparency(options.transparencyData);
        this.scaleData = options.scaleData;
        this.spinData = options.spinData;
        this.lightData = options.lightData;
        this.spriteData = options.spriteData;
        this.tickActors = options.tickActors;
        this.renderActors = options.renderActors;
        this.discardFunctionType = options.discardFunctionType;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.setLifetime(options.lifetime + random.nextInt(options.additionalLifetime + 1));
        this.gravity = options.gravity;
        if(options.additionalGravity > 0) this.gravity = this.gravity + random.nextFloat(options.additionalGravity);
        this.friction = options.friction;
        if(options.additionalFriction > 0) this.friction = this.friction + random.nextFloat(options.additionalFriction);
        this.shouldCull = options.shouldCull;
        this.shouldRenderTraits = options.shouldRenderTraits;
        this.hasPhysics = options.hasPhysics;
        this.spriteSet = spriteSet;
        this.roll = spinData.spinOffset + spinData.startingValue;
        this.randomSpin = (pickRandomValue(0, spinData.rsp1, spinData.rsp2));
        if(random.nextBoolean()) this.randomSpin = -this.randomSpin;
        this.roll = this.roll + pickRandomRollValue(0, spinData.rso1, spinData.rso2);

        float r1 = pickRandomValue(colorData.r1, colorData.rr11, colorData.rr12);
        float g1 = pickRandomValue(colorData.g1, colorData.rg11, colorData.rg12);
        float b1 = pickRandomValue(colorData.b1, colorData.rb11, colorData.rb12);
        float r2 = pickRandomValue(colorData.r2, colorData.rr21, colorData.rr22);
        float g2 = pickRandomValue(colorData.g2, colorData.rg21, colorData.rg22);
        float b2 = pickRandomValue(colorData.b2, colorData.rb21, colorData.rb21);

        st = pickRandomValue(transparencyData.startingValue, transparencyData.rs1, transparencyData.rs2);
        mt = pickRandomValue(transparencyData.middleValue, transparencyData.rm1, transparencyData.rm2);
        et = pickRandomValue(transparencyData.endingValue, transparencyData.re1, transparencyData.re2);

        ss = pickRandomValue(scaleData.startingValue, scaleData.rs1, scaleData.rs2);
        ms = pickRandomValue(scaleData.middleValue, scaleData.rm1, scaleData.rm2);
        es = pickRandomValue(scaleData.endingValue, scaleData.re1, scaleData.re2);

        sr = pickRandomValue(spinData.startingValue, spinData.rs1, spinData.rs2);
        mr = pickRandomValue(spinData.middleValue, spinData.rm1, spinData.rm2);
        er = pickRandomValue(spinData.endingValue, spinData.re1, spinData.re2);

        behavior = options.behavior;
        if(behavior != null) behavior.init(this);

        this.uo = random.nextFloat();
        this.vo = random.nextFloat();
        spriteData.init(this);

        Tmp.c1.set(r1,g1,b1);
        hsv1 = Tmp.c1.toHsv(hsv1);
        Tmp.c2.set(r2,g2,b2);
        hsv2 = Tmp.c2.toHsv(hsv2);

        options.spawnActors.forEach(actor -> actor.accept(this));
        updateTraits();
    }

    public static float pickRandomValue(float value, float value1, float value2){
        if(value1 >= 0 && value2 >= 0){
            return (value1 != value2) ? random.nextFloat(Math.min(value1, value2), Math.max(value1, value2)) : value1;
        }
        return value;
    }

    public static float pickRandomRollValue(float value, float value1, float value2){
        if(value1 != 0 || value2 != 0){
            return (value1 != value2) ? random.nextFloat(Math.min(value1, value2), Math.max(value1, value2)) : value1;
        }
        return value;
    }

    public void pickColor(float coeff){
        var col = Col.HSVtoRGB(hsv1[0], hsv1[1] * 100f, hsv1[2] * 100f);
        var col2 = Col.HSVtoRGB(hsv2[0], hsv2[1] * 100f, hsv2[2] * 100f);
        col.lerp(col2,coeff);
        setColor(col.r,col.g,col.b);
    }

    public void updateTraits(){
        pickColor(colorData.colorCurveEasing.apply(colorData.getProgress(age, lifetime)));
        quadSize = scaleData.getValue(age, lifetime, ss, ms, es);
        alpha = transparencyData.getValue(age, lifetime, st, mt, et);
        oRoll = roll;
        roll = roll + spinData.getValue(age, lifetime, sr, mr, er) + randomSpin;

        if(behavior != null) behavior.updateTraits(this);

        boolean shouldAttemptRemoval = discardFunctionType == GenericParticleOptions.DiscardFunctionType.INVISIBLE;
        if(discardFunctionType == GenericParticleOptions.DiscardFunctionType.ENDING_CURVE_INVISIBLE){
            if(scaleData.getProgress(age, lifetime) > 0.5f || transparencyData.getProgress(age, lifetime) > 0.5f){
                shouldAttemptRemoval = true;
            }
        }
        if(shouldAttemptRemoval){
            if((reachedPositiveAlpha && alpha <= 0) || (reachedPositiveScale && quadSize <= 0)){
                remove();
                return;
            }
        }

        if(!reachedPositiveAlpha && alpha > 0){
            reachedPositiveAlpha = true;
        }
        if(!reachedPositiveScale && quadSize > 0){
            reachedPositiveScale = true;
        }
    }

    @Override
    public void tick(){
        updateTraits();
        spriteData.tick(this);
        super.tick();
        tickActors.forEach(a -> a.accept(this));
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float partialTicks){
        if(shouldRenderTraits) updateRenderTraits(partialTicks);
        spriteData.renderTick(this, partialTicks);
        renderActors.forEach(actor -> actor.accept(this));
        if(behavior == null){
            super.render(renderType != null ? TridotRenderTypes.getDelayedRender().getBuffer(renderType) : vertexConsumer, camera, partialTicks);
        }else{
            behavior.render(this, renderType != null ? TridotRenderTypes.getDelayedRender().getBuffer(renderType) : vertexConsumer, camera, partialTicks);
        }
    }

    public void updateRenderTraits(float partialTicks){
        float time = age + partialTicks;
        pickColor(colorData.colorCurveEasing.apply(colorData.getProgress(time, lifetime)));
        quadSize = scaleData.getValue(time, lifetime, ss, ms, es);
        alpha = transparencyData.getValue(time, lifetime, st, mt, et);
    }

    @Override
    public ParticleRenderType getRenderType(){
        return particleRenderType;
    }

    @Override
    public int getLightColor(float partialTicks){
        return lightData.getLight(this, this.level, partialTicks);
    }

    @Override
    public float getU0(){
        return spriteData.getU0(this);
    }

    @Override
    public float getU1(){
        return spriteData.getU1(this);
    }

    @Override
    public float getV0(){
        return spriteData.getV0(this);
    }

    @Override
    public float getV1(){
        return spriteData.getV1(this);
    }

    public float getRoll(){
        return roll;
    }

    public float getORoll(){
        return oRoll;
    }

    public float getRed(){
        return rCol;
    }

    public float getGreen(){
        return gCol;
    }

    public float getBlue(){
        return bCol;
    }

    public float getAlpha(){
        return alpha;
    }

    public float getSize(){
        return quadSize;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getZ(){
        return z;
    }

    public double getXOld(){
        return xo;
    }

    public double getYOld(){
        return yo;
    }

    public double getZOld(){
        return zo;
    }

    public double getXMotion(){
        return xd;
    }

    public double getYMotion(){
        return yd;
    }

    public double getZMotion(){
        return zd;
    }

    public Vec3 getPosition(){
        return new Vec3(getX(), getY(), getZ());
    }

    public void setPosition(Vec3 pos){
        setPos(pos.x, pos.y, pos.z);
    }

    public Vec3 getSpeed(){
        return new Vec3(getXMotion(), getYMotion(), getZMotion());
    }

    public void setSpeed(Vec3 speed){
        setParticleSpeed(speed.x, speed.y, speed.z);
    }

    public int getLifetime(){
        return lifetime;
    }

    public int getAge(){
        return age;
    }

    public void pickSprite(int spriteIndex){
        if(spriteIndex < spriteSet.sprites.size() && spriteIndex >= 0){
            setSprite(spriteSet.sprites.get(spriteIndex));
        }
    }
}