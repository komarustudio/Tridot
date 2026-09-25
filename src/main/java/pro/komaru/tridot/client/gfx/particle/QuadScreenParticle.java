package pro.komaru.tridot.client.gfx.particle;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.core.Direction.*;
import net.minecraft.util.*;
import net.neoforged.api.distmarker.*;
import org.jetbrains.annotations.*;
import org.joml.*;
import pro.komaru.tridot.client.ClientTick;

@OnlyIn(Dist.CLIENT)
public abstract class QuadScreenParticle extends ScreenParticle {
    protected float quadSize = 0.1F * (random.nextFloat() * 0.5F + 0.5F) * 2.0F;

    protected QuadScreenParticle(ClientLevel pLevel, double pX, double pY) {
        super(pLevel, pX, pY);
    }

    protected QuadScreenParticle(ClientLevel pLevel, double pX, double pY, double pXSpeed, double pYSpeed) {
        super(pLevel, pX, pY, pXSpeed, pYSpeed);
    }

    @Override
    public void render(BufferBuilder bufferBuilder) {
        float partialTicks = ClientTick.mcPartialTick();
        float size = getQuadSize(partialTicks) * 10;
        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();
        Vector3f[] vectors = getVector3fs(partialTicks, size);
        float quadZ = getQuadZPosition();
        bufferBuilder.addVertex(vectors[0].x(), vectors[0].y(), quadZ).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha);
        bufferBuilder.addVertex(vectors[1].x(), vectors[1].y(), quadZ).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha);
        bufferBuilder.addVertex(vectors[2].x(), vectors[2].y(), quadZ).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha);
        bufferBuilder.addVertex(vectors[3].x(), vectors[3].y(), quadZ).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha);
    }

    private Vector3f @NotNull [] getVector3fs(float partialTicks, float size){
        float roll = Mth.lerp(partialTicks, this.oRoll, this.roll);
        Vector3f[] vectors = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        Quaternionf rotation = new Quaternionf(Axis.ZN.rotationDegrees(roll));
        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = vectors[i];
            vector3f.rotate(rotation);
            vector3f.mul(size);
            vector3f.add((float) x, (float) y, 0);
        }
        return vectors;
    }

    public float getQuadSize(float partialTicks) {
        return this.quadSize;
    }

    public float getQuadZPosition() {
        return 390;
    }

    protected abstract float getU0();

    protected abstract float getU1();

    protected abstract float getV0();

    protected abstract float getV1();
}