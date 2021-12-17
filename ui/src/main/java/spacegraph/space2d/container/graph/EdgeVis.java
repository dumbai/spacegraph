package spacegraph.space2d.container.graph;

import com.jogamp.opengl.GL2;
import jcog.Util;
import jcog.tree.rtree.rect.RectF;
import spacegraph.video.Draw;

public class EdgeVis<X> {
    public NodeVis<X> to;

    public volatile boolean invalid;

    float r;
    float g;
    float b;
    float a;
    public float weight;
    private int renderer;

    public EdgeVis() {
        clear();
    }

    public void clear() {
        invalid = true;
        r = g = b = 0.0f;
        a = 0.75f;
        to = null;
        weight = 1.0f;
        renderer = EdgeVisRenderer.Triangle.ordinal();
    }



//        protected void merge(EdgeVis<X> x) {
//            weight += x.weight;
//            r = Util.or(r, x.r);
//            g = Util.or(g, x.g);
//            b = Util.or(b, x.b);
//            a = Util.or(a, x.a);
//        }

    enum EdgeVisRenderer {
        Line {
            @Override
            public void render(EdgeVis e, NodeVis from, GL2 gl) {
                float x = from.cx(), y = from.cy();
                gl.glLineWidth(1.0f + e.weight * 4.0f);
                e.color(gl);
                NodeVis to = e.to;
                Draw.linf(x, y, to.cx(), to.cy(), gl);
            }
        },
        Triangle {
            @Override
            public void render(EdgeVis e, NodeVis from, GL2 gl) {

                NodeVis to = e.to;
                if (to == null)
                    return;

                RectF f = from.bounds;
                RectF t = to.bounds;
                float scale = Math.min(f.w, f.h);
                float base = Util.lerpSafe(Util.sqrt(e.weight), scale / 4, scale);

                e.color(gl);
                Draw.halfTriEdge2D(f.cx(), f.cy(), t.cx(), t.cy(), base, gl);

            }
        };

        protected abstract void render(EdgeVis e, NodeVis from, GL2 gl);
    }

    private void color(GL2 gl) {
        gl.glColor4f(r, g, b, a);
    }


    public EdgeVis<X> weight(float w) {
        weight = w;
        return this;
    }

    public EdgeVis<X> weightAddLerp(float w, float rate) {
        this.weight = Util.lerpSafe(rate, this.weight, this.weight + w);
        return this;
    }
    public EdgeVis<X> weightLerp(float w, float rate) {
        this.weight = Util.lerpSafe(rate, this.weight, w);
        return this;
    }

    public EdgeVis<X> color(float r, float g, float b, float a) {
        color(r,g,b);
        this.a = a;
        return this;
    }

    public EdgeVis<X> color(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
        return this;
    }

    public EdgeVis<X> colorLerp(float r, float g, float b /* TODO type */, float rate) {
        if (r==r) this.r = Util.lerpSafe(rate, this.r, r);
        if (g==g) this.g = Util.lerpSafe(rate, this.g, g);
        if (b==b) this.b = Util.lerpSafe(rate, this.b, b);
        return this;
    }
    public EdgeVis<X> colorAddLerp(float r, float g, float b /* TODO type */, float rate) {
        if (r==r) this.r = Util.lerpSafe(rate, this.r, r + this.r);
        if (g==g) this.g = Util.lerpSafe(rate, this.g, g + this.g);
        if (b==b) this.b = Util.lerpSafe(rate, this.b, b + this.b);
        return this;
    }

    final void draw(NodeVis<X> from, GL2 gl) {
        NodeVis<X> t = this.to;
        if (t != null && t.visible())
            renderers[renderer].render(this, from, gl);
    }

    static final EdgeVisRenderer[] renderers = EdgeVisRenderer.values();
}