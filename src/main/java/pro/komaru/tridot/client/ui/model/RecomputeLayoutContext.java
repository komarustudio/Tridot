package pro.komaru.tridot.client.ui.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class RecomputeLayoutContext {
    float availableWidth, availableHeight;
    float anchorX, anchorY;
}
