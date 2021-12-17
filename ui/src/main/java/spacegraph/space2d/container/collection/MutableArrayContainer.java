package spacegraph.space2d.container.collection;

import jcog.data.list.FastAtomicReferenceArray;
import spacegraph.space2d.Surface;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** TODO support resizing */
public abstract class MutableArrayContainer extends MutableContainer {

    /** TODO varhandle */
    protected final FastAtomicReferenceArray children;

    public MutableArrayContainer(int size) {
        this.children = new FastAtomicReferenceArray(size);
    }

    @SafeVarargs
    public MutableArrayContainer(Surface... items) {
        this(items.length);
        for (int i = 0, itemsLength = items.length; i < itemsLength; i++) {
            Surface s = items[i];
            if (s!=null)
                setAt(i, s);
        }
    }

    public final Surface get(int s) {
        return _get(s);
    }

    public final Surface remove(int index) {
        return setAt(index, null);
    }

    @Override
    protected void stopping() {
        super.stopping();
        int n = children.length();
        for (int i = 0; i < n; i++)
            children.set(i, null);
    }

    @Override
    protected boolean _remove(Surface s) {
        return children.nullifyInstance(s);
    }

    /** put semantics */
    public final Surface setAt(int index, Surface s) {
        if (s != setAt(index, s, true))
            layout();
        return s;
    }

    /** returns the removed element */
    private Surface setAt(int index, Surface next, boolean restart) {
//        return restart ?
//            children.getAndAccumulate(index, ss, this::updateRestart) :
//            children.getAndSet(index, ss);
        Surface prev = (Surface)children.getAndSet(index, next);
        if (restart && prev!=next) {
            if (prev != null) {
                //r.stop();
                prev.delete();
            }

            if (next != null) {
//                Surfacelike sParent = next.parent;
//                assert (sParent == null || sParent == this): this + " confused that " + next + " already has parent " + sParent;
//
//
////                        synchronized (this) {
                if (this.parent != null) {
                    next.start(this);
                }
                //otherwise it iSurface started, or this isnt started
//                        }

            }

        }
        return prev;
    }



    @Override
    protected MutableArrayContainer clear() {
        int length = children.length();
        for (int i= 0; i < length; i++)
            setAt(i, null);
        return this;
    }


    @Override
    public int childrenCount() {
        int result = 0;
        int length = children.length();
        for (int i = 0; i < length; i++) {
            if (_get(i) != null)
                result++;
        }
        return result;
    }

    @Override
    public void forEach(Consumer<Surface> o) {
        int length = children.length();
        for (int i = 0; i < length; i++) {
            Surface ii = _get(i);
            if (ii !=null) o.accept(ii);
        }
    }

    private Surface _get(int i) {
        return (Surface)children.getOpaque(i);
    }

    @Override
    public <X> void forEachWith(BiConsumer<Surface, X> o, X x) {
        int length = children.length();
        for (int i = 0; i < length; i++) {
            Surface ii = _get(i);
            if (ii !=null) o.accept(ii, x);
        }
    }

    @Override
    public boolean whileEach(Predicate<Surface> o) {
        int length = children.length();
        for (int i = 0; i < length; i++) {
            if (!test(o, i))
                return false;
        }
        return true;
    }

    @Override
    public boolean whileEachReverse(Predicate<Surface> o) {
        int length = children.length();
        for (int i = length - 1; i >= 0; i--) {
            if (!test(o, i))
                return false;
        }
        return true;
    }

    private boolean test(Predicate<Surface> o, int i) {
        Surface x = _get(i);
        return x == null || o.test(x);
    }

    @Override
    public void add(Surface... s) {
        throw new UnsupportedOperationException();
    }
}