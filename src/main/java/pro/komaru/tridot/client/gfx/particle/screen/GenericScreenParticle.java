package pro.komaru.tridot.client.gfx.particle.screen;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.particle.*;
import net.minecraft.util.*;
import org.joml.*;
import pro.komaru.tridot.client.gfx.particle.*;
import pro.komaru.tridot.client.gfx.particle.data.*;
import pro.komaru.tridot.client.gfx.particle.options.*;
import pro.komaru.tridot.client.render.TridotRenderTypes.*;
import pro.komaru.tridot.client.render.gui.particle.*;
import pro.komaru.tridot.util.*;

import java.lang.Math;
import java.util.Random;
import java.util.function.*;

import static pro.komaru.tridot.client.ClientTick.partialTicks;

public class GenericScreenParticle extends TextureSheetScreenParticle{
    public static final java.util.Random random = new Random();
    private final ScreenParticleRenderType renderType;
    public final ParticleEngine.MutableSpriteSet spriteSet;
    protected final ColorParticleData colorData;
    protected final GenericParticleData transparencyData;
    protected final GenericParticleData scaleData;
    public ScreenSpriteParticleData spriteData;

    protected final SpinParticleData spinData;
    protected final Consumer<GenericScreenParticle> actor;
    private final boolean tracksStack;
    private final double stackTrackXOffset;
    private final double stackTrackYOffset;

    private int lifeDelay;

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

    public GenericScreenParticle(ClientLevel world, ScreenParticleOptions options, ParticleEngine.MutableSpriteSet spriteSet, double x, double y, double xMotion, double yMotion) {
        super(world, x, y);
        this.renderType = options.renderType;
        this.spriteSet = spriteSet;
        this.colorData = options.colorData;
        this.transparencyData = options.transparencyData;
        this.scaleData = options.scaleData;
        this.spinData = options.spinData;
        this.actor = options.actor;
        this.spriteData = options.spriteData;
        this.tracksStack = options.tracksStack;
        this.stackTrackXOffset = options.stackTrackXOffset;
        this.stackTrackYOffset = options.stackTrackYOffset;
        this.xMotion = xMotion;
        this.yMotion = yMotion;
        this.setLifetime(options.lifetime + random.nextInt(options.additionalLifetime + 1));

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

        this.uo = random.nextFloat();
        this.vo = random.nextFloat();
        spriteData.init(this);

        Tmp.c1.set(r1,g1,b1);
        hsv1 = Tmp.c1.toHsv(hsv1);
        Tmp.c2.set(r2,g2,b2);
        hsv2 = Tmp.c2.toHsv(hsv2);
        this.uo = random.nextFloat();
        this.vo = random.nextFloat();
        updateTraits();
    }

    public void pickSprite(int spriteIndex) {
        if (spriteIndex < spriteSet.sprites.size() && spriteIndex >= 0) {
            setSprite(spriteSet.sprites.get(spriteIndex));
        }
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

    public float getCurve(float multiplier) {
        return Mth.clamp((age * multiplier) / (float) lifetime, 0, 1);
    }

    protected void updateTraits() {
        pickColor(colorData.colorCurveEasing.apply(colorData.getProgress(age, lifetime)));
        quadSize = scaleData.getValue(age, lifetime, ss, ms, es);
        alpha = transparencyData.getValue(age, lifetime, st, mt, et);
        oRoll = roll;
        roll = roll + spinData.getValue(age, lifetime, sr, mr, er) + randomSpin;

        if (actor != null) {
            actor.accept(this);
        }
    }

    @Override
    public void render(BufferBuilder bufferBuilder) {
        if (lifeDelay > 0) {
            return;
        }

        if (sprite == null) {
            return;
        }

        spriteData.renderTick(this, partialTicks);
        if (tracksStack) {
            x = ScreenParticleHandler.currentItemX + stackTrackXOffset + xMoved;
            y = ScreenParticleHandler.currentItemY + stackTrackYOffset + yMoved;
        }
        super.render(bufferBuilder);
    }

    public void updateRenderTraits(float partialTicks){
        float time = age + partialTicks;
        pickColor(colorData.colorCurveEasing.apply(colorData.getProgress(time, lifetime)));
        quadSize = scaleData.getValue(time, lifetime, ss, ms, es);
        alpha = transparencyData.getValue(time, lifetime, st, mt, et);
    }

    @Override
    public void tick() {
        if (lifeDelay > 0) {
            lifeDelay--;
            return;
        }

        spriteData.tick(this);
        updateTraits();
        super.tick();
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

    @Override
    public ScreenParticleRenderType getRenderType() {
        return renderType;
    }

    public void setParticleSpeed(Vector3d speed) {
        setParticleSpeed(speed.x, speed.y);
    }
}