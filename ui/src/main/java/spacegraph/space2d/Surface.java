package spacegraph.space2d;

import jcog.Str;
import jcog.Util;
import jcog.math.v2;
import jcog.sort.FloatRank;
import jcog.sort.RankedN;
import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.input.finger.Finger;
import spacegraph.space2d.container.ContainerSurface;
import spacegraph.space2d.container.collection.MutableContainer;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.util.MutableRectFloat;

import java.io.PrintStream;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static jcog.tree.rtree.Spatialization.EPSILONf;

/**
 * planar subspace.
 * (fractal) 2D Surface embedded relative to a parent 2D surface or 3D space
 */
public abstract class Surface implements Surfacelike {


    public static final Surface[] EmptySurfaceArray = new Surface[0];
    public static final Supplier<Surface> TODO = () -> new VectorLabel("TODO");
    private static final VarHandle BOUNDS = Util.VAR(Surface.class, "bounds", RectF.class);
    private static final VarHandle PARENT = Util.VAR(Surface.class, "parent", Surfacelike.class);
    private static final AtomicInteger serial = new AtomicInteger();
    /**
     * serial id unique to each instanced surface
     */
    public final int id = serial.incrementAndGet();
    /**
     * whether content can be expected outside of the bounds, ex: in order to react to events
     */
    public boolean clipBounds = true;
    /**
     * scale can remain the unit 1 vector, normally
     */

    public volatile RectF bounds = RectF.Unit;
    public volatile Surfacelike parent;

    private volatile boolean visible = true;


//    public volatile int zIndex;

    public Surface() {

    }

    public Surface finger(Finger finger) {
        return null;
    }

    public final float cx() {
        return bounds.cx();
    }

    public final float cy() {
        return bounds.cy();
    }

    @Deprecated
    public final float x() {
        return left();
    }

    @Deprecated
    public final float y() {
        return bottom();
    }

    public final float left() {
        return bounds.left();
    }

    public final float bottom() {
        return bounds.bottom();
    }

    public final float right() {
        return bounds.right();
    }

    public final float top() { return bounds.top(); }

