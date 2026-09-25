package pro.komaru.tridot.client.gfx.particle;

import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.math.*;

public abstract class AbstractParticleBuilder<T extends AbstractParticleBuilder<?>>{
    ArcRandom random = Tmp.rnd;
    double vx = 0, vy = 0, vz = 0;
    double fvx = 0, fvy = 0, fvz = 0;
    double fdx = 0, fdy = 0, fdz = 0;
    double maxXSpeed = 0, maxYSpeed = 0, maxZSpeed = 0;
    double maxXDist = 0, maxYDist = 0, maxZDist = 0;

    @SuppressWarnings("unchecked")
    public T self() {
        return (T) this;
    }

    public T randomVelocity(double maxSpeed){
        randomVelocity(maxSpeed, maxSpeed, maxSpeed);
        return self();
    }

    public T randomVelocity(double maxHSpeed, double maxVSpeed){
        randomVelocity(maxHSpeed, maxVSpeed, maxHSpeed);
        return self();
    }

    public T randomVelocity(double maxXSpeed, double maxYSpeed, double maxZSpeed){
        this.maxXSpeed = maxXSpeed;
        this.maxYSpeed = maxYSpeed;
        this.maxZSpeed = maxZSpeed;
        return self();
    }

    public T flatRandomVelocity(double fvx, double fvy, double fvz){
        this.fvx = fvx;
        this.fvy = fvy;
        this.fvz = fvz;
        return self();
    }

    public T addVelocity(double vx, double vy, double vz){
        this.vx += vx;
        this.vy += vy;
        this.vz += vz;
        return self();
    }

    public T setVelocity(double vx, double vy, double vz){
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        return self();
    }

    public T randomOffset(double maxDistance){
        randomOffset(maxDistance, maxDistance, maxDistance);
        return self();
    }

    public T randomOffset(double maxHDist, double maxVDist){
        randomOffset(maxHDist, maxVDist, maxHDist);
        return self();
    }

    public T randomOffset(double maxXDist, double maxYDist, double maxZDist){
        this.maxXDist = maxXDist;
        this.maxYDist = maxYDist;
        this.maxZDist = maxZDist;
        return self();
    }
}
