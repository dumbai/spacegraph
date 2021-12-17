package spacegraph.space2d.container.graph.model;

import com.jogamp.opengl.GL2;
import jcog.TODO;
import jcog.Util;
import jcog.data.list.Lst;
import jcog.data.map.ConcurrentFastIteratingHashMap;
import jcog.math.v2;
import jcog.tree.rtree.rect.RectF;
import org.eclipse.collections.api.block.procedure.primitive.ObjectLongProcedure;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.Surfacelike;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.container.ContainerSurface;
import spacegraph.space2d.container.PaintSurface;
import spacegraph.space2d.container.graph.GraphEdit2D;
import spacegraph.space2d.container.graph.Link;
import spacegraph.space2d.meta.WeakSurface;
import spacegraph.space2d.phys.collision.AABB;
import spacegraph.space2d.phys.collision.shapes.CircleShape;
import spacegraph.space2d.phys.collision.shapes.EdgeShape;
import spacegraph.space2d.phys.collision.shapes.PolygonShape;
import spacegraph.space2d.phys.collision.shapes.Shape;
import spacegraph.space2d.phys.common.Settings;
import spacegraph.space2d.phys.dynamics.*;
import spacegraph.space2d.phys.dynamics.joints.*;
import spacegraph.space2d.phys.fracture.PolygonFixture;
import spacegraph.space2d.phys.particle.ParticleSystem;
import spacegraph.space2d.phys.render.Phys2DDraw;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.port.CopyPort;
import spacegraph.space2d.widget.port.Port;
import spacegraph.space2d.widget.port.Wire;
import spacegraph.space2d.widget.windo.Windo;
import spacegraph.util.MutableRectFloat;
import spacegraph.util.ParticleColor;
import spacegraph.video.Draw;

import java.util.List;
import java.util.function.ObjLongConsumer;

import static java.lang.Math.max;
import static jcog.Util.lerpSafe;
import static jcog.Util.sqrt;
import static spacegraph.space2d.container.Bordering.E;
import static spacegraph.space2d.container.Bordering.S;
import static spacegraph.space2d.container.grid.Gridding.PHI;

/**
 * TODO
 */
public class Box2DGraphEditPhysics extends GraphEditPhysics {

    private final Dynamics2D physics;

    private final ConcurrentFastIteratingHashMap<Surface, PhySurface> w =
            new ConcurrentFastIteratingHashMap<>(new PhySurface[0]);

    private static final float minDimension = 0.1f;
    private static final float SHAPE_SIZE_EPSILON = Settings.EPSILON * 2;

    static float LINEAR_DAMPENING = 0.99f;
    static float ANGULAR_DAMPENING = 0.5f;
    private float timeScale = 64;
    private int posSteps = 1;
    private int velSteps = 2;

    public static class PhySurface<S extends Surface> {
        final S surface;
        final Body2D body;
        private final PolygonShape shape;


        static final float velUpdateRate = 0.95f;


        private transient float wNext = 0, hNext = 0;

        private PhySurface(S surface, Body2D body) {
            this(surface, body, 1.0f);
        }
        private PhySurface(S surface, Body2D body, float density) {
            this.surface = surface;
            this.body = body;
            this.shape = PolygonShape.box(max(minDimension, surface.w() / 2), max(minDimension, surface.h() / 2));
            body.setFixedRotation(true);
            Fixture f = body.addFixture(shape, density);
            f.setRestitution(0.0f);
            body.data(surface);
        }


        void pre(Dynamics2D physics, RectF outer) {

            RectF r = source().clamp(outer);

            boolean resized = false;

            float nrw = r.w, nrh = r.h;

            if (!Util.equals(nrw, wNext, SHAPE_SIZE_EPSILON) || !Util.equals(nrh, hNext, SHAPE_SIZE_EPSILON)) {

                body.updateFixtures(f -> {
                    wNext = max(1, nrw); //TODO max = container width
                    hNext = max(1, nrh); //TODO max = container height
                    f.setShape(shape.setAsBox(wNext / 2 / 1, hNext / 2 / 1));
                });
                resized = true;
            }

            float ncx = r.cx(), ncy = r.cy();


            v2 at = body.pos;
            v2 vel = body.vel;
            v2 deltaPos = new v2(
                lerpSafe(velUpdateRate, vel.x, (ncx - at.x)),
                lerpSafe(velUpdateRate, vel.y, (ncy - at.y))
            );
            if (deltaPos.lengthSquared() > minDimension*minDimension) {
                body.setLinearVelocity(deltaPos);
            } else if (resized)
                body.setAwake(true); //HACK force awake
        }

