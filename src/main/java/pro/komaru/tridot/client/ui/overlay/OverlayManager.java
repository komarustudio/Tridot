package pro.komaru.tridot.client.ui.overlay;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pro.komaru.tridot.client.ui.overlay.render.OverlayRenderHook;
import pro.komaru.tridot.client.ui.overlay.render.OverlayRenderer;

import java.util.Map;

/**
 * Global manager for all mod overlay registries.
 * Holds {@link Overlays} instances scoped per namespace (mod id).
 *
 * <p>Usage:
 * <pre>{@code
 * Overlays overlays = OverlayManager.getForNamespace("mymodid");
 * }</pre>
 */
public class OverlayManager {
    /**
     * The singleton instance of this manager.
     */
    @Getter
    private static final OverlayManager instance = new OverlayManager();

    private static final Map<String, Overlays> registries = new Object2ObjectOpenHashMap<>();
    private static final Map<String, OverlayRenderHook> renderHooks = new Object2ObjectOpenHashMap<>();

    /**
     * RenderGuiEvent.Post event listener to call render hooks
     * Retrieves all render hooks - or sets to default if custom hook
     * was not registered, and calls them.
     *
     * @param event the event object
     */
    @SubscribeEvent
    public void onRender(RenderGuiEvent.Post event) {
        for (String namespace : registries.keySet()) {
            var registry = forNamespace(namespace);
            var hook = renderHooks.computeIfAbsent(namespace, id ->
                    new OverlayRenderHook(OverlayRenderer.DEFAULT, registry));

            hook.onRenderGuiPost(event);
        }
    }

    /**
     * Static shortcut for {@link #forNamespace(String)}.
     *
     * @param namespaceId the mod/namespace id
     * @return the {@link Overlays} registry for the given namespace
     */
    public static Overlays getForNamespace(String namespaceId) {
        return getInstance().forNamespace(namespaceId);
    }

    /**
     * Returns the {@link Overlays} registry for the given namespace,
     * creating it if it does not exist yet.
     *
     * @param namespaceId the mod/namespace id
     * @return the {@link Overlays} registry for the given namespace
     */
    public Overlays forNamespace(String namespaceId) {
        return registries.computeIfAbsent(namespaceId, Overlays::new);
    }

    /**
     * Registers a custom render hook for this namespace.
     *
     * @param namespaceId the namespace id
     * @param renderer    the overlay renderer
     */
    public void registerCustomRenderer(String namespaceId, OverlayRenderer renderer) {
        var registry = forNamespace(namespaceId);
        var hook = new OverlayRenderHook(renderer, registry);

        if (renderHooks.putIfAbsent(namespaceId, hook) != null)
            throw new IllegalStateException("OverlayRenderer already defined for namespace: "+namespaceId);
    }
}
