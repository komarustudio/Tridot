package pro.komaru.tridot.client.render.screenshake;

import net.minecraft.world.phys.*;
import pro.komaru.tridot.util.Tmp;
import pro.komaru.tridot.util.math.ArcRandom;
import pro.komaru.tridot.util.math.Interp;
import net.minecraft.client.*;
import net.minecraft.util.*;

public class ScreenshakeInstance{
    public int progress;
    public final int duration;
    public float intensity1, intensity2, intensity3;
    public float vx = 0, vy = 0, vz = 0;
    public Interp intensityCurveStartEasing = Interp.linear, intensityCurveEndEasing = Interp.linear;

    public boolean isNormalize = true;
    public boolean isRotation = true;
    public boolean isPosition = false;
    public boolean isVector = false;
    public boolean isFov = false;
    public boolean isFovNormalize = false;

    public static final ArcRandom random = Tmp.rnd;

    public ScreenshakeInstance(int duration){
        this.duration = duration;
    }

    public ScreenshakeInstance intensity(float intensity){
        return intensity(intensity, intensity);
    }

    public ScreenshakeInstance intensity(float intensity1, float intensity2){
        return intensity(intensity1, intensity2, intensity2);
    }

    public ScreenshakeInstance intensity(float intensity1, float intensity2, float intensity3){
        this.intensity1 = intensity1;
        this.intensity2 = intensity2;
        this.intensity3 = intensity3;
        return this;
    }

    public ScreenshakeInstance interp(Interp interp){
        return interp(interp, interp);
    }

    public ScreenshakeInstance interp(Interp intensityCurveStartEasing, Interp intensityCurveEndEasing){
        this.intensityCurveStartEasing = intensityCurveStartEasing;
        this.intensityCurveEndEasing = intensityCurveEndEasing;
        return this;
    }

    public ScreenshakeInstance normalize(boolean normalize){
        this.isNormalize = normalize;
        return this;
    }

    public ScreenshakeInstance rot(boolean rotation){
        this.isRotation = rotation;
        return this;
    }

    public ScreenshakeInstance pos(boolean position){
        this.isPosition = position;
        return this;
    }

    public ScreenshakeInstance vec(boolean vector){
        this.isVector = vector;
        return this;
    }

    public ScreenshakeInstance vec(Vec3 vector){
        this.vx = (float) vector.x;
        this.vy = (float) vector.y;
        this.vz = (float) vector.z;
        return this;
    }

    public ScreenshakeInstance vec(float x, float y, float z){
        this.vx = x;
        this.vy = y;
        this.vz = z;
        return this;
    }

    public ScreenshakeInstance randVec(){
        double angleA = random.nextDouble() * Math.PI * 2;
        double angleB = random.nextDouble() * Math.PI * 2;
        this.vx = (float)(Math.cos(angleA) * Math.cos(angleB));
        this.vy = (float)(Math.sin(angleA) * Math.cos(angleB));
        this.vz = (float)Math.sin(angleB);
        return this;
    }

    public ScreenshakeInstance fov(boolean fov){
        this.isFov = fov;
        return this;
    }

    public ScreenshakeInstance normalizeFov(boolean fovNormalize){
        this.isFovNormalize = fovNormalize;
        return this;
    }

    public float updateIntensity(Camera camera){
        progress++;
        float percentage = progress / (float)duration;
        if(intensity2 != intensity3){
            float v = percentage >= 0.5f ? (percentage - 0.5f)/0.5f : percentage/0.5f;
            return Mth.lerp(intensityCurveEndEasing.apply(v), intensity2, intensity1);
        }else{
            return Mth.lerp(intensityCurveStartEasing.apply(percentage), intensity1, intensity2);
        }
    }
}