        public RectF source() {
            return surface.bounds;
        }
        public RectF target() {
            v2 p = body.pos;
            return RectF.XYWH(
                    p.x * 1, p.y * 1,
                    this.wNext, this.hNext);
        }

        void post(Dynamics2D physics, RectF outer) {


//            RectFloat bounds = surface.bounds;
            surface.pos(target().clamp(outer));


            //if (!r.equals(physBounds, Spatialization.EPSILONf)) {

            //pos(physBounds = r);
            //}
        }


    }


    public Box2DGraphEditPhysics() {
        this.physics = new Dynamics2D();

        //physics.setSubStepping(true);
        physics.setWarmStarting(true);
        physics.setContinuousPhysics(true);
        physics.setAllowSleep(true);
    }

    @Override
    public void pos(Surface s, RectF b) {
        pos(shadow(s), b);
    }

    public void pos(PhySurface s, RectF b) {
        s.surface.resize(b.w, b.h);
        s.body.pos.set(b.cx(), b.cy());
    }

    @Override
    public PhySurface add(Surface w) {
        return this.w.computeIfAbsent(w, (ww->{
            Body2D body = new Body2D(BodyType.DYNAMIC, physics);
            body.setLinearDamping(LINEAR_DAMPENING);
            PhySurface<?> wd = ww instanceof Windo ? new PhyWindo((Windo) ww, body) : new PhySurface(ww, body);
            physics.addBody(wd.body);
            return wd;
        }));
    }
    private PhySurface shadow(Surface w) {
        return this.w.computeIfAbsent(w, (ww->{
            Body2D body = new Body2D(BodyType.DYNAMIC, physics) {

//                @Override
//                public boolean preUpdate() {
//
//                    //prevent collisions HACK
//                    Fixture f = this.fixtures;
//                    if (f!=null) {
//                        Fixture fn = f.next;
//                        if (fn!=null) {
//                            Filter fnf = fn.filter;
//                            if (fnf!=null) fnf.groupIndex = -1;
//                        }
//                    }
//                    return super.preUpdate();
//                }

                @Override
                public boolean colllide(Body2D other) {
                    return false;
                }

            };

            PhySurface<?> wd = new DependentPhySurface(ww, body);
            physics.addBody(wd.body);
            pos(wd, w.bounds);
            return wd;
        }));
    }

    static final class PhyWindo extends PhySurface<Windo> {

        private PhyWindo(Windo surface, Body2D body) {
            super(surface, body);
        }

        @Override
        void pre(Dynamics2D physics, RectF bounds) {
            if (surface.parent == null) {
                physics.removeBody(body);
            } else {
                body.setType(surface.fixed() ? BodyType.STATIC : BodyType.DYNAMIC,
                        physics);
                super.pre(physics, bounds);
            }
        }

        @Override
        void post(Dynamics2D physics, RectF clamp) {
            if (!surface.fixed()) {
                super.post(physics, clamp);
            }
        }
    }

    @Override
    public void remove(Surface w) {
        physics.removeBody(this.w.remove(w).body, false);
    }

    @Override
    public void starting(GraphEdit2D g) {
        below = new Dyn2DRenderer(true, true, false);
        above = new Dyn2DRenderer(false, false, true);
        update(g, 0);
    }

    /** fence */
    private transient RectF outer;

