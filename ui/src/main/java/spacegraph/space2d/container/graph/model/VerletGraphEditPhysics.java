package spacegraph.space2d.container.graph.model;

import com.jogamp.opengl.GL2;
import jcog.Util;
import jcog.data.map.ConcurrentFastIteratingHashMap;
import jcog.exe.Exe;
import jcog.tree.rtree.rect.RectF;
import org.eclipse.collections.api.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.graph.GraphEdit2D;
import spacegraph.space2d.container.graph.Link;
import spacegraph.space2d.meta.MetaFrame;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.port.Wire;
import spacegraph.space2d.widget.shape.VerletSurface;
import spacegraph.space2d.widget.windo.Windo;
import spacegraph.video.Draw;
import toxi.physics2d.VerletParticle2D;
import toxi.physics2d.VerletPhysics2D;
import toxi.physics2d.behavior.AttractionBehavior2D;
import toxi.physics2d.spring.VerletSpring2D;

import java.util.List;

public class VerletGraphEditPhysics extends GraphEditPhysics {

    protected final VerletSurface physics = new VerletSurface();

    private final ConcurrentFastIteratingHashMap<Surface, PhySurface> w =
        new ConcurrentFastIteratingHashMap<>(new PhySurface[0]);

    private static final boolean DEBUG = true;
    float drag =
        //0;
        //0.5f;
        0.05f;
        //0.1f;


    public static final class PhySurface {

        final Surface surface;
        final VerletParticle2D particle;
        final AttractionBehavior2D repel;

        private PhySurface(Surface surface, VerletPhysics2D phy) {
            this.surface = surface;
            this.particle = new VerletParticle2D();
            this.repel = new AttractionBehavior2D(particle, 1, 0);
            phy.addParticle(particle);
            phy.addBehavior(repel);
        }

        private void delete(VerletPhysics2D phy) {
            phy.removeParticle(particle);
            phy.removeBehavior(repel);
        }

        public boolean update(VerletPhysics2D phy) {
            Surface p = (Surface) surface.parent;
            if (p == null) {
                delete(phy);
                return false; //removed
            }

            var c = surface.bounds.clamp(p.bounds);
            particle.mass(c.area());
            particle.set(c.cx(), c.cy());

            repel.setRadius(surface.radius() * 2);
            repel.setStrength(
                    -(float) (Math.sqrt(particle.mass()) * 0.1)
                    //-50
            );
            return true;
        }

        public void updatePost() {
            surface.pos(particle.x()-surface.w()/2,  particle.y()-surface.h()/2);
        }

    }

    @Override
    protected void starting(GraphEdit2D parent) {
        physics.debugRender.set(DEBUG);
        below = physics;
        physics.pos(parent.bounds);
        physics.start(parent);
    }

    @Override
    public final void invokeLater(Runnable o) {
        Exe.runLater(o);
    }

    @Override
    public void pos(Surface x, RectF pos) {
        PhySurface y = w.get(x);
        y.particle.set(pos.x, pos.y);
        y.surface.resize(pos.w, pos.h);
    }

    @Override
    public void stop() {
        physics.stop();
    }

    @Override
    public PhySurface add(Surface x) {
        return w.computeIfAbsent(x, y->{
            PhySurface z = new PhySurface(y, physics.physics);
            return z;
        });
    }

    @Override
    public synchronized void update(GraphEdit2D g, float dt) {
        VerletPhysics2D pp = this.physics.physics;
        pp.setDrag(drag);

        w.removeIf(z -> !z.update(pp));

        pp.update(dt);

        for (PhySurface w : w.valueArray()) {
//            System.out.println(w.surface.pos() + "\t" + w.center);
            w.updatePost();
        }

//        System.out.println();

    }

    @Override
    public void remove(Surface x) {
        PhySurface removed = this.w.remove(x);
        physics.physics.removeBehavior(removed.repel);
    }


    @Override
    public Link link(Wire w) {
        return new VerletVisibleLink(w);
    }

    class VerletVisibleLink extends GraphEdit2D.VisibleLink {

