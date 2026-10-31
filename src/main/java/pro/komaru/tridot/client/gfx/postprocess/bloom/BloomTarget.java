package pro.komaru.tridot.client.gfx.postprocess.bloom;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * RGBA16F render target for bloom.
 * <p>
 * Float format lets overlapping glowing layers add up past 1.0 (HDR-ish) and keeps the blur chain from banding.
 * Optionally borrows the main target's depth texture (no copy) so glowing geometry is correctly hidden behind walls,
 * the mob's own body, etc.
 */
public class BloomTarget extends TextureTarget {

    public BloomTarget(int width, int height) {
        super(width, height, false, Minecraft.ON_OSX);
        this.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.setFilterMode(GL11.GL_LINEAR);
    }

    @Override
    public void createBuffers(int width, int height, boolean clearError) {
        super.createBuffers(width, height, clearError);
        // Re-specify colour storage as half float. The texture stays attached to the FBO.
        GlStateManager._bindTexture(this.colorTextureId);
        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, this.width, this.height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, null);
        GlStateManager._bindTexture(0);
    }

    /**
     * Attaches the given target's depth (and stencil, if Forge enabled it) texture to this FBO.
     * Cheap, so it is simply redone every frame, which also survives main target re-creation.
     */
    public void shareDepthWith(RenderTarget main) {
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.frameBufferId);
        int attachment = main.isStencilEnabled() ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : GL30.GL_DEPTH_ATTACHMENT;
        GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachment, GL11.GL_TEXTURE_2D, main.getDepthTextureId(), 0);
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
}