    @Override
    public final void update(GraphEdit2D g, float dt) {
        outer = g.bounds;

        physics.sync();

        w.forEachValue(ww -> ww.pre(physics, outer));

//        physics.sync();

        physics.step(dt * timeScale, velSteps, posSteps);

//        physics.sync();

        w.forEachValue(ww -> ww.post(physics, outer));
    }
//
//    /** apply any preprocessing of bounds before entry to the physics engine (and affecting its possible feedback after it finishes)
//     * @param r*/
//    private RectFloat preBounds(Surface r) {
//        RectFloat b = preBoundsPos(preBoundsSize(r.bounds));
//        r.pos(b);
//        return b;
//    }
//
//    private RectFloat preBoundsPos(RectFloat x) {
//        return x.clamp(graph.bounds);
////        return x.move(
////                    Math.min(fence.w - x.right(), Math.max(0, -x.left())),
////                    Math.min(fence.h - x.top(), Math.max(0, -x.bottom())));
//    }
//
//    private RectFloat preBoundsSize(RectFloat r) {
//        float rw = r.w; if (!Float.isFinite(rw)) rw = 0;
//        float rh = r.h; if (!Float.isFinite(rh)) rh = 0;
//        float nw = Util.clamp(rw, wMin, clamp.w), nh = Util.clamp(rh, hMin, clamp.h);
//        return r.size(nw, nh);
//    }

    @Override
    public void stop() {

    }

    @Override
    public Link link(Wire w) {
        //one is a dependent of the other
        return (w.a instanceof WeakSurface) ^ (w.b instanceof WeakSurface) ? new GlueLink(w) : new SnakeLink(w);
    }

    @Override
    public final void invokeLater(Runnable o) {
        physics.invoke(o);
    }

    @Deprecated private static final Object Invisible = new Object();

    /** does not update the surface it's attached to */
    private static class DependentPhySurface extends PhySurface {

        float scale = 1.0f;

        MutableRectFloat b = new MutableRectFloat();

        DependentPhySurface(Surface ww, Body2D body) {
            super(ww, body, 0.01f);
            body.data(Invisible);
        }

        @Override
        public RectF source() {
            RectF s = super.source();
            b.w = s.w*scale;
            b.h = s.h*scale;
            b.clamp(s);
            b._commit();
            return b;
        }

        @Override
        void post(Dynamics2D physics, RectF outer) {
            b.set(target().clamp(outer).clamp(super.source()));
        }
    }

    abstract class Box2DLink extends GraphEdit2D.VisibleLink {

        Box2DLink(Wire wire) {
            super(wire);
        }

        @Nullable Dynamics2D world() {
            Body2D b = sourceBody();
            return b.W;
//            Body2D c = targetBody();
//            if (c != null)
//                return c.W;
//            return null;
        }

        <C extends ContainerSurface> PhySurface body(Surface x, Class<? extends C> type) {
            while (true) {
                if (type.isInstance(x)) {
                    return shadow(x);
    //                PhySurface p = phy((ContainerSurface) x);
    //                if (p != null)
    //                    return p.body;
                }

//            while (true) {
                Surfacelike p = x.parent;
                if (!(p instanceof ContainerSurface)) return null;
                x = (Surface)p;
            }
//                Surfacelike xx = x.parent;
//                if (!(xx instanceof ContainerSurface))
//                    break;
//
//                x = (Surface) xx;
//            }
//            return null;
        }

        final Body2D sourceBody() { return body(a(), ContainerSurface.class).body; }

        final Body2D targetBody() {
            return body(b(), ContainerSurface.class).body;
        }


        float targetRadius() {
            return targetBounds().extents().length();
        }

        float sourceRadius() {
            return sourceBounds().extents().length();
        }

        AABB sourceBounds() {
            return physicsBounds(sourceBody());
        }

        AABB targetBounds() {
            return physicsBounds(this.targetBody());
        }


        AABB physicsBounds(Body2D x) {
            Fixture f = x.fixtures();
            //empty
            return f != null ? f.getAABB(0) : new AABB();

        }

        void remove() {
            remove(graph);
        }

    }

    /**
     * lightweight elastic attachment/binder
     */
    class GlueLink extends Box2DLink {

        private final Joint joint;

        static final float margin = 0.02f;

