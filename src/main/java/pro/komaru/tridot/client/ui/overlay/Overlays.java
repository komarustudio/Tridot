package pro.komaru.tridot.client.ui.overlay;

import lombok.RequiredArgsConstructor;
import org.jetbrains.kotlin.it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import stellar.qrix.infrastructure.exception.AlreadyDefinedException;
import stellar.qrix.infrastructure.exception.NotFoundException;
import pro.komaru.tridot.client.ui.overlay.interfaces.Overlay;
import pro.komaru.tridot.client.ui.overlay.render.OverlayRenderer;

import java.util.Map;

/**
 * A namespace-scoped overlay registry for a specific mod.
 * Holds and manages {@link OverlayHolder} instances registered under this namespace.
 *
 * @see OverlayManager#forNamespace(String)
 * @see OverlayManager#getForNamespace(String)
 */
@RequiredArgsConstructor
public class Overlays {
    private final String namespaceId;

    private final Map<String, OverlayHolder<?>> holders = new Object2ObjectOpenHashMap<>();

    /**
     * Empty helper method to trigger static initialization of classes
     * that register overlays in their static blocks.
     */
    public void register() {

    }

    /**
     * Helper method to register custom renderer for this registry
     *
     * @param renderer the overlay renderer
     */
    public void registerCustomRenderer(OverlayRenderer renderer) {
        OverlayManager.getInstance().registerCustomRenderer(namespaceId, renderer);
    }

    /**
     * Registers an overlay under the given id within this namespace.
     * The overlay instance is created immediately via the provided initializer.
     *
     * @param holderId the unique id for this overlay within the namespace
     * @param type     the overlay class
     * @param instance the overlay instance
     * @param <T>      the overlay type
     * @return the registered {@link OverlayHolder}
     * @throws AlreadyDefinedException if an overlay with the same id is already registered
     */
    public <T extends Overlay> OverlayHolder<T> register(String holderId, Class<T> type,
                                                         T instance) {
        var holder = new OverlayHolder<>(type, instance);

        if (holders.putIfAbsent(holderId, holder) != null)
            throw new AlreadyDefinedException(Overlay.class, namespaceId + ":" + holderId);

        return holder;
    }

    /**
     * Returns the overlay holder for the given id, validated against the expected type.
     *
     * @param holderId the overlay id
     * @param type     the expected overlay class
     * @param <T>      the overlay type
     * @return the {@link OverlayHolder} for the given id
     * @throws NotFoundException     if no overlay is registered under the given id
     * @throws IllegalStateException if the registered overlay type does not match the expected type
     */
    @SuppressWarnings("unchecked")
    public <T extends Overlay> OverlayHolder<T> get(String holderId, Class<T> type) {
        var holder = holders.get(holderId);

        if (holder == null)
            throw new NotFoundException(Overlay.class, namespaceId + ":" + holderId);

        if (!holder.matchesType(type))
            throw new IllegalStateException("Overlay type does not match: expected " + type.getName());

        return (OverlayHolder<T>) holder;
    }

    /**
     * Returns the overlay holder for the given id without type validation.
     *
     * @param holderId the overlay id
     * @return the {@link OverlayHolder} for the given id
     * @throws NotFoundException if no overlay is registered under the given id
     */
    public OverlayHolder<?> get(String holderId) {
        var holder = holders.get(holderId);

        if (holder == null)
            throw new NotFoundException(Overlay.class, namespaceId + ":" + holderId);

        return holder;
    }

    /**
     * Returns all overlay holders
     *
     * @return overlay holder map
     */
    public Map<String, OverlayHolder<?>> getAll() {
        return Map.copyOf(holders);
    }
}