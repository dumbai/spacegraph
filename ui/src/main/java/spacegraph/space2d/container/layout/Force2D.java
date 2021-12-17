package spacegraph.space2d.container.layout;

import jcog.Util;
import jcog.math.v2;
import jcog.random.XoRoShiRo128PlusRandom;
import jcog.signal.FloatRange;
import jcog.tree.rtree.Spatialization;
import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.container.graph.Graph2D;
import spacegraph.space2d.container.graph.NodeVis;
import spacegraph.util.MutableRectFloat;

import java.util.Random;

import static jcog.Util.fma;

public class Force2D<X> extends DynamicLayout2D<X> {

    final Random rng = new XoRoShiRo128PlusRandom(1);

    public final FloatRange repelSpeed = new FloatRange(0.02f, 0, 0.3f);

    public final FloatRange attractSpeed = new FloatRange(1.0f, 0, 2.0f);

    /** in (visible) graph radii */
    public final FloatRange scaleFactor = new FloatRange(1.0f, 0.02f, 2.0f);

    public final FloatRange scaleExponent = new FloatRange(1.0f, 0.1f, 2.0f);

    public final FloatRange scaleMin = new FloatRange(0.1f, 0.02f, 2.0f);

    /** in node radii */
    public final FloatRange nodeSpacing  = new FloatRange(0.01f, 0.0f, 8.0f);

    /** 1.0 - momentum LERP */
    public final FloatRange speed = new FloatRange(0.2f, 0.0f, 1.0f);

    int iterations = 1;

    private float maxRepelDist;

    private float equilibriumDistFactor;
    protected transient double[] dxy;
    protected transient int n;
    protected transient MutableRectFloat<X>[] nn;
    protected transient Graph2D<X> graph;

    @Override
    public void init(NodeVis<X> newNode, Graph2D<X> g) {
        float rx = g.w()/2*(rng.nextFloat()*2-1), ry = g.h()/2*(rng.nextFloat()*2-1);
        newNode.posXYWH(g.cx() + rx, g.cy() + ry, 1, 1);
    }

    @Override
    protected void layout(Graph2D<X> g, float dtS) {

        this.graph = g;

        int n = this.n = nodes.size();
        int iterations = this.iterations;
        if (n == 0 || iterations <= 0)
            return;


        float sqrtN = Util.sqrt(1.0f + n);
        float s = scaleFactor.floatValue();

        float gRadius = g.radius();


        maxRepelDist = 2 * gRadius; //estimate

        equilibriumDistFactor = s * nodeSpacing.floatValue();
        double scale = gRadius * s / sqrtN;

        float repelSpeed = (float) (this.repelSpeed.doubleValue() / iterations * scale);
        float attractSpeed = (float) (this.attractSpeed.doubleValue() / iterations * scale);

//        float maxSpeedPerIter = (nodeSpeedMax.floatValue() * dtS) * gRad / iterations;

        float speed = this.speed.floatValue();

        RectF gg = g.bounds;

        nn = nodes.array();

        double[] dxy = this.dxy;
        if (dxy == null || dxy.length<n*2) {
            dxy = this.dxy = new double[n * 2];
        }

        float min = (float) (scaleMin.floatValue() * scale), exponent = scaleExponent.floatValue();
        int idxy = 0;
        for (int a = 0; a < n; a++) {
            set(nn[a], min, scale, exponent);
            dxy[idxy++] = dxy[idxy++] = 0;
        }

        for (int i = 0; i < iterations; i++) {

            pre();

            repel(repelSpeed);

            attract(attractSpeed);

            post();

            apply(speed, gg);
        }

        this.graph = null; //release
    }

    /** before iteration */
    protected void pre() {
    }

    /** after iteration */
    protected void post() {
    }

    private void apply(float speed, RectF gg) {
        int n = this.n;
        MutableRectFloat<X>[] nn = this.nn;
        int idxy;
        idxy = 0;
        for (int a = 0; a < n; a++) {
            MutableRectFloat<X> A = nn[a];
            A.move(dxy[idxy++], dxy[idxy++]);

//                a.move(rng.nextFloat()*0.001f, rng.nextFloat()*0.001f); //jitter
            A.clamp(gg);
            A.commitLerp(speed);
        }
    }

    private void attract(float attractSpeed) {
        int n = this.n;
        MutableRectFloat<X>[] nn = this.nn;
        for (int a = 0; a < n; a++)
            attract(a, nn[a], attractSpeed);
    }

    private void repel(float repelSpeed) {
        int n = this.n;
        MutableRectFloat<X>[] nn = this.nn;
        for (int a = 0; a < n; a++) {
            MutableRectFloat<X> A = nn[a];
            float ar = A.radius()/2;
            for (int y = a + 1; y < n; y++)
                repel(a, A, ar, y, nn[y], repelSpeed);
        }
    }

    protected void set(MutableRectFloat<X> m, double min, double scale, double exponent) {
        float p = (float) (min + Math.pow((float) (Math.sqrt(m.node.pri)) * scale, exponent));
        m.size(p, p);

    }


    private final transient v2 delta = new v2();

    /**
     * HACK this reads the positions from the nodevis not the rectangle
     */
    private void attract(int a, MutableRectFloat A, float attractSpeed) {

        NodeVis<X> from = A.node;
        float px = A.x, py = A.y;

        float aRad = A.radius();

        from.outs.forEachValue(ab -> {
//            if (edge == null)
//                return; //wtf

            NodeVis<X> bv = ab.to;
            if (bv == null)
                return;

            MutableRectFloat B = bv.m;

            float abRad = aRad + B.radius();
            float lenIdeal = abRad * (1 + equilibriumDistFactor);

            delta.set(B.x - px, B.y - py);
            float len = delta.normalize();
            if (len > lenIdeal) {
                float s = attractSpeed * weightToVelocity(ab.weight) / 2;
                s = Util.min(len - lenIdeal, s);
                float dx = delta.x;
                float dy = delta.y;

                move(a, dx, dy, s);
//                A.move(abx, aby, s);

                //TODO
//                move(b, abx, aby, -s);
                B.move(dx, dy, -s);
            }
        });

    }

    private static float weightToVelocity(float weight) {
        //return 1;
        return weight;
        //return weight * weight;
    }

    private void repel(int a, MutableRectFloat A, float ar, int b, MutableRectFloat B, float repelSpeed) {

        double len = delta.set(A.x - B.x, A.y - B.y).normalize();
        if (len >= maxRepelDist)
            return;

        float br = B.radius()/2;
        double radSum = (ar + br);

//        if (len <= radSum) {
//            //collision, apply random vector
//            double theta = (float) (rng.nextFloat()*Math.PI*2);
//            dx = (float) Math.cos(theta);
//            dy = (float) Math.sin(theta);
//        }

        double ideal = radSum * (1 + equilibriumDistFactor);
        len -= ideal;

        double s = repelSpeed;
        if (len > 0) s /= (1 + (len * len))/radSum;

        if (s > Spatialization.EPSILONf) {
            double dx = delta.x, dy = delta.y;
            dx*=s; dy*=s;
            move(a, dx, dy, br);
            move(b, dx, dy, -ar);
        }

    }

    private void move(int a, double dx, double dy, double scale) {
        int aa = a * 2;
        double[] dxy = this.dxy;
        dxy[aa]   = fma(scale, dx, dxy[aa]);
        dxy[aa+1] = fma(scale, dy, dxy[aa+1]);
    }

}