        GlueLink(Wire wire) {
            super(wire);

            Dynamics2D w = world();
            if (w == null) {
                throw new TODO();
                //wire.remove();
            }

            RopeJointDef jd = new RopeJointDef(sourceBody(), targetBody());
            //WeldJointDef jd = new WeldJointDef();
            jd.maxLength = targetLength();
            jd.collideConnected = true;

//            jd.bodyA = sourceBody();
//            jd.bodyB = targetBody();


            //joint = new WeldJoint(w, jd);
            joint = new RopeJoint(w.pool, jd) {
                @Override
                public void initVelocityConstraints(SolverData data) {
                    setPositionFactor(0.01f);
                    this.targetLength = GlueLink.this.targetLength();
                    super.initVelocityConstraints(data);
                }
            };

            w.addJoint(joint);

            on(() -> {
                Dynamics2D ww = world();
                if (ww != null)
                    ww.removeJoint(joint);
            });
        }

        float targetLength() {
            //TODO sourceBody().fixtures.shape.computeDistanceToOut()

            Body2D a = sourceBody();
            Body2D b = targetBody();
            //                RayCastOutput out12 = new RayCastOutput();
            //                RayCastInput in12 = new RayCastInput();
            //                in12.p2.setAt(a.pos);
            //                in12.p1.setAt(b.pos);
            //                in12.maxFraction = 1f;
            //                if (a.fixtures.raycast(out12, in12, 0)) {
            //                    RayCastInput in21 = new RayCastInput();
            //                    in21.p1.setAt(a.pos);
            //                    in21.p2.setAt(b.pos);
            //                    in21.maxFraction = 1-out12.fraction;
            //                    RayCastOutput out21 = new RayCastOutput();
            //                    if (b.fixtures.raycast(out21, in21, 0)) {
            //                        return (out21.fraction + out12.fraction) * in12.p1.distance(in12.p2);
            //                    }
            //
            //                }
            //                float ad = a.fixtures.distance(b.pos, 0, b.pos.sub(a.pos));
            //                float bd = b.fixtures.distance(a.pos, 0, a.pos.sub(b.pos));
            //                return (ad + bd) * (1f+margin);
            return a != null && b != null && a.fixtures != null && b.fixtures != null ? (sourceRadius() + targetRadius()) * (1.0f + margin) : 0;
        }
    }

    /**
     * represents a cable that transmits data from port to port
     */
    class SnakeLink extends Box2DLink {

        private final Snake snake;
        private final Windo linkPanel;
        final int segments = 7;

        SnakeLink(Wire wire) {
            super(wire);


            Bordering l = new Bordering<>(
                    new PushButton("X").clicked((@Nullable Runnable) this::remove)
            );
            l.set(E, new PushButton("Tap").clicked(() -> splice(new CopyPort()))); //as in wire-tap, aka splice
            l.set(S, new PushButton("Split"));

            linkPanel = graph.add(l);
            on(linkPanel::delete);
            PhySurface linkPanelBody = body(linkPanel, Windo.class);

            this.snake = new Snake(sourceBody(), targetBody(), segments) {
                @Override
                protected void updateGeometry() {
                    super.updateGeometry();

                    float r = widgetRadius();
                    linkPanelBody.wNext = Math.max(linkPanelBody.wNext, r);
                    linkPanelBody.hNext = Math.max(linkPanelBody.hNext, r);
                }
            };
            on(snake::remove);



            snake.updateGeometry();
            snake.attach(linkPanelBody.body, segments / 2 - 1);

        }

        v2 center() {
            return a().bounds.midPoint(b().bounds);
        }

        float widgetRadius() {
            Snake s = this.snake;
            return (s != null ? s.elementThickness * (1 / PHI) : (a().radius() + b().radius()))* 1;
        }

        void splice(Port port) {
            synchronized (this) {
                remove();

                ContainerSurface wPort = graph.add(port);
                wPort.pos(center());
                float w = widgetRadius() * 4.0f;
                wPort.resize(w, w);
                graph.addWire(new Wire(a(), port));
                graph.addWire(new Wire(b(), port));
            }
        }


    }

    static class Snake {

        float TENSION = 0.25f; //0.75f;

        private final List<Body2D> bodies;
        private final List<Body2D> attachments;
        final List<Joint> joints;
        //        private final Surface source;
//        private final Surface target;
        private final Body2D sourceBody;
        private final Body2D targetBody;
        private final int n;

        private transient float elementLength;
        transient float elementThickness;

        v2 sourceCenterWorld() {
            return sourceBody.pos;
        }

        v2 targetCenterWorld() {
            return targetBody.pos;
        }

