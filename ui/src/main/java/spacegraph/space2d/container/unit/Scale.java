package spacegraph.space2d.container.unit;

import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.Surface;

public class Scale extends UnitContainer {

    private float scale;

    public Scale(Surface the, float s) {
        super(the);
        scale(s);
    }

    private Scale scale(float scale) {
        this.scale = scale;
        return this;
    }

    public float scale() {
        return scale;
    }

    @Override
    protected RectF innerBounds() {
        return bounds.scale(scale);
    }

}
