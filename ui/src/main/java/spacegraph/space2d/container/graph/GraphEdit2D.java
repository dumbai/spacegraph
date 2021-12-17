package spacegraph.space2d.container.graph;

import com.google.common.base.Joiner;
import com.jogamp.opengl.GL2;
import jcog.data.graph.MapNodeGraph;
import jcog.data.graph.Node;
import jcog.data.graph.edge.ImmutableDirectedEdge;
import jcog.data.graph.path.FromTo;
import jcog.event.Off;
import jcog.math.v2;
import jcog.tree.rtree.rect.RectF;
import spacegraph.SpaceGraph;
import spacegraph.input.finger.Finger;
import spacegraph.input.finger.state.DoubleClicking;
import spacegraph.layer.OrthoSurfaceGraph;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.ContainerSurface;
import spacegraph.space2d.container.PaintSurface;
import spacegraph.space2d.container.Stacking;
import spacegraph.space2d.container.collection.MutableMapContainer;
import spacegraph.space2d.container.graph.model.GraphEditPhysics;
import spacegraph.space2d.container.graph.model.VerletGraphEditPhysics;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.container.unit.Animating;
import spacegraph.space2d.container.unit.Scale;
import spacegraph.space2d.meta.MetaFrame;
import spacegraph.space2d.meta.ProtoWidget;
import spacegraph.space2d.meta.WeakSurface;
import spacegraph.space2d.meta.WizardFrame;
import spacegraph.space2d.widget.port.Wire;
import spacegraph.space2d.widget.port.util.Wiring;
import spacegraph.space2d.widget.text.AbstractLabel;
import spacegraph.space2d.widget.text.BitmapLabel;
import spacegraph.space2d.widget.windo.DependentWindow;
import spacegraph.space2d.widget.windo.Windo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * wall which organizes its sub-surfaces according to 2D phys dynamics
 * TODO unify this with Graph2D
 * TODO remove all synchronized(links) with appropriate reliance on its ConcurrentHashMap and any additional per-node/per-edge locking
 */
public class GraphEdit2D extends MutableMapContainer<Surface, ContainerSurface> {

    public final GraphEditPhysics physics;

    /**
     * TODO use more efficient graph representation
     * TODO encapsulate so its private
     */
    public final MapNodeGraph<Surface, Wire> links = new LinkGraph();

//    /**
//     * practically invisibile epsilon TODO is this absolute or relative
//     */
//    private final v2 windoSizeMinRel = new v2(0.002f, 0.002f);


    /**
     * for links and other supporting geometry that is self-managed
     * TODO encapsulate so its private
     */
    private final Stacking raw = new Stacking();

    private final DoubleClicking doubleClicking = new DoubleClicking(0, this::doubleClick, this);

    private transient Off loop;

    public GraphEdit2D() {
        this(new VerletGraphEditPhysics());
    }

    public GraphEdit2D(GraphEditPhysics physics) {
        super();
        this.physics = physics;
        clipBounds = false; //TODO only if fencing
    }

    public static GraphEdit2D graphWindow(int w, int h) {
        GraphEdit2D g = new GraphEdit2D();
        OrthoSurfaceGraph win = SpaceGraph.window(g, w, h);
        //win.dev();
        return g;
    }

    @Override
    protected void starting() {

        physics.start(this);

        assert (loop == null);
        loop = root().animate((float dt) -> this.physics.update(GraphEdit2D.this, dt));

        super.starting();
    }

    @Override
    protected final void stopping() {
        super.stopping();

        loop.close();
        loop = null;

        links.clear();

        physics.stop();
    }

    public final Windo add(Surface x) {
        return add(x, xx ->
                new DependentWindow(new Scale(new MetaFrame(xx), 0.98f)));
    }

    public final Windo addUndecorated(Surface x) {
        return add(x, DependentWindow::new);
    }

    public final Windo addWeak(Surface x) {
        return add(x, xx -> new DependentWindow(new MyWeakSurface(xx)));
    }


    @Override
    public void doLayout(float dtS) {
        RectF b = this.bounds;
        physics.below.pos(b);
        physics.above.pos(b);
        raw.pos(b);

//        forEach(w -> {
//            if (w.parent == null)
//                w.start(GraphEdit2D.this);
//
////            if (fenceInside)
////                w.pos(w.bounds.fenceInside(graphBounds));
//        });
    }

