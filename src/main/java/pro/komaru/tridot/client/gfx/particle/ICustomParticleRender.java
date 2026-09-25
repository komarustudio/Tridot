package pro.komaru.tridot.client.gfx.particle;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.neoforged.api.distmarker.*;

@OnlyIn(Dist.CLIENT)
public interface ICustomParticleRender{
    void render(PoseStack poseStack, MultiBufferSource buffer, float partialTicks);
}
