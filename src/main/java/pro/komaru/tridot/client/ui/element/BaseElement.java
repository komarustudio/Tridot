package pro.komaru.tridot.client.ui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import pro.komaru.tridot.client.ui.enums.Alignment;
import pro.komaru.tridot.client.ui.enums.AlignmentDirection;
import pro.komaru.tridot.client.ui.enums.MouseButtonType;
import pro.komaru.tridot.client.ui.model.RecomputeLayoutContext;
import pro.komaru.tridot.client.ui.model.RecomputeMeasurementsContext;
import pro.komaru.tridot.util.struct.data.Pair;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BaseElement<T extends BaseElement<T>> implements UIElement {
    private float minWidth = -1f, minHeight = -1f;
    private float maxWidth = -1f, maxHeight = -1f;

    private UIElement parent;

    private float width, height;
    private float relativeX, relativeY;
    private float absoluteX, absoluteY;
    private float padLeft, padRight, padBottom, padTop;
    private float marginLeft, marginRight, marginBottom, marginTop;

    public AlignmentDirection childrenAlignment = AlignmentDirection.ROW;
    private int alignment;

    public float growX = 0f, growY = 0f;
    public boolean overflowX = false, overflowY = false;
    private boolean hovered = false;
    private boolean pressed = false;
    public float renderOffsetX, renderOffsetY;

    private List<UIElement> children = new CopyOnWriteArrayList<>();

    public float getMaxHeight(){
        return this.maxHeight;
    }

    public float getMaxWidth(){
        return this.maxWidth;
    }

    public void setMaxHeight(float maxHeight){
        this.maxHeight = maxHeight;
    }

    public void setMaxWidth(float maxWidth){
        this.maxWidth = maxWidth;
    }

    public float getHeight(){
        return this.height;
    }

    public float getWidth(){
        return this.width;
    }

    public void setHeight(float height){
        this.height = height;
    }

    public void setWidth(float width){
        this.width = width;
    }

    public float getMinHeight(){
        return this.minHeight;
    }

    public float getMinWidth(){
        return this.minWidth;
    }

    public void setMinHeight(float minHeight){
        this.minHeight = minHeight;
    }

    public void setMinWidth(float minWidth){
        this.minWidth = minWidth;
    }

    @Override
    public UIElement getParent(){
        return parent;
    }

    @Override
    public void setParent(UIElement parent){
        this.parent = parent;
    }

    public float getRelativeY(){
        return this.relativeY;
    }

    public float getRelativeX(){
        return this.relativeX;
    }

    public void setRelativeY(float relativeY){
        this.relativeY = relativeY;
    }

    public void setRelativeX(float relativeX){
        this.relativeX = relativeX;
    }

    public int getAlignment(){
        return alignment;
    }

    public void setAlignment(int alignment){
        this.alignment = alignment;
    }

    public AlignmentDirection getChildrenAlignment(){
        return childrenAlignment;
    }

    public void setChildrenAlignment(AlignmentDirection childrenAlignment){
        this.childrenAlignment = childrenAlignment;
    }

    public void setChildren(List<UIElement> children){
        this.children = children;
    }

    public void setHovered(boolean hovered){
        this.hovered = hovered;
    }

    public boolean isHovered(){
        return hovered;
    }

    public boolean isPressed(){
        return pressed;
    }

    public void setPressed(boolean pressed){
        this.pressed = pressed;
    }

    public float getAbsoluteY(){
        return this.absoluteY;
    }

    public float getAbsoluteX(){
        return this.absoluteX;
    }

    public void setAbsoluteY(float absoluteY){
        this.absoluteY = absoluteY;
    }

    public void setAbsoluteX(float absoluteX){
        this.absoluteX = absoluteX;
    }

    public float getPadLeft(){
        return padLeft;
    }

    public void setPadLeft(float padLeft){
        this.padLeft = padLeft;
    }

    public float getPadBottom(){
        return padBottom;
    }

    public void setPadBottom(float padBottom){
        this.padBottom = padBottom;
    }

    public float getPadRight(){
        return padRight;
    }

    public void setPadRight(float padRight){
        this.padRight = padRight;
    }

    public float getPadTop(){
        return padTop;
    }

    public void setPadTop(float padTop){
        this.padTop = padTop;
    }

    @Override
    public float getMarginLeft(){
        return marginLeft;
    }

    public void setMarginLeft(float marginLeft){
        this.marginLeft = marginLeft;
    }

    @Override
    public float getMarginBottom(){
        return marginBottom;
    }

    public void setMarginBottom(float marginBottom){
        this.marginBottom = marginBottom;
    }

    @Override
    public float getMarginRight(){
        return marginRight;
    }

    public void setMarginRight(float marginRight){
        this.marginRight = marginRight;
    }

    @Override
    public float getMarginTop(){
        return marginTop;
    }

    public void setMarginTop(float marginTop){
        this.marginTop = marginTop;
    }

    public float getRenderOffsetX(){
        return renderOffsetX;
    }

    public float getRenderOffsetY(){
        return renderOffsetY;
    }

    public void setRenderOffsetX(float renderOffsetX){
        this.renderOffsetX = renderOffsetX;
    }

    public void setRenderOffsetY(float renderOffsetY){
        this.renderOffsetY = renderOffsetY;
    }

    public boolean isOverflowX(){
        return overflowX;
    }

    public void setOverflowX(boolean overflowX){
        this.overflowX = overflowX;
    }

    public boolean isOverflowY(){
        return overflowY;
    }

    public void setOverflowY(boolean overflowY){
        this.overflowY = overflowY;
    }

    public float getGrowX(){
        return growX;
    }

    public void setGrowX(float growX){
        this.growX = growX;
    }

    public float getGrowY(){
        return growY;
    }

    public void setGrowY(float growY){
        this.growY = growY;
    }

    public abstract void renderElement(Minecraft mc, GuiGraphics gui, float pt);

    public T minDims(float w, float h) {
        setMinWidth(w);
        setMinHeight(h);
        return self();
    }

    public T maxDims(float w, float h) {
        setMaxWidth(w);
        setMaxHeight(h);
        return self();
    }

    public T minWidth(float value) {
        setMinWidth(value);
        return self();
    }

    public T maxWidth(float value) {
        setMaxWidth(value);
        return self();
    }

    public T minHeight(float value) {
        setMinHeight(value);
        return self();
    }

    public T maxHeight(float value) {
        setMaxHeight(value);
        return self();
    }

    public T noMinWidth() {
        setMinWidth(-1f);
        return self();
    }

    public T noMaxWidth() {
        setMaxWidth(-1f);
        return self();
    }

    public T noMinHeight() {
        setMinHeight(-1f);
        return self();
    }

    public T noMaxHeight() {
        setMaxHeight(-1f);
        return self();
    }

    public T renderOffset(float x, float y) {
        setRenderOffsetX(x);
        setRenderOffsetY(y);

        return self();
    }

    public T overflow(boolean x, boolean y) {
        setOverflowX(x);
        setOverflowY(y);

        return self();
    }

    public T growX(float value) {
        setGrowX(value);
        return self();
    }

    public T growY(float value) {
        setGrowY(value);
        return self();
    }

    public T grow(float x, float y) {
        setGrowX(x);
        setGrowY(y);
        return self();
    }

    public T growX() {
        setGrowX(1f);
        return self();
    }

    public T growY() {
        setGrowY(1f);
        return self();
    }

    public T grow() {
        setGrowX(1f);
        setGrowY(1f);
        return self();
    }

    public T alignSelf(int alignment) {
        setAlignment(alignment);

        return self();
    }

    public T alignLeft() {
        return alignSelf(getAlignment() | Alignment.LEFT);
    }

    public T alignRight() {
        return alignSelf(getAlignment() | Alignment.RIGHT);
    }

    public T alignTop() {
        return alignSelf(getAlignment() | Alignment.TOP);
    }

    public T alignBottom() {
        return alignSelf(getAlignment() | Alignment.BOTTOM);
    }

    public T alignCenterX() {
        return alignSelf(getAlignment() | Alignment.CENTER_X);
    }

    public T alignCenterY() {
        return alignSelf(getAlignment() | Alignment.CENTER_Y);
    }

    public T alignCenter() {
        return alignSelf(Alignment.CENTER);
    }

    public T alignChildren(AlignmentDirection direction) {
        setChildrenAlignment(direction);
        return self();
    }

    public T margin(float left, float top, float right, float bottom) {
        setMarginLeft(left);
        setMarginTop(top);
        setMarginRight(right);
        setMarginBottom(bottom);

        return self();
    }

    public T margin(float val) {
        return margin(val, val, val, val);
    }

    public T pad(float left, float top, float right, float bottom) {
        setPadLeft(left);
        setPadTop(top);
        setPadRight(right);
        setPadBottom(bottom);

        return self();
    }

    public T pad(float val) {
        return pad(val, val, val, val);
    }

    public T addChild(UIElement element) {
        children.add(element);
        element.setParent(this);
        return self();
    }

    public T removeChild(UIElement element) {
        children.remove(element);
        element.setParent(null);
        return self();
    }

    @Override
    public void render(Minecraft mc, GuiGraphics gui, float pt, float mx, float my) {
        var p = gui.pose();

        p.pushPose();
        p.translate(relativeX, relativeY, 0);
        p.translate(renderOffsetX, renderOffsetY, 0);

        hovered = isMouseOver(mx, my);

        renderElement(mc, gui, pt);

        for (UIElement child : children)
            child.render(mc, gui, pt, mx, my);

        p.popPose();
    }

    @Override
    public boolean isMouseOver(float mouseX, float mouseY) {
        return mouseX >= absoluteX && mouseX <= absoluteX + width &&
                mouseY >= absoluteY && mouseY <= absoluteY + height;
    }

    @Override
    public boolean mouseDown(float mouseX, float mouseY, MouseButtonType mouseButtonType) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        for (int i = children.size() - 1; i >= 0; i--) {
            UIElement child = children.get(i);
            if (!child.isMouseOver(mouseX, mouseY)) continue;
            if (!child.mouseDown(mouseX, mouseY, mouseButtonType)) continue;
            return true;
        }

        pressed = true;

        return onMouseDown(mouseX, mouseY, mouseButtonType);
    }

    @Override
    public void mouseUp(float mouseX, float mouseY, MouseButtonType mouseButtonType) {
        for (UIElement child : children) {
            child.mouseUp(mouseX, mouseY, mouseButtonType);
        }

        if (isPressed() && isMouseOver(mouseX, mouseY))
            onMouseClicked(mouseX, mouseY, mouseButtonType);

        pressed = false;

        onMouseUp(mouseX, mouseY, mouseButtonType);
    }

    protected void onMouseClicked(float mouseX, float mouseY, MouseButtonType mouseButtonType) {}

    protected boolean onMouseDown(float mouseX, float mouseY, MouseButtonType mouseButtonType) {return false;}

    protected void onMouseUp(float mouseX, float mouseY, MouseButtonType mouseButtonType) {}

    @Override
    public void recomputeMeasurements(RecomputeMeasurementsContext context) {
        float maxW = maxWidth < 0 ? Float.MAX_VALUE : maxWidth;
        float maxH = maxHeight < 0 ? Float.MAX_VALUE : maxHeight;

        float currentAvailW = Math.min(context.availableWidth(), maxW - (padLeft + padRight));
        float currentAvailH = Math.min(context.availableHeight(), maxH - (padTop + padBottom));

        for (UIElement child : children) {
            var childContext = new RecomputeMeasurementsContext(currentAvailW - (child.getMarginLeft() + child.getMarginRight()), currentAvailH - (child.getMarginTop() + child.getMarginBottom()));
            child.recomputeMeasurements(childContext);
            switch (childrenAlignment) {
                case ROW -> {
                    float occupied = child.getWidth() + child.getMarginLeft() + child.getMarginRight();
                    currentAvailW = Math.max(0, currentAvailW - occupied);
                }
                
                case COLUMN -> {
                    float occupied = child.getHeight() + child.getMarginTop() + child.getMarginBottom();
                    currentAvailH = Math.max(0, currentAvailH - occupied);
                }
            }
        }

        width = height = 0;
        recomputeInternalDimensions(context);
    }

    @Override
    public void recomputeLayout(RecomputeLayoutContext context) {
        recomputeOuterDimensions(context);
        recomputePosition(context);

        recomputeChildrenLayout(context);

        recomputeAbsolutePosition();
    }

    protected void recomputeAbsolutePosition() {
        float absX = relativeX;
        float absY = relativeY;
        UIElement currentParent = parent;

        while (currentParent != null) {
            if (currentParent instanceof BaseElement<?> be) {
                absX += be.getRelativeX();
                absY += be.getRelativeY();
            }
            currentParent = currentParent.getParent();
        }

        absoluteX = absX;
        absoluteY = absY;
    }

    protected void recomputeChildrenLayout(RecomputeLayoutContext context) {
        float availableWidth = width - (padLeft + padRight);
        float availableHeight = height - (padTop + padBottom);

        float anchorX = padLeft;
        float anchorY = padTop;

        for (UIElement child : children) {
            var updatedContext = new RecomputeLayoutContext(availableWidth - anchorX, availableHeight - anchorY, anchorX, anchorY);
            child.recomputeLayout(updatedContext);
            switch (childrenAlignment) {
                case ROW -> anchorX += child.getWidth() + child.getMarginLeft() + child.getMarginRight();
                case COLUMN -> anchorY += child.getHeight() + child.getMarginTop() + child.getMarginBottom();
            }
        }
    }

    protected void recomputeInternalDimensions(RecomputeMeasurementsContext context) {
        float w = width;
        float h = height;

        for (UIElement child : children) {
            float marginWidth = child.getMarginLeft() + child.getMarginRight();
            float marginHeight = child.getMarginTop() + child.getMarginBottom();

            switch (childrenAlignment) {
                case ROW:
                    w += child.getWidth() + marginWidth;
                    h = Math.max(h, child.getHeight() + marginHeight);
                    break;
                case COLUMN:
                    w = Math.max(w, child.getWidth() + marginWidth);
                    h += child.getHeight() + marginHeight;
                    break;
            }
        }

        w += padLeft + padRight;
        h += padTop + padBottom;

        if (minWidth > 0) w = Math.max(w, minWidth);
        if (maxWidth > 0) w = Math.min(w, maxWidth);

        if (minHeight > 0) h = Math.max(h, minHeight);
        if (maxHeight > 0) h = Math.min(h, maxHeight);

        width = w;
        height = h;
    }

    protected void recomputeOuterDimensions(RecomputeLayoutContext context) {
        float w = getWidth();
        float h = getHeight();

        float growW = context.availableWidth() * growX;
        float growH = context.availableHeight() * growY;

        w = Math.max(w, growW);
        h = Math.max(h, growH);

        if (!overflowX) w = Math.min(w, context.availableWidth());
        if (!overflowY) h = Math.min(h, context.availableHeight());

        if (minWidth > 0) w = Math.max(w, minWidth);
        if (maxWidth > 0) w = Math.min(w, maxWidth);

        if (minHeight > 0) h = Math.max(h, minHeight);
        if (maxHeight > 0) h = Math.min(h, maxHeight);

        width = w;
        height = h;
    }

    protected void recomputePosition(RecomputeLayoutContext context) {
        float x = marginLeft;
        float y = marginTop;

        var align = recomputeAlignment(context);
        float alignX = align.first();
        float alignY = align.second();

        relativeX = context.anchorX() + x + alignX;
        relativeY = context.anchorY() + y + alignY;
    }

    protected Pair<Float, Float> recomputeAlignment(RecomputeLayoutContext context) {
        float freeW = Math.max(0, context.availableWidth() - width);
        float freeH = Math.max(0, context.availableHeight() - height);

        float alignX = 0f;
        float alignY = 0f;

        if (Alignment.has(alignment, Alignment.CENTER_X))
            alignX = freeW / 2f;
        else if (Alignment.has(alignment, Alignment.RIGHT))
            alignX = freeW;
        else alignX = 0;

        if (Alignment.has(alignment, Alignment.CENTER_Y))
            alignY = freeH / 2f;
        else if (Alignment.has(alignment, Alignment.BOTTOM))
            alignY = freeH;
        else alignY = 0;

        return new Pair<>(alignX, alignY);
    }

    @SuppressWarnings("unchecked")
    protected final T self() {
        return (T) this;
    }

    @Override
    public List<UIElement> getChildren() {
        return Collections.unmodifiableList(children);
    }
}