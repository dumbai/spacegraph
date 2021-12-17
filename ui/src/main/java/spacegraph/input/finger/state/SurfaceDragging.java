package spacegraph.input.finger.state;

import spacegraph.input.finger.Finger;
import spacegraph.space2d.Surface;

/** dragging initiated within the bounds of a surface */
public abstract class SurfaceDragging extends Dragging {

    private final Surface s;

    public SurfaceDragging(Surface s, int button) {
        super(button);
        this.s = s;
    }

    @Override
    protected boolean starting(Finger f) {
        return f.intersects(s.bounds);
    }

    @Override
    public Surface touchNext(Surface prev, Surface next) {
        return s;
    }
}
