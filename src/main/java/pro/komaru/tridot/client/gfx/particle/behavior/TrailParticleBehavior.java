package pro.komaru.tridot.client.gfx.particle.behavior;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;
import pro.komaru.tridot.client.gfx.TridotParticles;
import pro.komaru.tridot.client.gfx.particle.GenericParticle;
import pro.komaru.tridot.client.gfx.particle.behavior.component.TrailParticleBehaviorComponent;
import pro.komaru.tridot.client.gfx.particle.data.ColorParticleData;
import pro.komaru.tridot.client.gfx.particle.data.GenericParticleData;
import pro.komaru.tridot.client.gfx.particle.data.SpinParticleData;
import pro.komaru.tridot.client.render.RenderBuilder;
import pro.komaru.tridot.client.gfx.trail.TrailPoint;
import pro.komaru.tridot.client.gfx.trail.TrailPointBuilder;
import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.phys.*;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.*;

public class TrailParticleBehavior extends ParticleBehavior implements ICustomBehaviorParticleRender{
    public ColorParticleData colorData;
    public GenericParticleData transparencyData;
    public boolean secondColor;
    public int trailSize;
    Function<Float, Float> widthFunc;

    public TrailParticleBehavior(ColorParticleData colorData, GenericParticleData transparencyData, boolean secondColor, int trailSize, Function<Float, Float> widthFunc, SpinParticleData xSpinData, SpinParticleData ySpinData, SpinParticleData zSpinData, float xOffset, float yOffset, float zOffset, boolean firstSide, boolean secondSide, boolean camera, boolean xRotCam, boolean yRotCam){
        super(xSpinData, ySpinData, zSpinData, xOffset, yOffset, zOffset, firstSide, secondSide, camera, xRotCam, yRotCam);
        this.colorData = colorData;
        this.transparencyData = transparencyData;
        this.secondColor = secondColor;
        this.trailSize = trailSize;
        this.widthFunc = widthFunc;
    }

    public TrailParticleBehavior copy(){
        return new TrailParticleBehavior(colorData, transparencyData, secondColor, trailSize, widthFunc, xSpinData, ySpinData, zSpinData, xOffset, yOffset, zOffset, firstSide, secondSide, camera, xRotCam, yRotCam);
    }

    public static TrailParticleBehaviorBuilder create(){
        return new TrailParticleBehaviorBuilder(0, 0, 0);
    }

    public static TrailParticleBehaviorBuilder create(float xOffset, float yOffset, float zOffset){
        return new TrailParticleBehaviorBuilder((float)Math.toRadians(xOffset), (float)Math.toRadians(yOffset), (float)Math.toRadians(zOffset));
    }

    public TrailParticleBehaviorComponent getComponent(){
        return new TrailParticleBehaviorComponent();
    }

    public TrailParticleBehaviorComponent getTrailComponent(GenericParticle particle){
        if(particle.behaviorComponent instanceof TrailParticleBehaviorComponent behaviorComponent){
            return behaviorComponent;
        }

        return getComponent();
    }

    @Override
    public void init(GenericParticle particle){
        super.init(particle);
        TrailParticleBehaviorComponent component = getTrailComponent(particle);
        component.trailPointBuilder = TrailPointBuilder.create(trailSize);

        float r1 = GenericParticle.pickRandomValue(colorData.r1, colorData.rr11, colorData.rr12);
        float g1 = GenericParticle.pickRandomValue(colorData.g1, colorData.rg11, colorData.rg12);
        float b1 = GenericParticle.pickRandomValue(colorData.b1, colorData.rb11, colorData.rb12);
        float r2 = GenericParticle.pickRandomValue(colorData.r2, colorData.rr21, colorData.rr22);
        float g2 = GenericParticle.pickRandomValue(colorData.g2, colorData.rg21, colorData.rg22);
        float b2 = GenericParticle.pickRandomValue(colorData.b2, colorData.rb21, colorData.rb21);

        component.st = GenericParticle.pickRandomValue(transparencyData.startingValue, transparencyData.rs1, transparencyData.rs2);
        component.mt = GenericParticle.pickRandomValue(transparencyData.middleValue, transparencyData.rm1, transparencyData.rm2);
        component.et = GenericParticle.pickRandomValue(transparencyData.endingValue, transparencyData.re1, transparencyData.re2);

        Tmp.c1.set(r1,g1,b1);
        component.hsv1 = Tmp.c1.toHsv(component.hsv1);
        Tmp.c2.set(r2,g2,b2);
        component.hsv2 = Tmp.c2.toHsv(component.hsv2);
    }

