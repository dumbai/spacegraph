package spacegraph.space2d.container.collection;

import jcog.Util;
import jcog.data.list.FastCoWList;
import jcog.util.ArrayUtil;
import org.eclipse.collections.api.set.primitive.IntSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.ContainerSurface;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public abstract class MutableListContainer extends MutableContainer {


    private static final IntFunction<Surface[]> NEW_SURFACE_ARRAY = (i) -> i == 0 ? Surface.EmptySurfaceArray : new Surface[i];
    private final FastCoWList<Surface> children;

    public MutableListContainer(Surface... children) {
        super();
        this.children = new FastCoWList(NEW_SURFACE_ARRAY) {
            @Override
            public void commit() {
                super.commit();
                layout();
            }
        };

        if (children.length > 0)
            set(children);
    }

    public Surface[] children() {
        return children.array();
    }

    @Override
    public int childrenCount() {
        return children.size();
    }


    public Surface get(int index) {
        return children.get(index);
    }

    public Surface remove(int index) {
        return children.remove(index);
    }

//    @Nullable
//    public Surface getSafe(int index) {
//        @Nullable Surface[] c = children.copy;
//        if (index >= 0 && index < c.length)
//            return c[index];
//        else
//            return null;
//    }

//    /**
//     * returns the existing value that was replaced
//     */
//    protected Surface set(int index, Surface next) {
//        synchronized (children.list) {
//            Surfacelike p = this.parent;
//            if (p == null) {
//                return children.set(index, next);
//            } else {
//                if (children.size() - 1 < index)
//                    throw new RuntimeException("index out of bounds");
//
//                Surface existing = children.set(index, next);
//                if (existing != next) {
//                    if (existing != null)
//                        existing.delete();
//
//                    next.start(this);
//                }
//                return existing;
//            }
//        }
//    }

    public void add(Surface s) {
        if (children.add(s))
            layout();
    }

    public void add(Surface... s) {
        if (children.addAll(s))
            layout();
    }

    public void addAll(Collection<Surface> s) {
        children.addAll(s);
        layout();
    }

    @Override
    protected boolean _remove(Surface s) {
        return children.removeFirstInstance(s);
    }

    protected boolean detachChild(int i) {
        children.remove(i);
        return true;
    }

    public final ContainerSurface set(Surface... next) {
        if (next.length == 0) {
            clear();
        } else {

            synchronized (children.list) { //HACK do better

                if (parent == null) {
                    children.setDirect(next);
                    return this;
                } else {

                    Surface[] ee = children.array();

                    int numExisting = ee.length;
                    if (numExisting == 0) {


                        addAll(next);

                    } else {


                        if (!ArrayUtil.equalsIdentity(ee, next)) {
                            IntSet pi = Util.intSet(x -> x.id, ee);
                            IntSet ni = Util.intSet(x -> x.id, next);
                            IntHashSet unchanged = new IntHashSet(ee.length + next.length).withAll(pi.select(ni::contains));

//                    Sets.SetView unchanged = Sets.intersection(
//                            Set.of(cc), Set.of(next)
//                    );
                            if (unchanged.isEmpty()) unchanged = null;

                            int ei = 0;
                            for (Surface e : ee) {
                                if (unchanged == null || !unchanged.contains(e.id)) {
                                    e.stop();
                                    detachChild(ei);
                                }

                                ei++;
                            }

                            for (Surface n : next) {
                                if (unchanged == null || !unchanged.contains(n.id))
                                    add(n);
                            }
                        }

                    }
                }

            }
        }

        return this;
    }

    public final ContainerSurface set(Collection<? extends Surface> next) {
        //set(next.toArray(Surface.EmptySurfaceArray));
        children.set(next);
        layout();
        return this;
    }

    @Override
    public void forEach(Consumer<Surface> o) {
        children.forEachNonNull(o);
    }

    @Override
    public <X> void forEachWith(BiConsumer<Surface, X> o, X x) {
        children.forEachWith(o, x);
    }

    @Override
    public boolean whileEach(Predicate<Surface> o) {
        return children.whileEach(o);
    }

    @Override
    public boolean whileEachReverse(Predicate<Surface> o) {
        return children.whileEachReverse(o);
    }

    public int size() {
        return children.size();
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public MutableListContainer clear() {
        synchronized (children.list) {
            if (parent == null) {
                children.clear();
            } else {
                if (!children.isEmpty()) {
                    for (Surface child : children) {
                        child.stop();
                    }
                    children.clear();
                }
            }
        }
        return this;
    }


    @Override
    protected abstract void doLayout(float dtS);
}