    @Override
    public ContainerSurface remove(Object key) {
        ContainerSurface w = super.remove(key);
        if (w != null) {
            w.delete(); //stop();
            physics.remove(w);
            return w;
        }
        return null;
    }


    /**
     * uses put() semantics
     */
    public final Windo add(Surface x, Function<Surface, ContainerSurface> windowize) {
        return (Windo) computeIfAbsent(x, (xx) -> {
            ContainerSurface ww = windowize.apply(xx);
            ww.start(this);
            physics.add(ww);
            return ww;
        }).value;
    }

    public Animating<Debugger> debugger() {
        Debugger d = new Debugger();
        return new Animating<>(d, d::update, 0.25f);
    }

    public final void addRaw(Surface s) {
        raw.add(s);
    }

    @Override
    public final void forEach(Consumer<Surface> each) {
        whileEach(x -> {
            each.accept(x);
            return true;
        });
    }

    @Override
    public boolean whileEach(Predicate<Surface> o) {
        return o.test(physics.below) && o.test(raw) && super.whileEach(o) && o.test(physics.above);
    }

    @Override
    public boolean whileEachReverse(Predicate<Surface> o) {
        return o.test(physics.above) && super.whileEach(o) && o.test(raw) && o.test(physics.below);
    }

    public Iterable<FromTo<Node<Surface, Wire>, Wire>> edges(Surface s) {
        Node<Surface, Wire> n = links.node(s);
        return n != null ? n.edges(true, true) : Collections.EMPTY_LIST;
    }

    @Override
    public final Surface finger(Finger finger) {

        Surface s = super.finger(finger);

        if (s == null) {
            if (doubleClicking.update(finger))
                return this;
        } else {
            doubleClicking.reset();
        }

        return s;
    }

    private void doubleClick(v2 pos) {

        Windo z = add(
                new WizardFrame(new ProtoWidget())
//                    @Override
//                    protected void become(Surface next) {
//                        super.become(next);
//
//                        //GraphEdit pp = parent(GraphEdit.class);
////                        if (next instanceof ProtoWidget) {
////                            pp.setCollidable(false);
////                        } else {
////                            pp.setCollidable(true);
////                        }
//
//                    }
                //}
        );
        z.posRel(pos.x, pos.y, 0.1f, 0.1f);
        //z.pos(RectFloat.XYWH(pos.x, pos.y, 0.1f * w(), 0.1f * h()));
        //((SpaceGraphFlat)(z.root())).zoomNext(z);
    }

    /**
     * undirected link
     */
    public boolean addWire(Wire wire) {

        Surface A = wire.a, B = wire.b;
        synchronized (links) {
            if (!links.addEdgeByNode(links.addNode(A), wire, links.addNode(B)))
                return false; //already exists
        }

        wire.connected();

        physics.invokeLater(() -> {
            physics.link(wire);

            Wiring w = null; //HACK
            if (A instanceof Wiring.Wireable) {
                ((Wiring.Wireable) A).onWireOut(w, false);
                ((Wiring.Wireable) A).onWireIn(w, false);
            }
            if (B instanceof Wiring.Wireable) {
                ((Wiring.Wireable) B).onWireOut(w, false);
                ((Wiring.Wireable) B).onWireIn(w, false);
            }

        });

        return true;
    }

    public void removeComponent(Surface s) {
        synchronized (links) {
            links.removeNode(s);
        }
    }