    public void pickColor(GenericParticle particle, float coeff){
        TrailParticleBehaviorComponent component = getTrailComponent(particle);
        float[] hsv1 = component.hsv1, hsv2 = component.hsv2;
        var col = Col.HSVtoRGB(hsv1[0], hsv1[1] * 100f, hsv1[2] * 100f);
        var col2 = Col.HSVtoRGB(hsv2[0], hsv2[1] * 100f, hsv2[2] * 100f);
        col.lerp(col2,coeff);
        setColor(particle, col.r,col.g,col.b);
    }

    public void setColor(GenericParticle particle, float r, float g, float b){
        TrailParticleBehaviorComponent component = getTrailComponent(particle);
        component.r = r;
        component.g = g;
        component.b = b;
    }

    @Override
    public void updateTraits(GenericParticle particle){
        TrailParticleBehaviorComponent component = getTrailComponent(particle);
        component.trailPointBuilder.add(Vec3f.from(particle.getPosition()));
        component.trailPointBuilder.tick();

        pickColor(particle, colorData.colorCurveEasing.apply(colorData.getProgress(particle.age, particle.lifetime)));
        component.a = transparencyData.getValue(particle.age, particle.lifetime, component.st, component.mt, component.et);
    }

    @Override
    public void updateRenderTraits(GenericParticle particle, float partialTicks){
        TrailParticleBehaviorComponent component = getTrailComponent(particle);
        pickColor(particle, colorData.colorCurveEasing.apply(colorData.getProgress(particle.age, particle.lifetime)));
        component.a = transparencyData.getValue(particle.age, particle.lifetime, component.st, component.mt, component.et);
    }

    @Override
    public void render(GenericParticle particle, VertexConsumer vertexConsumer, Camera renderInfo, float partialTicks){
        if(particle.shouldRenderTraits) updateRenderTraits(particle, partialTicks);
        TridotParticles.addBehaviorParticleList(particle, this);
    }

    @Override
    public void render(GenericParticle particle, PoseStack poseStack, MultiBufferSource buffer, float partialTicks){
        TrailParticleBehaviorComponent component = getTrailComponent(particle);
        List<TrailPoint> trail = new ArrayList<>(component.trailPointBuilder.points());
        if(trail.size() > 2 && particle.getAge() > component.trailPointBuilder.trailLength.get()){
            TrailPoint position = trail.get(0);
            TrailPoint nextPosition = trail.get(1);
            float x = Mth.lerp(partialTicks, position.getPosition().x, nextPosition.getPosition().x);
            float y = Mth.lerp(partialTicks, position.getPosition().y, nextPosition.getPosition().y);
            float z = Mth.lerp(partialTicks, position.getPosition().z, nextPosition.getPosition().z);
            trail.set(0, new TrailPoint(new Vec3f(x, y, z)));
        }

        float x = (float)(Mth.lerp(partialTicks, particle.xo, particle.x));
        float y = (float)(Mth.lerp(partialTicks, particle.yo, particle.y));
        float z = (float)(Mth.lerp(partialTicks, particle.zo, particle.z));
        if(!trail.isEmpty()){
            trail.set(trail.size() - 1, new TrailPoint(new Vec3f(x, y, z)));
        }

        RenderBuilder builder = RenderBuilder.create().setRenderType(particle.renderType).setSided(firstSide, secondSide)
        .setUV(particle.getU0(), particle.getV0(), particle.getU1(), particle.getV1())
        .setColorRaw(particle.getRed(), particle.getGreen(), particle.getBlue())
        .setAlpha(particle.getAlpha())
        .setLight(particle.getLightColor(partialTicks));
        if(secondColor){
            builder.setSecondColorRaw(component.r, component.g, component.b)
            .setSecondAlpha(component.a);
        }
        if(widthFunc == null) widthFunc = (f) -> f;
        builder.renderTrail(poseStack, trail, (f) -> widthFunc.apply(f) * (particle.getSize() / 2f));
    }
}
