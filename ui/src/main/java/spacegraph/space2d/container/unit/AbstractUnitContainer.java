package spacegraph.space2d.container.unit;

import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.ContainerSurface;

import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractUnitContainer<S extends Surface> extends ContainerSurface {

    protected abstract @Nullable S the();


    /** default behavior: inherit bounds directly
     * @param dtS*/
    @Override
    @Deprecated protected void doLayout(float dtS) {
        S t = the();
        if (t!=null)
            t.pos(innerBounds());
    }

    protected RectF innerBounds() {
        return bounds;
    }


    @Override
    public final int childrenCount() {
        S t = the();
        return t!=null ? 1 : 0;
    }

    @Override
    public final void forEach(Consumer<Surface> o) {
        S t = the();
        if (t!=null)
            o.accept(t);
    }

    @Override
    public boolean whileEach(Predicate<Surface> o) {
//        S content = the();
//        if (content instanceof ContainerSurface)  return ((ContainerSurface)content).whileEach(o);
//        else return o.test(content);
        S t = the();
        return t == null || o.test(t);
    }

    @Override
    public boolean whileEachReverse(Predicate<Surface> o) {
//        S content = the();
//        if (content instanceof Container)  return ((Container)content).whileEachReverse(o);
//        else return o.test(content);
        S t = the();
        return t == null || o.test(t);
    }


}
