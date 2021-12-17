package spacegraph.space2d.container;

import jcog.TODO;
import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.collection.MutableArrayContainer;

/* 9-element subdivision


 */
public class Bordering<S extends Surface> extends MutableArrayContainer {
    public static final int C = 0;
    public static final int N = 1;
    public static final int S = 2;
    public static final int E = 3;
    public static final int W = 4;
    public static final int NE = 5;
    public static final int NW = 6;
    public static final int SW = 7;
    public static final int SE = 8;

    public static final float BORDER_SIZE_DEFAULT = 0.2f;

    /**
     * in percent of the half total size of the corresponding dimension
     */
    protected float borderWest = BORDER_SIZE_DEFAULT;
    protected float borderEast = BORDER_SIZE_DEFAULT;
    protected float borderSouth = BORDER_SIZE_DEFAULT;
    protected float borderNorth = BORDER_SIZE_DEFAULT;

    boolean autocollapse = true;

    public Bordering() {
        super(SE + 1);
    }

    public Bordering(S center) {
        this();
        center(center);
    }

    public final Bordering<S> set(S center) {
        set(C, center);
        return this;
    }

    /**
     * sets all edge sizes to a value
     * TODO layout only if changed
     */
    protected Bordering<S> borderSize(float size) {
        borderNorth = borderSouth = borderEast = borderWest = size;
        layout();
        return this;
    }

    /**
     * sets a specific edge size
     * TODO layout only if changed
     */
    public Bordering borderSize(int direction, float size) {
        switch (direction) {
            case N -> borderNorth = size;
            case S -> borderSouth = size;
            case E -> borderEast = size;
            case W -> borderWest = size;
            default -> throw new UnsupportedOperationException();
        }
        layout();
        return this;
    }

    @Override
    protected void doLayout(float dtS) {

        float X = x();
        float Y = y();
        float W = w();
        float H = h();

        boolean se = get(Bordering.SE) != null;
        boolean ne = get(Bordering.NE) != null;
        boolean sw = get(Bordering.SW) != null;
        boolean nw = get(Bordering.NW) != null;
        boolean ew = autocollapse && !(sw || nw || get(Bordering.W) != null);
        boolean ee = autocollapse && !(se || ne || get(Bordering.E) != null);
        boolean en = autocollapse && !(ne || nw || get(Bordering.N) != null);
        boolean es = autocollapse && !(se || sw || get(Bordering.S) != null);

        /* "half" width radii */
        float wRad, hRad;
        boolean aspectEqual = true;
        if (aspectEqual) {
            if (ee && ew) { hRad = H/2; wRad = 0; }
            else if (es && en) { wRad = W/2; hRad = 0; }
            else wRad = hRad = Math.min(W, H) / 2;
        } else {
            wRad = W / 2;
            hRad = H / 2;
        }

        float be;
        float bn;
        float bs;
        float bw;
        bw = ew ? 0 : this.borderWest * wRad;
        be = ee ? 0 : this.borderEast * wRad;
        bn = en ? 0 : this.borderNorth * hRad;
        bs = es ? 0 : this.borderSouth * hRad;

        for (int i = 0, childrenLength = 9; i < childrenLength; i++) {

            Surface c = get(i);

            if (c == null || !c.visible())
                continue;

            float x1, y1, x2, y2;

            switch (i) {
                case C -> {
                    x1 = bw;
                    y1 = bs;
                    x2 = W - be;
                    y2 = H - bn;
                }
                case N -> {
                    x1 = bw;
                    y1 = H - bn;
                    x2 = W - be;
                    y2 = H;
                }
                case S -> {
                    x1 = bw;
                    y1 = 0;
                    x2 = W - be;
                    y2 = bs;
                }
                case Bordering.W -> {
                    x1 = 0;
                    y1 = bs;
                    x2 = bw;
                    y2 = H - bn;
                }
                case E -> {
                    x1 = W - be;
                    y1 = bs;
                    x2 = W;
                    y2 = H - bn;
                }
                case NE -> {
                    x1 = W - be;
                    y1 = H - bn;
                    x2 = W;
                    y2 = H;
                }
                case NW -> {
                    x1 = 0;
                    y1 = H - bn;
                    x2 = bw;
                    y2 = H;
                }
                case SW -> {
                    x1 = 0;
                    x2 = bw;
                    y1 = 0;
                    y2 = bs;
                }
                case SE -> {
                    x1 = W - be;
                    x2 = W;
                    y1 = 0;
                    y2 = bs;
                }
                default -> throw new TODO();
            }

            c.pos(RectF.XYXY(X + x1, Y + y1, X + x2, Y + y2));
        }

    }


    /**
     * replace center content
     */
    public Bordering center(S next) {
        set(C, next);
        return this;
    }

    public Surface center() {
        return get(C);
    }

    public Bordering set(int direction, S next, float borderSizePct) {
        borderSize(direction, borderSizePct);
        return set(direction, next);
    }

    public Bordering set(int direction, S next) {
        if (direction >= 9)
            throw new ArrayIndexOutOfBoundsException();

        setAt(direction, next);
        return this;
    }

    public final Bordering north(S x) {
        return set(Bordering.N, x);
    }
    public final Bordering south(S x) {
        return set(Bordering.S, x);
    }
    public final Bordering east(S x) {
        return set(Bordering.E, x);
    }
    public final Bordering west(S x) { return set(Bordering.W, x); }
    public final Bordering northwest(S x) {
        return set(Bordering.NW, x);
    }
    public final Bordering northeast(S x) {
        return set(Bordering.NE, x);
    }
    public final Bordering southwest(S x) {
        return set(Bordering.SW, x);
    }
    public final Bordering southeast(S x) {
        return set(Bordering.SE, x);
    }

    public final Surface east() { return get(Bordering.E); }
    public final Surface west() { return get(Bordering.W); }
    public final Surface north() { return get(Bordering.N); }
    public final Surface south() { return get(Bordering.S); }
    public final Surface northeast() { return get(Bordering.NE); }
    public final Surface northwest() { return get(Bordering.NW); }
    public final Surface southeast() { return get(Bordering.SE); }
    public final Surface southwest() { return get(Bordering.SW); }

}