package spacegraph.space2d.widget.port;

import com.jogamp.opengl.GL2;
import jcog.data.graph.Node;
import jcog.data.graph.path.FromTo;
import jcog.tree.rtree.rect.RectF;
import org.eclipse.collections.api.block.procedure.primitive.FloatObjectProcedure;
import spacegraph.input.finger.Finger;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.SurfaceGraph;
import spacegraph.space2d.container.graph.GraphEdit2D;
import spacegraph.space2d.widget.Widget;
import spacegraph.space2d.widget.port.util.Wiring;
import spacegraph.video.Draw;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * base class for a port implementation
 *
 * @see http:
 */
public class Port<X> extends Widget implements Wiring.Wireable {
    private FloatObjectProcedure<Port<X>> updater;

    private transient Node<Surface, Wire> node;
    public transient volatile Wiring beingWiredOut;
    public transient volatile Wiring beingWiredIn;
    private boolean enabled = true;

    /**
     * input handler
     */
    In<? super X> in;

//    /**
//     * prototype (example) builder.  stipulates a protocol as specified by an example instance
//     */
//    private Supplier specifyHow = null;

//    /**
//     * prototype (example) acceptor. accepts a protocol (on connect / re-connect)
//     */
//    private final Consumer obeyHow = null;



    private static final int WIRING_BUTTON = 2;


    public Port() {
        super();
    }


    Port(In<? super X> i) {
        this();
        on(i);
    }

    /**
     * for convenience
     */
    Port(Consumer<? super X> i) {
        this();
        on(i);
    }

    public static boolean canConnect(Port a, Port b) {
        //synchronized (this) {
        return Port.canConnect(b) && Port.canConnect(a);
        //}
    }


    public Port<X> on(Consumer<? super X> i) {
        return on((w, x) -> i.accept(x));
    }

    public Port<X> on(Runnable i) {
        return on((w, x) -> i.run());
    }

//    public Port<X> specify(Supplier<X> proto) {
//        this.specifyHow = proto;
//        return this;
//    }

//    public Port<X> obey(Consumer<? super X> withRecievedProto) {
//        this.obeyHow = withRecievedProto;
//        return this;
//    }

    /**
     * set the input handler
     */
    Port<X> on(@Nullable In<? super X> i) {
        this.in = i;
        return this;
    }

    public Port<X> update(@Nullable Runnable update) {
        this.updater = (i, p) -> update.run();
        return this;
    }

    public Port<X> update(@Nullable Consumer<Port<X>> update) {
        this.updater = (i, p) -> update.accept(p);
        return this;
    }

    public Port<X> update(@Nullable FloatObjectProcedure<Port<X>> update) {
        this.updater = update;
        return this;
    }

    public boolean enabled() {
        return enabled;
    }

    private static boolean canConnect(Port other) {
//        if (other.specifyHow != null) {
//
//            if (specifyHow != null) {
//
//                return specifyHow.get().equals(other.specifyHow.get());
//            }
//
//            if (obeyHow != null) {
//
//                obeyHow.accept(other.specifyHow.get());
//            }
//        }

        return true;
    }

    /* override in subclasses to implement behavior to be executed after wire connection has been established in the graph. */
    void connected(Port a) {

    }
    void disconnected(Port a) {

    }

    @FunctionalInterface
    public interface In<T> {

        /**
         * TODO more informative backpressure-determining state
         * TODO pluggable receive procedure:
         * local buffers (ex: QueueLock), synch, threadpool, etc
         */

//        /** test before typed wire connection */
//        default boolean canAccept(T proto) {
//            return true;
//        }

        void accept(Wire from, T t);


    }


    @Override
    protected void paintWidget(RectF bounds, GL2 gl) {

        if (beingWiredOut != null) {
            gl.glColor4f(0.5f, 1, 0, 0.35f);
        } else if (beingWiredIn != null) {
            gl.glColor4f(0, 0.5f, 1, 0.35f);
        } else {
            gl.glColor4f(0.0f, 0.0f, 0.0f, 0.5f);
        }

        Draw.rect(bounds, gl);
    }


    @Override
    public Surface finger(Finger f) {

        Surface x = super.finger(f);
        if (x == null || x == this) {
            if (f.test(new Wiring(WIRING_BUTTON, this)))
                return this;
        }

        beingWiredIn = beingWiredOut = null;

        return x;

    }


    private static boolean acceptWiring(Wiring w) {
        return true;
    }

    @Override
    public boolean onWireIn(@Nullable Wiring w, boolean preOrPost) {
        if (preOrPost && !acceptWiring(w)) {
            this.beingWiredIn = null;
            return false;
        }
        this.beingWiredIn = preOrPost ? w : null;
        return true;
    }


    @Override
    public void onWireOut(@Nullable Wiring w, boolean preOrPost) {
        this.beingWiredOut = preOrPost ? w : null;
        if (!preOrPost) {
            onWired(w);
        }
    }

    /**
     * wiring complete
     */
    protected void onWired(Wiring w) {

    }


    @Override
    public boolean canRender(ReSurface r) {
        if (super.canRender(r)) {
            FloatObjectProcedure<Port<X>> u = this.updater;
            if (u != null)
                u.value(r.frameDT, this);

            return true;
        }
        return false;
    }

    public boolean out(X x) {
        return out(this, x);
    }

    public boolean outLazy(Supplier<X> x) {
        if (active()) {
            return this.out(x.get());
        }
        return false;
    }

    @Override
    protected void starting() {
        super.starting();

        GraphEdit2D graph = parentOrSelf(GraphEdit2D.class);
        if (graph != null)
            this.node = graph.links.addNode(this);
//        else
//            this.node = EditGraph2D.staticLinks.addNode(this); //HACK

        FloatObjectProcedure<Port<X>> u = this.updater;
        if (u != null)
            u.value(0, this);

    }

    @Override
    protected void stopping() {
        node = null;
        enabled = false;
        GraphEdit2D p = parentOrSelf(GraphEdit2D.class);
        if (p != null)
            p.links.removeNode(this);
        super.stopping();
    }

    final boolean out(Port<?> sender, X x) {
        if (enabled) {
            Node<Surface, Wire> n = this.node;
            if (n != null) {
                for (Iterator<FromTo<Node<Surface, Wire>, Wire>> iterator = n.edgeIterator(true, true); iterator.hasNext(); ) {
                    FromTo<Node<Surface, Wire>, Wire> t = iterator.next();
                    Wire wire = t.id();
                    Port recv = ((Port) wire.other(Port.this));
                    if (recv != sender) //1-level cycle block
                        wire.send(this, recv, x);
                }
                return true;
            }
        }
        return false;
    }

    /** returns true if sent */
    final boolean recv(Wire from, X s) {
        if (!enabled) {
            in.accept(null, s);
		} else {
            In<? super X> in = this.in;
            if (in != null) {
                try {
                    in.accept(from, s);
                    return true;
                } catch (RuntimeException t) {
                    SurfaceGraph r = root();
                    if (r != null)
                        r.error(this, 1.0f, t);
                    else
                        t.printStackTrace(); //TODO HACK
                    return false;
                }
            }
		}
		return false;

	}

    public void enable(boolean b) {
        this.enabled = b;
    }

    public boolean active() {
        return enabled && node != null && node.edgeCount(true, true) > 0;
    }

}