    /** request focus */
    public final <S extends Surface> S focus() {
        SurfaceGraph r = root();
        if (r!=null)
            r.keyFocus(this);
        else {
            System.err.println("detached from root for focus(): " + this);
            //TODO logger.warn("root not found")
        }


        return (S) this;
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public final int hashCode() {
        return id;
    }


    public <S extends Surface> S pos(MutableRectFloat next) {
        //TODO equality test?
        //return pos(next.immutable());
        //TODO force mutate if same instance, or use its internal versioning
        return pos((RectF)next);
    }

    public <S extends Surface> S pos(RectF next) {
        // if (next.area() < ScalarValue.EPSILON)
        //      throw new WTF();

        BOUNDS.set(this, next);

        //BOUNDS.accumulateAndGet(this, next, (prev, n) -> prev.equals(n) ? prev : n);

        return (S) this;
    }

    /**
     * override and return false to prevent movement
     */
    protected boolean posChanged(RectF next) {
        RectF last = (RectF) BOUNDS.getAndSet(this, next);
//        if (bounds.area() < ScalarValue.EPSILON)
//            throw new WTF();
        return last != next && !last.equals(next, EPSILONf);
    }
    protected boolean posChanged(MutableRectFloat next) {
        RectF last = (RectF) BOUNDS.get(this);
        if (!last.equals(next, EPSILONf)) {
            BOUNDS.set(this, next.immutable());
            return true;
        }
        return false;
    }

    public final Surface posXYWH(float cx, float cy, float w, float h) {
        return pos(RectF.XYWH(cx, cy, w, h));
    }

//    public AspectAlign align(AspectAlign.Align align) {
//        return new AspectAlign(this, 1.0f, align, 1.0f);
//    }

    public SurfaceGraph root() {
        return rootParent();
    }

    /**
     * default root() impl
     */
    public final SurfaceGraph rootParent() {
        Surfacelike parent = this.parent;
        return parent == null ? null : parent.root();
    }

    /**
     * finds the most immediate parent matching the class
     */
    public <S> S parentOrSelf(Class<S> s) {
        return (S) (s.isInstance(this) ? this : parent(s::isInstance, false));
    }

    /**
     * finds the most immediate parent matching the predicate
     */
    public Surfacelike parent(Predicate<Surfacelike> test, boolean includeSelf) {

        if (includeSelf && test.test(this))
            return this;

        Surfacelike p = this.parent;

        if (p instanceof Surface)
            return test.test(p) ? p : ((Surface) p).parent(test, true);
        else
            return null;
    }

    public final boolean start(Surfacelike parent) {
        assert (parent != null);

        Surfacelike p = (Surfacelike) PARENT.getAndSet(this, parent);
        if (p == parent)
            return false; //no change
        else {
            assert p == null;
            starting();
            return true;
        }
    }

    public final boolean stop() {
        Surfacelike p = (Surfacelike) PARENT.getAndSet(this, (Surfacelike) null);
        if (p != null) {
            hide();
            stopping();
            return true;
        }
        return false;
    }

    protected void starting() {
        //for implementing in subclasses
    }

    protected void stopping() {
        //for implementing in subclasses
    }

    public final float w() {
        return bounds.w;
    }

    public final float h() {
        return bounds.h;
    }

    public Surface pos(float x, float y) {
        pos(bounds.pos(x, y, EPSILONf));
        return this;
    }

    public Surface move(float dx, float dy) {
        pos(bounds.move(dx, dy, EPSILONf));
        return this;
    }

    public void print(PrintStream out, int indent) {
        out.print(Str.repeat("  ", indent));
        out.println(this);
    }

    /**
     * prepares the rendering procedures in the rendering context
     */
    public final void renderIfVisible(ReSurface r) {
        boolean visible = visible(r);
        showing(visible);
        if (visible)
            render(r);


//        else
//            System.out.println("invisible: " + r + " " + visible(r));
    }

    /** actual render implementation */
    protected abstract void render(ReSurface r);

    /**
     * test visibility in the current rendering context
     */
    public final boolean visible(ReSurface r) {
        RectF b = this.bounds;
        return visible() &&
               (b.w > Float.MIN_NORMAL && b.h > Float.MIN_NORMAL) &&
               (!clipBounds || r.visibleByCamera(b)) &&
               r.visibleByPixels(b);
    }



    public Surface hide() {
        visible = false;
        return this;
    }

    public final Surface show() {
        visible = true;
        return this;
    }

    public final Surface visible(boolean b) {
        return b ? show() : hide();
    }

    public final boolean visible() {
        return visible && parent != null;
    }

    public boolean showing() {
        return visible;
    }

    public float radius() {
        return bounds.radius();
    }


    /**
     * detach from parent, if possible
     * TODO common remove(x) interface
     */
    public boolean delete() {


        Surfacelike p = (Surfacelike) PARENT.getAndSet(this, (Surfacelike)null);
        if (p!=null) {

//            if (p instanceof MutableUnitContainer) {
//                ((MutableUnitContainer) p).set(null);
//            }
            if (p instanceof MutableContainer) {
                ((MutableContainer) p).remove(this);
            }

            stop();

            if (this instanceof ContainerSurface) {
                ((ContainerSurface) this).forEach(Surface::delete);
            }

            return true;
        }

        return false;
    }

//    public boolean reattach(Surface nextParent) {
//        if (this == nextParent)
//            return true;
//
//        Surfacelike prevParent = this.parent;
//        if (prevParent instanceof AbstractMutableContainer && nextParent instanceof AbstractMutableContainer) {
//            AbstractMutableContainer prevMutableParent = (AbstractMutableContainer) prevParent;
//            AbstractMutableContainer nextMutableParent = (AbstractMutableContainer) nextParent;
//            if (PARENT.compareAndSet(this, prevParent, nextParent)) {
//                if (prevMutableParent.detachChild(this)) {
//
//                    //now it is at risk of being lost
//
//                    if (nextMutableParent.attachChild(this)) {
//                        return true;
//                    } else {
//                        //could not attach to nextParent, reattach to prev
//                        if (!prevMutableParent.attachChild(this)) {
//                            //recovered
//                        }
//                    }
//
//                }
//                System.err.println("lost: " + this + " while reattaching from " + prevParent + " to " + nextParent);
//                //TODO logger.warn(...
//                stop();
//            }
//        }
//
//        return false;
//    }


    public boolean exist() {
        //TODO optimize with boolean flag
        return rootParent() != null;
    }

    public v2 pos() {
        return new v2(x(), y());
    }


    public final Surface resize(float w, float h) {
        return pos(bounds.size(w, h));
    }

//    public final boolean resizeIfChanged(float w, float h) {
//        return posChanged(bounds.size(w, h));
//    }

    /** called before rendering */
    public void showing(boolean s) {

    }

    @Nullable
    public RankedN<Surface> rank(FloatRank<Surface> o, int n) {
        return null;
    }
}