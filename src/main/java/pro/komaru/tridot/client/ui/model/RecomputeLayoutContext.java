package pro.komaru.tridot.client.ui.model;

public record RecomputeLayoutContext(float availableWidth, float availableHeight, float anchorX, float anchorY) {
    public RecomputeLayoutContext(float availableWidth, float availableHeight) {
        this(availableWidth, availableHeight, 0.0f, 0.0f);
    }

    public RecomputeLayoutContext withAnchor(float x, float y) {
        return new RecomputeLayoutContext(availableWidth, availableHeight, x, y);
    }
}