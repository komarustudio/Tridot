package pro.komaru.tridot.client.gfx.particle;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.core.Direction.*;
import net.minecraft.util.*;
import net.minecraftforge.api.distmarker.*;
import org.jetbrains.annotations.*;
import org.joml.*;

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
        float partialTicks = Minecraft.getInstance().getPartialTick();
        float size = getQuadSize(partialTicks) * 10;
        float u0 = getU0();
        float u1 = getU1();
        float v0 = getV0();
        float v1 = getV1();

        float roll = Mth.lerp(partialTicks, this.oRoll, this.roll);
        float rad = roll * Mth.DEG_TO_RAD;
        float cos = Mth.cos(rad) * size;
        float sin = Mth.sin(rad) * size;

        float px = (float) this.x;
        float py = (float) this.y;
        float quadZ = getQuadZPosition();

        float x0 = -cos - sin + px;
        float y0 =  sin - cos + py;

        float x1 = -cos + sin + px;
        float y1 =  sin + cos + py;

        float x2 =  cos + sin + px;
        float y2 = -sin + cos + py;

        float x3 =  cos - sin + px;
        float y3 = -sin - cos + py;

        bufferBuilder.vertex(x0, y0, quadZ).uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).endVertex();
        bufferBuilder.vertex(x1, y1, quadZ).uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).endVertex();
        bufferBuilder.vertex(x2, y2, quadZ).uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha).endVertex();
        bufferBuilder.vertex(x3, y3, quadZ).uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha).endVertex();
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