        float distance() {
            return sourceBody.pos.distance(targetBody.pos);
        }

        void updateGeometry() {
            float d = distance();
            elementLength = (d / n) * Util.PHI_min_1f;

            float sourceRadius = sourceRadius(); //((Surface)sourceBody.data()).radius();
            float targetRadius = targetRadius();  //((Surface)targetBody.data()).radius();
            elementThickness = max(Settings.EPSILONsqrt,
                    Util.mean(sourceRadius, targetRadius) / (10.0f) / (1 + sqrt(((d)/(sourceRadius+targetRadius)))));


            //TODO get from surfaces Math.min( sourceBody.fixtures.shape.radius, targetBody.fixtures.shape.radius );
        }

        float targetRadius() {
            return radius(targetBody);
        }

        float sourceRadius() {
            return radius(sourceBody);
        }

        private static float radius(@Nullable Body2D t) {
            Fixture tf = t!=null ? t.fixtures : null;
            return tf!=null ? tf.getAABB(0).extents().length() : 1;
        }


        Snake(Body2D source, Body2D target, int num) {

//            this.source = source;
//            this.target = target;
            this.sourceBody = source;
            this.targetBody = target;

            this.n = num;

            bodies = new Lst(num);
            joints = new Lst(num);
            attachments = new Lst(0);

            Dynamics2D w = sourceBody.W;

            updateGeometry();

            FixtureDef segment = new FixtureDef(PolygonShape.box(1, 1), 0.01f, 0.0f);
            segment.restitution = 0.0f;
            segment.filter.maskBits = 0;
            segment.filter.groupIndex = -1;

            FixtureDef segmentCollidable = new FixtureDef(PolygonShape.box(1, 1), 0.01f, 0.0f);
            segmentCollidable.restitution = 0.0f;

            Body2D from = null;

            v2 center = sourceCenterWorld().addToNew(targetCenterWorld()).scaleClone(0.5f);

            int mid = num / 2;
            for (int i = 0; i < num; ++i) {


                if (from == null) {
                    from = sourceBody;
                } else {


                    Body2D to;
                    if (i == num - 1) {
                        to = targetBody;
                    } else {
                        bodies.add(to = new SnakeElementBody(
                                center, i, i == mid ? segmentCollidable : segment, w
                        ));
                    }

                    joints.add(new MyRevoluteJoint(newJoint(from, to), TENSION, w));

                    from = to;
                }


            }

            w.invoke(() -> {
                for (Body2D b : bodies) w.addBody(b);
                for (Joint j : joints) w.addJoint(j);
            });
        }

        private RevoluteJointDef newJoint(Body2D from, Body2D to) {
            RevoluteJointDef jd = new RevoluteJointDef(true);
            jd.collideConnected = false;
            jd.bodyA = from;
            jd.bodyB = to;
            jd.upperAngle = (float) (+Math.PI * 1.0f);
            jd.lowerAngle = -jd.upperAngle;

            //jd.referenceAngle = 0;
            //jd.motorSpeed = 100;
            return jd;
        }


        /**
         * attach a body to center of one of the segments
         */
        void attach(Body2D b, int segment) {
            world().invoke(() -> {
                RevoluteJointDef rr = new RevoluteJointDef(bodies.get(segment), b);
                RevoluteJoint w = b.W.addJoint(rr);
                attachments.add(b);
                joints.add(w);
            });
        }

        private Dynamics2D world() {
            return bodies.get(0).W;
        }

        void remove() {

            world().invoke(() -> {

                for (Body2D attachment : attachments)
                    attachment.remove();

                attachments.clear();

                for (Body2D body : bodies)
                    body.remove();

                bodies.clear();
            });
        }

        private static class MyRevoluteJoint extends RevoluteJoint {


            MyRevoluteJoint(RevoluteJointDef jd, float power, Dynamics2D w) {
                super(w, jd);
                this.positionFactor = power;
            }

        }

        private class SnakeElementBody extends Body2D {

            private final int i;
            float eleLen;
            float eleThick;