        VerletVisibleLink(@Nullable Wire w) {
            super(w);
            Surface a = w.a;
            Surface b = w.b;
            VerletParticle2D ap = physics.bind(a, VerletSurface.VerletSurfaceBinding.NearestSurfaceEdge);
            VerletParticle2D bp = physics.bind(b, VerletSurface.VerletSurfaceBinding.NearestSurfaceEdge);

            int extraJoints = 3;
            int chainLen = 2 + 1 + (extraJoints * 2); //should be an odd number

            Pair<List<VerletParticle2D>, List<VerletSpring2D>> chain = physics.addParticleChain(ap, bp,
                    chainLen, 0.0f /* some minimal # */, 0.5f);

            List<VerletSpring2D> springs = chain.getTwo();
            on(() -> {
                //destroy the chain springs on destruction
                VerletPhysics2D verletPhysics2D = physics.physics;
                for (VerletSpring2D spring : springs) {
                    verletPhysics2D.removeSpringAndItsParticles(spring);
                }
            });

            List<VerletParticle2D> points = chain.getOne();
//        VerletParticle2D first = points.get(0);
//        VerletParticle2D last = points.get(points.size() - 1);
            VerletParticle2D mid = points.get(points.size() / 2);


//        if (first!=mid) {
//            mid.addBehaviorGlobal(new AttractionBehavior2D<>(mid, 300, -1));
//        }


            bind(graph.add(new PushButton("x", () -> remove(graph)), ff ->
                            new Windo(new MetaFrame(ff))).resize(20, 20),
                    mid, false, VerletSurface.VerletSurfaceBinding.Center, graph);


//            bind(graph.add(new PushButton(".."), Windo::new).resize(5, 5),
//                    chain.getOne().get(1), false, VerletSurface.VerletSurfaceBinding.Center, graph);
//            bind(graph.add(new PushButton(".."), Windo::new).resize(5, 5),
//                    chain.getOne().get(chainLen - 2), false, VerletSurface.VerletSurfaceBinding.Center, graph);

            /** link rendering */
            Surface r = renderer(chain);
            on(r);

            graph.addRaw(r);
        }

        void bind(Surface gripWindow, VerletParticle2D particle, boolean surfaceOverrides, VerletSurface.VerletSurfaceBinding where, GraphEdit2D g) {
            physics.bind(gripWindow, particle, surfaceOverrides, where);
            on(gripWindow);
        }

        private Surface renderer(Pair<List<VerletParticle2D>, List<VerletSpring2D>> chain) {
            return new VerletVisibleLinkSurface(chain);
        }

        private class VerletVisibleLinkSurface extends VisibleLinkSurface {

            private final Pair<List<VerletParticle2D>, List<VerletSpring2D>> chain;

            VerletVisibleLinkSurface(Pair<List<VerletParticle2D>, List<VerletSpring2D>> chain) {
                this.chain = chain;
            }

            @Override protected void paintLink(GL2 gl, ReSurface reSurface) {
                int window = 100 * 1000 * 1000;
                long renderStart = reSurface.frameNS;

                Wire id = VerletVisibleLink.this.id;
                float aa = id.activity(true, renderStart, window);
                float bb = id.activity(false, renderStart, window);

                float base = Util.min(a().radius(), b().radius());
                float baseA = base * Util.lerp(aa, 0.25f, 0.75f);
                float baseB = base * Util.lerp(bb, 0.25f, 0.75f);
                Draw.colorHash(gl, id.typeHash(true), 0.25f + 0.45f * aa);
                for (VerletSpring2D s : chain.getTwo())
                    Draw.halfTriEdge2D(s.a, s.b, baseA, gl); //Draw.line(a.x, a.y, b.x, b.y, gl);

                Draw.colorHash(gl, id.typeHash(false), 0.25f + 0.45f * bb);
                for (VerletSpring2D s : chain.getTwo())
                    Draw.halfTriEdge2D(s.b, s.a, baseB, gl); //Draw.line(a.x, a.y, b.x, b.y, gl);

            }
        }

    }

}