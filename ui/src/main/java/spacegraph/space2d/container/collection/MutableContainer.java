package spacegraph.space2d.container.collection;

import spacegraph.space2d.Surface;
import spacegraph.space2d.container.ContainerSurface;

public abstract class MutableContainer extends ContainerSurface {


    public final boolean remove(Surface s) {
        return _remove(s) && s.stop();
    }

    protected abstract boolean _remove(Surface s);

    public final void addAll(Surface... s) {
        add(s);
    }

    public abstract void add(Surface... s);


    protected abstract MutableContainer clear();
}