            SnakeElementBody(v2 center, int i, FixtureDef fxture, Dynamics2D w) {
                super(new BodyDef(BodyType.DYNAMIC, center), w);
                this.i = i;
                eleLen = Float.NaN;
                eleThick = Float.NaN;
                addFixture(fxture);
                setGravityScale(0);
                setLinearDamping(LINEAR_DAMPENING);
                setAngularDamping(ANGULAR_DAMPENING);
                postUpdate();
            }

            @Override
            public void postUpdate() {


                if (i == 1 /* head */ && (sourceBody.isActive() || targetBody.isActive())) {
                    updateGeometry();
                }

                if (!Util.equals(eleLen, elementLength, Settings.EPSILONsqrt) || !Util.equals(eleThick, elementThickness, Settings.EPSILONsqrt)) {
                    eleLen = elementLength;
                    eleThick = elementThickness;

                    _updateFixtures(this::updateFixtures);
                }

            }

            private void updateFixtures(Fixture f /* the only one */) {
                ((PolygonShape) f.shape).setAsBox(eleLen, eleThick);

                RevoluteJoint rj = (RevoluteJoint) ((Snake.this.joints).get(i - 1));
                if (rj != null) {
                    rj.getLocalAnchorB().set(+eleLen, 0);
                }

                RevoluteJoint rk = (RevoluteJoint) ((Snake.this.joints).get(i));
                if (rk != null) {
                    rk.getLocalAnchorA().set(-eleLen, 0);
                }
            }

        }
    }


//    @Deprecated public class PhyWindow extends Windo {
//        //public final Body2D body;
//        //private final PolygonShape shape;
//
//
//        PhyWindow(RectFloat2D initialBounds, boolean collides) {
//            super();
//            pos(initialBounds);
//
//
////            this.shape =
////
////                    PolygonShape.box(initialBounds.w / 2, initialBounds.h / 2);
////
////            FixtureDef fd = new FixtureDef(shape, 1f, 0.75f);
////            if (!collides) {
////                fd.filter.maskBits = 0;
////            }
////
////            fd.setRestitution(0.1f);
////
////
////            W.addBody(this.body = new WallBody(initialBounds.cx(), initialBounds.cy()), fd);
////            body.setLinearDamping(linearDampening);
////            if (!collides) {
////                body.setGravityScale(0f);
////            }
//        }
//
////        void setCollidable(boolean c) {
////            W.invoke(() -> {
////                body.fixtures.filter.maskBits = (c ? 0xffff : 0);
////                body.setGravityScale(c ? 1f : 0f);
////                body.setAwake(true);
////            });
////        }
//
//        @Override
//        public boolean fingerable(DragEdit d) {
//            if (d == DragEdit.MOVE)
//                return false;
//
//
//            return super.fingerable(d);
//        }
//
//        public void remove() {
//            synchronized (links) {
//                links.removeNode(this);
//            }
////            W.removeBody(this.body);
//            //Dyn2DSurface.this.remove(this);
//            throw new TODO();
//        }
//
//
//        public Pair<PhyWindow, Wire> sprout(Surface target, float scale) {
//            return sprout(target, scale, 1f);
//        }
//
//        /**
//         * convenience method for essentially growing a separate window
//         * of a proportional size with some content (ex: a port),
//         * and linking it to this window via a constraint.
//         */
//        Pair<PhyWindow, Wire> sprout(Surface target, float scale, float targetAspect) {
//            PhyWindow sprouted = spawn(target, scale, targetAspect);
//
//            return pair(sprouted, link(target));
//        }
//
////        /**
////         * spawns and attaches a new component to the boundary of this
////         */
////        public PhyWindow grow(Surface target, float scale, float targetAspect, v2 normal) {
////
////            PhyWindow x = spawn(target, scale, targetAspect);
////
////            W.invoke(() -> {
////                v2 myWeldLocal, theirWeldLocal;
////                RayCastInput input = new RayCastInput();
////                RayCastOutput output = new RayCastOutput();
////                {
////                    input.p2.setAt(0, 0);
////                    float r = radius() * 2;
////                    input.p1.setAt(0 + normal.x * r, 0 + normal.y * r);
////                    input.maxFraction = 1.0f;
////
////                    boolean hit = body.fixtures.raycast(output, input, 0);
////                    assert (hit);
////                    v2 hitPoint = (input.p2.sub(input.p1)).scaled(output.fraction).added(input.p1);
////                    myWeldLocal = hitPoint;
////                }
////                {
////                    input.p2.setAt(0, 0);
////                    float r = x.radius() * 2;
////                    input.p1.setAt(0 - normal.x * r, 0 - normal.y * r);
////                    input.maxFraction = 1.0f;
////
////                    boolean hit = x.body.fixtures.raycast(output, input, 0);
////                    assert (hit);
////                    v2 hitPoint = (input.p2.sub(input.p1)).scaled(output.fraction).added(input.p1);
////                    theirWeldLocal = hitPoint;
////                }
////
////                WeldJoint j = weld(x, myWeldLocal, theirWeldLocal);
////
////            });
////            return x;
////        }
//
////        private WeldJoint weld(PhyWindow x, v2 myLocal, v2 theirLocal) {
////            WeldJointDef jd = new WeldJointDef();
////            jd.bodyA = this.body;
////            jd.bodyB = x.body;
////            jd.localAnchorA.setAt(myLocal.scaled(0.5f));
////            jd.localAnchorB.setAt(theirLocal.scaled(0.5f));
////            jd.referenceAngle = ((v2) myLocal).angle(theirLocal);
////            jd.collideConnected = false;
////            jd.dampingRatio = 0.5f;
////            jd.frequencyHz = 0.25f;
////
////            WeldJoint j = new WeldJoint(W.pool, jd);
////            W.addJoint(j);
////            return j;
////        }
//
//        PhyWindow spawn(Surface target, float scale, float targetAspect) {
//            float W = w();
//            float H = h();
//            float sprouterRadius = radius();
//            float w = W * scale;
//            float h = H * scale;
//
//
//            RectFloat2D sproutSize = RectFloat2D.XYWH(0, 0, w, h);
//
//
//            float minRadius = sprouterRadius + sproutSize.radius();
//
//            float a = (float) (Math.random() * 2 * (float) Math.PI);
//            float dx = cx() + (float) (minRadius * Math.cos(a));
//            float dy = cy() + (float) (minRadius * Math.sin(a));
//
//            return put(target, sproutSize.move(dx, dy, Spatialization.EPSILONf));
//        }
//
//
//        /**
//         * assumes the PhyWindow wraps *THE* source
//         */
//        Wire link(Surface target) {
//            assert (children().length == 1);
//            return link(get(0), target);
//        }
//
//
//
//        /**
//         * convenience method for creating a basic undirected link joint.
//         * no endpoint is necessarily an owner of the other so
//         * it should not matter who is the callee.
//         * <p>
//         * duplicate links are prevented.
//         */
//        public Wire link(Surface source, Surface target) {
//            return link(new Wire(source, target));
//        }
//
//        /**
//         * undirected link
//         */
//        Wire link(Wire wire) {
//
//            Surface aa = wire.a;
//            Surface bb = wire.b;
//
//            synchronized (links) {
//
//                NodeGraph.MutableNode<Surface, Wire> A = links.addNode(aa);
//
//                Iterable<FromTo<Node<spacegraph.space2d.Surface, spacegraph.space2d.widget.windo.Wire>, Wire>> edges = A.edges(false, true);
//                if (edges != null) {
//
//                    for (FromTo<Node<spacegraph.space2d.Surface, spacegraph.space2d.widget.windo.Wire>, Wire> e : edges) {
//                        Wire ee = e.id();
//                        if (wire.equals(ee))
//                            return ee;
//                    }
//                }
//
//                if (!wire.connect()) {
//                    return null;
//                }
//
//                NodeGraph.MutableNode<Surface, Wire> B = links.addNode(bb);
//                links.addEdge(A, wire, B);
//
//
////                W.invoke(() -> {
////
////
////                    {
////
////
////                        Snake s = snake(wire, () -> unlink(aa, bb));
////
////                    }
////
////
////                });
//            }
//
//            return wire;
//
//        }
//
//
//        void sproutBranch(String label, float scale, float childScale, Iterable<Surface> children) {
//            CheckBox toggle = new CheckBox(label);
//            Pair<PhyWindow, Wire> toggleWindo = sprout(toggle, scale);
////            List<PhyWindow> built = new FasterList(0);
////            toggle.on((cb, enabled) -> W.invoke(() -> {
////
////                if (enabled) {
////                    for (Surface x : children) {
////                        built.addAt(toggleWindo.getOne().sprout(x, childScale).getOne());
////                    }
////                } else {
////
////                    built.forEach(PhyWindow::remove);
////                    built.clear();
////                }
////
////            }));
//        }
//
//        public void sproutBranch(String label, float scale, float childScale, Supplier<Surface[]> children) {
//            sproutBranch(label, scale, childScale, ArrayIterator.iterable(children.get()));
//        }
//
//        @Override
//        public boolean tangible() {
//            return true;
//        }
//
//    }

