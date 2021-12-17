package spacegraph.space2d.container.layout;

import jcog.data.list.Lst;
import spacegraph.space2d.container.graph.Graph2D;
import spacegraph.util.MutableRectFloat;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DynamicLayout2D<X> implements Graph2D.Graph2DUpdater<X> {

    protected final Lst<MutableRectFloat<X>> nodes = new Lst<>(new MutableRectFloat[0]);

    final AtomicBoolean getBusy = new AtomicBoolean(false), layoutBusy = new AtomicBoolean(false);

    @Override
    public void update(Graph2D<X> g, float dtS) {
        if (getBusy.compareAndSet(false, true)) {
            try {
                if (!get(g))
                    return;
            } finally {
                getBusy.set(false);
            }
        }

        if (layoutBusy.compareAndSet(false, true)) {
            try {
                layout(g, dtS);
            } finally {
                layoutBusy.set(false);
            }
        }
    }

    protected abstract void layout(Graph2D<X> g, float dtS);

    private boolean get(Graph2D<X> g) {
        nodes.clear();
        g.forEachValue(v -> {
            if (v.visible() && !v.pinned()) nodes.add(v.m);
        });

        return !nodes.isEmpty();
    }

}
