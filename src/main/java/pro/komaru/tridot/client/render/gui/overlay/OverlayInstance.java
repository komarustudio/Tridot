package pro.komaru.tridot.client.render.gui.overlay;

import net.neoforged.neoforge.client.event.*;

public interface OverlayInstance{

    default void tick(ClientTickEvent.Post event){
    }

    void onDraw(RenderGuiLayerEvent.Post event);
}