    private class Dyn2DRenderer extends PaintSurface {
        final boolean drawJoints;
        final boolean drawBodies;
        final boolean drawParticles;

        Dyn2DRenderer(boolean drawJoints, boolean drawBodies, boolean drawParticles) {
            this.drawJoints = drawJoints;
            this.drawBodies = drawBodies;
            this.drawParticles = drawParticles;
        }

        @Override
        protected void paint(GL2 gl, ReSurface reSurface) {

            Dynamics2D w = physics;

            if (drawJoints) {
                long frameNS = reSurface.frameNS;
                w.joints(j -> drawJoint(j, frameNS, gl));
            }

            if (drawBodies)
                w.bodies(b -> drawBody(b, gl));

            if (drawParticles)
                drawParticleSystem(gl, w.particles);

        }

        private void drawParticleSystem(GL2 gl, ParticleSystem system) {

            int particleCount = system.getParticleCount();
            if (particleCount != 0) {
                float particleRadius = system.getParticleRadius();
                v2[] positionBuffer = system.getParticlePositionBuffer();
                ParticleColor[] colorBuffer = null;
                if (system.m_colorBuffer.data != null) {
                    colorBuffer = system.getParticleColorBuffer();
                }


                Draw.particles(gl, positionBuffer, particleRadius, 6, colorBuffer, particleCount);

            }
        }

