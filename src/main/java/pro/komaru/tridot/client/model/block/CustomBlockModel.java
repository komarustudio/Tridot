package pro.komaru.tridot.client.model.block;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;

public abstract class CustomBlockModel extends Model{
    public ModelPart root;

    public CustomBlockModel(ModelPart root){
        super(RenderType::entitySolid);
        this.root = root;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color){
        this.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color){
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
        this.render(poseStack, buffer, packedLight, packedOverlay, FastColor.ARGB32.colorFromFloat(alpha, red, green, blue));
    }
}
