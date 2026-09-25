package pro.komaru.tridot.client.gfx.lightningBolt;

import com.mojang.blaze3d.vertex.*;
import com.mojang.math.*;
import net.minecraft.client.renderer.*;
import net.neoforged.api.distmarker.*;
import org.joml.*;
import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.phys.*;

@OnlyIn(Dist.CLIENT)
public class LightningEffect{

    private static void addLayeredSegments(Vec3 startPos, int segCount, float scaleMod, float segTaper, Col color, float[] segXOffset, float xOffSum, float[] segZOffset, float zOffSum, float relScale, Matrix4f matrix4f, VertexConsumer builder, float segHeight){
        for(int layer = 0; layer < 4; ++layer){
            float red = color.r;
            float green = color.g;
            float blue = color.b;
            float alpha = color.a;
            if(layer == 0) red = green = blue = alpha = 1;

            for(int seg = 0; seg < segCount; seg++){
                float pos = seg / (float)segCount;
                float x = segXOffset[seg] - (xOffSum * pos);
                float z = segZOffset[seg] - (zOffSum * pos);

                float nextPos = (seg + 1) / (float)segCount;
                float nextX = segXOffset[seg + 1] - (xOffSum * nextPos);
                float nextZ = segZOffset[seg + 1] - (zOffSum * nextPos);

                float layerOffsetA = (0.1F + (layer * 0.2F * (1F + segTaper))) * relScale * scaleMod;
                float layerOffsetB = (0.1F + (layer * 0.2F * (1F - segTaper))) * relScale * scaleMod;

                addSegmentQuad(matrix4f, builder, x, startPos.y, z, seg, nextX, nextZ, red, green, blue, alpha, layerOffsetA, layerOffsetB, false, false, true, false, segHeight);    //North Side
                addSegmentQuad(matrix4f, builder, x, startPos.y, z, seg, nextX, nextZ, red, green, blue, alpha, layerOffsetA, layerOffsetB, true, false, true, true, segHeight);      //East Side
                addSegmentQuad(matrix4f, builder, x, startPos.y, z, seg, nextX, nextZ, red, green, blue, alpha, layerOffsetA, layerOffsetB, true, true, false, true, segHeight);      //South Side
                addSegmentQuad(matrix4f, builder, x, startPos.y, z, seg, nextX, nextZ, red, green, blue, alpha, layerOffsetA, layerOffsetB, false, true, false, false, segHeight);    //West Side
            }
        }
    }

    public static void renderLightningP2PRotate(PoseStack mStack, MultiBufferSource getter, Vec3 startPos, Vec3 endPos, LightningInstance cache) {
        mStack.pushPose();
        Vec3 dirVec = endPos.cpy();
        dirVec.sub(startPos);
        dirVec.nor();

        mStack.translate(startPos.x, startPos.y, startPos.z);
        mStack.mulPose(Axis.YP.rotationDegrees(cache.rotY - 90));
        mStack.mulPose(Axis.ZP.rotationDegrees(cache.rotX - 90));
        mStack.translate(-startPos.x, -startPos.y, -startPos.z);

        VertexConsumer buffer = getter.getBuffer(RenderType.lightning());
        Matrix4f matrix = mStack.last().pose();

        addLayeredSegments(startPos, cache.builder.segCount, cache.builder.scaleModifier, cache.builder.segTaper, cache.getColor(), cache.segXOffset, cache.xOffSum, cache.segZOffset, cache.zOffSum, cache.relScale, matrix, buffer, cache.segHeight);
        mStack.popPose();
    }

    private static void addSegmentQuad(Matrix4f matrix4f, VertexConsumer builder, float x1, float yOffset, float z1, int segIndex, float x2, float z2, float red, float green, float blue, float alpha, float offsetA, float offsetB, boolean invA, boolean invB, boolean invC, boolean invD, float segHeight){
        builder.addVertex(matrix4f,
        x1 + (invA ? offsetB : -offsetB),
        yOffset + segIndex * segHeight,
        z1 + (invB ? offsetB : -offsetB))
        .setColor(red, green, blue, alpha);

        builder.addVertex(matrix4f,
        x2 + (invA ? offsetA : -offsetA),
        yOffset + (segIndex + 1F) * segHeight,
        z2 + (invB ? offsetA : -offsetA))
        .setColor(red, green, blue, alpha);

        builder.addVertex(matrix4f,
        x2 + (invC ? offsetA : -offsetA),
        yOffset + (segIndex + 1F) * segHeight,
        z2 + (invD ? offsetA : -offsetA))
        .setColor(red, green, blue, alpha);

        builder.addVertex(matrix4f,
        x1 + (invC ? offsetB : -offsetB),
        yOffset + segIndex * segHeight,
        z1 + (invD ? offsetB : -offsetB))
        .setColor(red, green, blue, alpha);
    }
}