        private void drawJoint(Joint joint, long now, GL2 g) {
            Object data = joint.data();
            if (data instanceof ObjectLongProcedure) {
                ((ObjLongConsumer) data).accept(g, now);
            } else {

                Draw.colorHash(g, joint.getClass().hashCode(), 0.5f);
                g.glLineWidth(10.0f);
            }
            v2 v1 = new v2(), v2 = new v2();
//            switch (joint.getType()) {
//                default:
            joint.anchorA(v1);
            joint.anchorB(v2);
//                    break;
//            }
            Draw.linf(v1.x * 1, v1.y * 1, v2.x * 1, v2.y * 1, g);

        }

        private void drawBody(Body2D body, GL2 gl) {


            //        if (body.data() instanceof PhyWindow.WallBody) {
            //            return;
            //        }
//            if (body instanceof Consumer) {
//                ((Consumer) body).accept(gl);
//                return;
//            }

            Object d = body.data();
            if (d == Invisible)
                return; //HACK

            boolean awake = body.isAwake();

            gl.glColor4f(0.5f, 0.5f, 0.5f, awake ? 0.75f : 0.65f);

            for (Fixture f = body.fixtures; f != null; f = f.next) {
                PolygonFixture pg = f.polygon;
                if (pg != null) {

                } else {
                    Shape shape = f.shape();


                    switch (shape.m_type) {
                        case POLYGON -> Phys2DDraw.poly(body, gl, 1, (PolygonShape) shape);
                        case CIRCLE -> {
                            CircleShape circle = (CircleShape) shape;
                            float r = circle.skinRadius;
                            v2 v = new v2();
                            body.getWorldPointToOut(circle.center, v);
                            v.scaleClone(1);
                            Draw.circle(gl, v, true, r * 1, 9);
                        }
                        case EDGE -> {
                            EdgeShape edge = (EdgeShape) shape;
                            v2 p1 = edge.m_vertex1;
                            v2 p2 = edge.m_vertex2;
                            gl.glLineWidth(4.0f);
                            Draw.linf(p1.x * 1, p1.y * 1, p2.x * 1, p2.y * 1, gl);
                        }
                    }
                }
            }


        }

        //TODO
    }


}