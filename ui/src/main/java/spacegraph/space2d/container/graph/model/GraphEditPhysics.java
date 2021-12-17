package spacegraph.space2d.container.graph.model;

import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.EmptySurface;
import spacegraph.space2d.container.graph.GraphEdit2D;
import spacegraph.space2d.container.graph.Link;
import spacegraph.space2d.widget.port.Wire;

/** model for physics-based management of EditGraph2D spaces */
public abstract class GraphEditPhysics {

    protected GraphEdit2D graph;

    public transient Surface below = new EmptySurface();
    public transient Surface above = new EmptySurface();


    public abstract Object add(Surface w);

    public abstract void remove(Surface w);

    public final void start(GraphEdit2D parent) {
        starting(this.graph = parent);
    }

    /** may construct surfaceBelow and surfaceAbove in implementations */
    protected abstract void starting(GraphEdit2D graph);

    public abstract void stop();

    public abstract Link link(Wire w);

    /** queues procedures synchronously in the physics model's private sequential queue */
    public abstract void invokeLater(Runnable o);

    public abstract void update(GraphEdit2D g, float dt);

    public abstract void pos(Surface s, RectF pos);
}