    //protected Wire removeWire(Surface source, Surface target) {
    boolean removeWire(Wire wire) {

        wire.preRemove();

        boolean removed;
        synchronized (links) {
            //Wire wire = new Wire(source, target);
            Node<Surface, Wire> an = links.node(wire.a);
            if (an != null) {
                Node<Surface, Wire> bn = links.node(wire.b);
                removed = bn != null && links.edgeRemove(new ImmutableDirectedEdge<>(an, wire, bn));
            } else
                removed = false;
        }

        if (removed) {
            wire.remove();
            //TODO log( unwire(..) )
            return true;
        }

        return false;
    }

//    public void removeRaw(Surface x) {
//        raw.remove(x);
//    }

//    public Windo sprout(S from, S toAdd, float scale) {
//        Windo to = addAt(toAdd);
//        to.pos(RectFloat.XYWH(from.cx(), from.cy(), from.w() * scale, from.h() * scale));
//
//        VerletParticle2D toParticle = physics.bind(to, VerletSurface.VerletSurfaceBinding.Center, false);
//        toParticle.addBehaviorGlobal(new AttractionBehavior2D<>(toParticle, 100 /* TODO auto radius*/, -20));
//
//        VerletParticle2D fromParticle = physics.bind(from, VerletSurface.VerletSurfaceBinding.NearestSurfaceEdge);
//
//
//        physics.physics.addSpring(new VerletSpring2D(fromParticle, toParticle, 10, 0.5f));
//
////        cable(from, fromParticle, to, toParticle);
//
//        return to;
//    }

    private static class LinkGraph extends MapNodeGraph<Surface, Wire> {

        LinkGraph() {
            super(new ConcurrentHashMap<>());
        }

        @Override
        protected void onRemoved(Node<Surface, Wire> r) {
            for (FromTo<Node<Surface, Wire>, Wire> e : r.edges(true, true))
                e.id().remove();
        }

    }

    public static class VisibleLink extends Link {

        public VisibleLink(Wire wire) {
            super(wire);
        }

        public Surface a() {
            return VisibleLink.this.id.a;
        }

        public Surface b() {
            return VisibleLink.this.id.b;
        }

        protected abstract class VisibleLinkSurface extends PaintSurface {

            protected abstract void paintLink(GL2 gl, ReSurface reSurface);

            @Override
            protected final void paint(GL2 gl, ReSurface reSurface) {
                if (a().parent == null || b().parent == null) {
                    GraphEdit2D graphParent = parentOrSelf(GraphEdit2D.class);
                    if (graphParent != null)
                        VisibleLink.this.remove(graphParent);

                    delete();
                }
                paintLink(gl, reSurface);
            }
        }
    }

    private static class MyWeakSurface extends WeakSurface {
        MyWeakSurface(Surface xx) {
            super(xx);
        }

        @Override
        public boolean delete() {
            if (!super.delete())
                return false;

            DependentWindow w = parentOrSelf(DependentWindow.class);
            if (w != null)
                w.delete();
            return true;
        }
    }

    class Debugger extends Gridding {

        private final AbstractLabel boundsInfo;
        private final AbstractLabel children;

        {
            add(boundsInfo = new BitmapLabel());
            add(children = new BitmapLabel());
        }

        void update() {
            boundsInfo.text(GraphEdit2D.this.bounds.toString());

            List<String> list = GraphEdit2D.this.keySet().stream().map(t -> info(t, getValue(t))).collect(Collectors.toList());
            children.text(Joiner.on("\n").join(list));
        }

        private String info(Surface x, ContainerSurface w) {
            return x + "\n  " + (w != null ? w.bounds : "?");
        }

    }
}


//    @Override
//    protected void paintBelow(GL2 gl, SurfaceRender r) {
//        raw.renderContents(gl, r);
//    }


//    /**
//     * create a static box around the content, which moves along with the surface's bounds
//     */
//    public Dyn2DSurface enclose() {
//        new StaticBox(this::bounds);
//        return this;
//    }

//    private RectFloat2D bounds() {
//        return bounds;
//    }


//    public float rngPolar(float scale) {
//        return
//                (float) rng.nextGaussian() * scale;
//    }
//
//    public float rngNormal(float scale) {
//        return rng.nextFloat() * scale;
//    }

//    /**
//     * spawns in view center at the given size
//     */
//    public PhyWindow put(Surface content, float w, float h) {
//        //Ortho view = (Ortho) root();
//        return put(content, RectFloat2D.XYWH(0, 0, w, h)); //view.x(), view.y(),
//    }
//
//    public PhyWindow frame(Surface content, float w, float h) {
//        return put(new MetaFrame(content), w, h);
//    }
//
//    public PhyWindow put(Surface content, RectFloat2D initialBounds) {
//        return put(content, initialBounds, true);
//    }
//
//    private PhyWindow put(Surface content, RectFloat2D initialBounds, boolean collides) {
//        PhyWindow s = new PhyWindow(initialBounds, collides);
//
//        s.addAt(content);
//
//        return s;
//    }

