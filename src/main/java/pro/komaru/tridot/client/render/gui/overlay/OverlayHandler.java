package pro.komaru.tridot.client.render.gui.overlay;

import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.*;
import pro.komaru.tridot.util.struct.data.*;

public class OverlayHandler{
    public static final Seq<OverlayInstance> instanceSeq = Seq.with();
    public static void addInstance(OverlayInstance instance) {
        instanceSeq.add(instance);
    }
    public static void killInstance(OverlayInstance instance) {
        instanceSeq.remove(instance);
    }

    public static void renderInstances(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            return;
        }

        instanceSeq.forEach((inst) -> inst.onDraw(event));
    }

    public static void tickInstances(ClientTickEvent.Post event) {
        instanceSeq.forEach((inst) -> inst.tick(event));
    }
}
