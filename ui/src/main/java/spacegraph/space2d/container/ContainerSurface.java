package spacegraph.space2d.container;

import com.jogamp.opengl.GL2;
import jcog.Str;
import jcog.Util;
import jcog.math.v2;
import jcog.sort.FloatRank;
import jcog.sort.RankedN;
import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.input.finger.Finger;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;

import java.io.PrintStream;
import java.lang.invoke.VarHandle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * branch node.  layout is an asynchronous lazy update.
 * TODO illustrate the various update loops and their interactions
 */
public abstract class ContainerSurface extends Surface {

    private static final VarHandle MUSTLAYOUT = Util.VAR(ContainerSurface.class, "mustLayout", int.class);

    @SuppressWarnings({"FieldMayBeFinal", "unused"})
    private volatile int mustLayout;


    @Override
    public @Nullable RankedN<Surface> rank(FloatRank<Surface> o, int n) {
        RankedN<Surface> r = new RankedN<>(new Surface[n], o);
        forEachRecursively(r);
        return r.isEmpty() ? null : r;
    }

    /** TODO just accept the ReRender instance, not this dtS which it can get as needed */
    @Deprecated
    protected abstract void doLayout(float dtS);

    @Override
    public void print(PrintStream out, int indent) {
        super.print(out, indent);

        forEach(c -> {
            out.print(Str.repeat("  ", indent + 1));
            c.print(out, indent + 1);
        });
    }

    public final <S extends Surface> S pos(v2 p) {
        pos(p.x, p.y);
        return (S) this;
    }

    @Override
    public final <S extends Surface> S pos(RectF r) {
        if (posChanged(r))
            layout();
        return (S) this;
    }

    /**
     * first sub-layer
     */
    @Deprecated
    protected void paintIt(GL2 gl, ReSurface r) {

    }

    @Override
    protected final void render(ReSurface r) {
        if (canRender(r)) {
            //TODO all of these called methods can be merged into one method that does these in whatever order the impl chooses

            if (MUSTLAYOUT.compareAndSet(this, 1, 0)) {
                ensureChildrenStarted();
                doLayout(r.frameDT);
            }

            //TODO if transparent ("non-opaque" in Swing terminology) this doesnt need rendered
            paintIt(r.gl, r);

            renderContent(r);
        }
    }

    private void ensureChildrenStarted() {
        forEach(c -> {
            if (c.parent == null)
                c.start(this);
        });
    }

    protected static final VarHandle SHOWING = Util.VAR(ContainerSurface.class, "showing", boolean.class);
    private volatile boolean showing;

    @Override
    public final void showing(boolean s) {
        if ((boolean)SHOWING.getAndSet(this, s)!=s) {
            if (!s)
                forEach(c -> c.showing(false));
        }
    }

    @Override public final boolean showing() {
        return (boolean) SHOWING.getOpaque(this);
    }

    @Override
    public Surface hide() {
        super.hide();
        showing(false);
        return this;
    }

    @Deprecated protected void renderContent(ReSurface r) {
        forEachWith(Surface::renderIfVisible, r);
    }

    /** post-visibility render guard */
    protected boolean canRender(ReSurface r) {
        return true;
    }

    public Surface finger(Finger finger) {

        if (showing() && childrenCount() > 0 && (!clipBounds || finger.intersects(bounds))) {
            FingerFirst ff = new FingerFirst(finger);
            if (!whileEachReverse(ff))
                return ff.found;
        }

        return null;
    }

    public abstract int childrenCount();


    @Override
    protected void starting() {
        ensureChildrenStarted();
        doLayout(0);
        //layout();
    }

    @Override
    protected void stopping() {
        forEach(Surface::stop);
    }

    /** TODO forEachWith */
    public abstract void forEach(Consumer<Surface> o);

    public <X> void forEachWith(BiConsumer<Surface,X> o, X x) {
        forEach(c -> o.accept(c, x));
    }

//    public final void forEachOrphan(Consumer<Surface> S) {
//        forEachWith((c,s) -> {
//            if (c.parent == null)
//                s.accept(c);
//        }, S);
//    }

    public void forEachRecursively(Consumer<Surface> O) {

        O.accept(this);

        forEachWith((z,o) -> {
            if (z instanceof ContainerSurface)
                ((ContainerSurface) z).forEachRecursively(o);
            else
                o.accept(z);
        }, O);

    }

    public abstract boolean whileEach(Predicate<Surface> o);

    /** TODO make whileNullReverse(UnaryOperator<Surface> o) */
    public abstract boolean whileEachReverse(Predicate<Surface> o);

    /** default implementation */
    public <X extends Surface> X first(Class<? extends X> zoomedClass) {
        Surface[] found = {null};
        whileEach(s -> {
            if (zoomedClass.isInstance(s)) {
                found[0] = s;
                return false;
            }
            return true; //keep going
        });
        return (X) found[0];
    }

    public final void layout() {
        MUSTLAYOUT.setOpaque(this, 1);
    }

//    public final boolean layoutPending() {
//        return MUSTLAYOUT.getOpaque(this)>0;
//    }
    private static final class FingerFirst implements Predicate<Surface> {
        private final Finger finger;
        public Surface found;

        FingerFirst(Finger finger) {
            this.finger = finger;
        }

        @Override
        public boolean test(Surface c) {
            Surface s = c.finger(finger);
            if (s != null) {
                found = s;
                return false;
            } else
                return true;
        }
    }
}