//    private Snake snake(Wire wire, Runnable onRemove) {
//        Surface source = wire.a;
//        Surface target = wire.b;
//
//        assert (source != target);
//
//        float sa = source.bounds.area();
//        float ta = target.bounds.area();
//        float areaDiff = Math.abs(sa - ta) / (sa + ta);
//
//        int segments = Util.lerp(areaDiff, 8, 6);
//
//        float EXPAND_SCALE_FACTOR = 4;
//
//        PushButton deleteButton = new PushButton("x");
//        Surface menu = new TabPane(Map.of("o", () -> new Gridding(
//                new VectorLabel(source.toString()),
//                new VectorLabel(target.toString()),
//                deleteButton
//        )), (l) -> new CheckBox(l) {
//            @Override
//            protected String label(String text, boolean on) {
//                return text;
//            }
//
//            @Override
//            public ToggleButton setAt(boolean expanded) {
//
//                super.setAt(expanded);
//
//                synchronized (wire) {
//
//                    PhyWindow w = parent(PhyWindow.class);
//                    if (w == null)
//                        return this;
//                    float cx = w.cx();
//                    float cy = w.cy();
//                    float ww, hh;
//                    if (expanded) {
//
//                        ww = w.w() * EXPAND_SCALE_FACTOR;
//                        hh = w.h() * EXPAND_SCALE_FACTOR;
//                    } else {
//
//                        ww = w.w() / EXPAND_SCALE_FACTOR;
//                        hh = w.h() / EXPAND_SCALE_FACTOR;
//                    }
//                    w.pos(cx - ww / 2, cy - hh / 2, cx + ww / 2, cy + hh / 2);
//                }
//
//                return this;
//            }
//        });
//
//        PhyWindow menuBody = put(menu,
//                RectFloat2D.mid(source.bounds, target.bounds, 0.1f));
//
//        float mw = menuBody.radius();
//
////        Snake s = new Snake(source, target, segments, 1.618f * 2 * mw, mw) {
////
////            @Override
////            public void remove() {
////                onRemove.run();
////                super.remove();
////            }
////        };
//
//
//        //s.attach(menuBody.body, segments / 2 - 1);
//
//        deleteButton.click(s::remove);
//
//        int jj = 0;
//        for (Joint j : s.joints) {
//
//            float p = ((float) jj) / (segments - 1);
//
//
//            j.setData((ObjectLongProcedure<GL2>) (g, now) -> {
//
//                int TIME_DECAY_MS = 250;
//                boolean side = p < 0.5f;
//                float activity =
//                        wire.activity(side, now, TIME_DECAY_MS);
//
//
//                int th = wire.typeHash(side);
//                if (th == 0) {
//                    g.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);
//                } else {
//                    Draw.colorHash(g, th, 0.9f, 0.5f + 0.5f * activity, 0.5f + 0.4f * activity);
//                }
//
//                g.glLineWidth(10f + activity * 10f);
//
//
//            });
//            jj++;
//        }
//
//        return s;
//    }









/*
     content = new Graph2D<>();
                //.render()...
        W.setParticleRadius(0.2f);
        W.setParticleDensity(1.0f);

        W.setWarmStarting(true);
        W.setAllowSleep(true);
        W.setContinuousPhysics(true);


 */

