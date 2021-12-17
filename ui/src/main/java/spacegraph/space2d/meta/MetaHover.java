package spacegraph.space2d.meta;

import org.eclipse.collections.api.block.function.primitive.BooleanToBooleanFunction;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Stacking;
import spacegraph.space2d.widget.Widget;

import java.util.function.Supplier;

public class MetaHover extends Stacking {


    private final Supplier<Surface> hover;
    private final BooleanToBooleanFunction hoverVisible;
    boolean hovering;

    /**
     *
     * @param x
     * @param visible the argument provided to the lambda is whether it was already visible
     */
    public MetaHover(Surface x, Supplier<Surface> hover, BooleanToBooleanFunction visible) {
        super();
        this.hover = hover;
        this.hoverVisible = visible;
        add(x);
    }

    @Override
    protected boolean canRender(ReSurface r) {
        if (super.canRender(r)) {
            boolean nextHovering = hoverVisible.valueOf(hovering);
            if (nextHovering!=hovering) {
                if (nextHovering) {
                    add(hover.get());
                } else {
                    remove(1);
                }
                hovering = nextHovering;
            }

            return true;
        }
        return false;
    }

    public MetaHover(Widget w, Supplier<Surface> hover) {
        this(w, hover, (currentlyVisible)-> w.pri() > (currentlyVisible ? 0.1f : 0.25f) );
    }
}