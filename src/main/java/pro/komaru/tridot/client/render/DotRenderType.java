package pro.komaru.tridot.client.render;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;

public class DotRenderType extends RenderType {
    public final CompositeState state;

    public static final RenderStateShard.ShaderStateShard LIGHTNING_SHADER = RENDERTYPE_LIGHTNING_SHADER;
    public static final RenderStateShard.TransparencyStateShard LIGHTNING_TRANSPARENCY_STATE = LIGHTNING_TRANSPARENCY;

    public static DotRenderType createRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, CompositeState state){
        return new DotRenderType(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, state);
    }

    public DotRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, CompositeState state){
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, () -> {
            state.states.forEach(RenderStateShard::setupRenderState);
        }, () -> {
            state.states.forEach(RenderStateShard::clearRenderState);
        });
        this.state = state;
    }
}