//    public final Dynamics2D W = new Dynamics2D(new v2(0, 0));
//    public final Random rng = new XoRoShiRo128PlusRandom(1);
//
//    private FingerDragging jointDrag = new FingerDragging(MOUSE_JOINT_BUTTON) {
//
//        final Body2D ground = W.addBody(new BodyDef(BodyType.STATIC),
//                new FixtureDef(PolygonShape.box(0, 0), 0, 0).noCollide());
//
//        private volatile MouseJoint mj;
//
//        @Override
//        protected boolean startDrag(Finger f) {
//            if (super.startDrag(f)) {
//                Body2D touched2D;
//                if (((touched2D = pick(f)) != null)) {
//                    MouseJointDef def = new MouseJointDef();
//
//                    def.bodyA = ground;
//                    def.bodyB = touched2D;
//                    def.collideConnected = true;
//
//
//                    def.target.setAt(f.pos);
//
//                    def.maxForce = 500f * touched2D.getMass();
//                    def.dampingRatio = 0;
//
//                    mj = (MouseJoint) W.addJoint(new MouseJoint(W.pool, def));
//                    return true;
//                }
//            }
//            return false;
//        }
//
//
//        Body2D pick(Finger ff) {
//            v2 p = ff.pos.scale(scaling);
//
//
//            float w = 0;
//            float h = 0;
//
//
//            final Fixture[] found = {null};
//            W.queryAABB((Fixture f) -> {
//                if (f.body.type != BodyType.STATIC &&
//                        f.filter.maskBits != 0 /* filter non-colllidables */ && f.testPoint(p)) {
//                    found[0] = f;
//                    return false;
//                }
//
//                return true;
//            }, new AABB(new v2(p.x - w, p.y - h), new v2(p.x + w, p.y + h), false));
//
//
//
//
//
//
//
//
//
//
//
//
//            return found[0] != null ? found[0].body : null;
//        }
//
//        @Override
//        public void stop(Finger finger) {
//            super.stop(finger);
//            if (mj != null) {
//                W.removeJoint(mj);
//                mj = null;
//            }
//        }
//
//        @Override
//        protected boolean drag(Finger f) {
//            if (mj != null) {
//                v2 p = f.pos.scale(scaling);
//
//                /*if (clickedPoint != null)*/
//
//
//
//
//                mj.setTarget(p);
//            }
//
//
//
//
//
//
//
//            return true;
//
//        }
//
//    };

//    protected RopeJoint rope(Surface source, Surface target) {
//
//        RopeJointDef jd = new RopeJointDef(source.parent(PhyWindow.class).body, target.parent(PhyWindow.class).body);
//
//        jd.collideConnected = true;
//        jd.maxLength = Float.NaN;
//
//        RopeJoint ropeJoint = new RopeJoint(Dyn2DSurface.this.W.pool, jd) {
//
//            float lengthScale = 2.05f;
//
//            @Override
//            public float targetLength() {
//
//
//                return ((source.radius() + target.radius()) * lengthScale)
//
//                        ;
//
//
//            }
//        };
//
//
//        W.addJoint(ropeJoint);
//        return ropeJoint;
//    }
//

//
//    }
//class StaticBox {
//
//    private final Body2D body;
//    private final Fixture bottom;
//    private final Fixture top;
//    private final Fixture left;
//    private final Fixture right;
//
//    StaticBox(Supplier<RectFloat2D> bounds) {
//
//        float w = 1, h = 1, thick = 0.5f;
//
//        body = W.addBody(new Body2D(new BodyDef(BodyType.STATIC), W) {
//            @Override
//            public boolean preUpdate() {
//                update(bounds.get());
//                synchronizeFixtures();
//                return true;
//            }
//        });
//        bottom = body.addFixture(
//                new FixtureDef(PolygonShape.box(w / 2 - thick / 2, thick / 2),
//                        0, 0)
//        );
//        top = body.addFixture(
//                new FixtureDef(PolygonShape.box(w / 2 - thick / 2, thick / 2),
//                        0, 0)
//        );
//        left = body.addFixture(
//                new FixtureDef(PolygonShape.box(thick / 2, h / 2 - thick / 2),
//                        1, 0)
//        );
//        right = body.addFixture(
//                new FixtureDef(PolygonShape.box(thick / 2, h / 2 - thick / 2),
//                        1, 0)
//        );
//
//
//    }
//
//    void update(RectFloat2D bounds) {
//
//        body.updateFixtures(f -> {
//
//            float cx = bounds.cx() / scaling;
//            float cy = bounds.cy() / scaling;
//            float thick = Math.min(bounds.w, bounds.h) / 16f / scaling;
//
//            float W = bounds.w / scaling;
//            float H = bounds.h / scaling;
//            ((PolygonShape) top.shape).setAsBox(W, thick, new v2(cx / 2, +H), 0);
//            ((PolygonShape) right.shape).setAsBox(thick, H, new v2(+W, cy / 2), 0);
//            ((PolygonShape) bottom.shape).setAsBox(W, thick, new v2(cx, 0), 0);
//            ((PolygonShape) left.shape).setAsBox(thick, H, new v2(0, cy), 0);
//        });
//
//    }
//}