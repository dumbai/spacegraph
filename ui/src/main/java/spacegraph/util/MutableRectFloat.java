package spacegraph.util;

import jcog.Util;
import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.container.graph.NodeVis;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static jcog.Util.fma;

/**
 * similar to RectFloat2D with additional
 * except the x,y components are mutable
 *
 * the x and y correspond to the center of the float (unlke RectFloat which corresponds to bottom-left corner)
 */
public class MutableRectFloat<X> extends RectF {

    float nx;
    float ny;
    float nw;
    float nh;

    /**
     * TODO make a MutableFloatRect proxy and adapter class for transparently controlling a graph2d node
     */
    @Deprecated public final NodeVis<X> node;


    public MutableRectFloat() {
        super();
        this.node = null;
    }

    public void clear() {
        set(RectF.Unit);
    }

    public MutableRectFloat(NodeVis<X> n) {
        super();
        this.node = n;
        set(n.bounds);
    }

    public final MutableRectFloat setXYXY(float x1, float y1, float x2, float y2) {
      return setX0Y0WH(x1, y1, x2-x1, y2-y1);
    }

    public final MutableRectFloat setXYWH(float x, float y, float w, float h) {
        return setX0Y0WH(x - w/2, y - h/2, w, h);
    }

    public final void set(MutableRectFloat r) {
        super.set(r);
        _commit();
    }

    public final void set(RectF r) {
        super.set(r);
        _commit();
    }

    public void _commit() {
        nx = x;
        ny = y;
        nw = w;
        nh = h;
    }


    public final MutableRectFloat setX0Y0WH(float x, float y, float w, float h) {
        pos(x, y);
        size(w, h);
        return this;
    }


//    final boolean setIfChanged(MutableRectFloat r, float epsilon) {
//        if (equals(r, epsilon))
//            return false;
//        else {
//            set(r);
//            return true;
//        }
//    }


    public float radius() {
        //double ww = w/2, hh = h/2;
        return (float) Math.sqrt(Util.sqr(w) + Util.sqr(h))/2;
    }

    public MutableRectFloat pos(float x, float y) {
        this.nx = x;
        this.ny = y;
        return this;
    }

    public MutableRectFloat move(float dx, float dy) {
        this.nx += dx;
        this.ny += dy;
        return this;
    }
    public MutableRectFloat move(float dx, float dy, float speed) {
        this.nx = fma(dx, speed, this.nx);
        this.ny = fma(dy, speed, this.ny);
        return this;
    }


    private void _pos(float px, float py) { this.x = px; this.y = py; /* TODO inc version#? */ }
    private void _size(float pw, float ph) { this.w = pw; this.h = ph; /* TODO inc version#? */ }

    public void commitLerp(float rate) {
        _pos(x==x ? Util.lerpSafe(rate, x, nx) : nx, y == y ? Util.lerpSafe(rate, y, ny) : ny);
        _size(w == w  ? Util.lerpSafe(rate, w, nw) : nw, h == h ? Util.lerpSafe(rate, h, nh) : nh);

    }

//    public void commit(float speedLimit) {
//        v2 delta = new v2(x -cxPrev, y -cyPrev);
//        float lenSq = delta.lengthSquared();
//        if (lenSq > speedLimit * speedLimit) {
//            float len = (float) Math.sqrt(lenSq);
//            delta.scale(speedLimit / len);
//            x = cxPrev + delta.x; cxPrev = x;
//            y = cyPrev + delta.y; cyPrev = y;
//        }
//
//    }

    public MutableRectFloat move(double dx, double dy) {
        return move((float) dx, (float) dy);
    }


    public float aspectRatio() {
        return h / w;
    }


    /**
     * keeps this rectangle within the given bounds
     */
    public MutableRectFloat clamp(RectF bounds) {
        return (nx != nx) || (ny != ny) ? randomize(bounds) : pos(
            Util.clampSafe(nx, bounds.left() + nw, bounds.right() - nw), Util.clampSafe(ny, bounds.bottom() + nh, bounds.top() - nh));
    }

    private MutableRectFloat randomize(RectF bounds) {
        Random r = ThreadLocalRandom.current(); //HACK
        return pos(bounds.x + r.nextFloat() * bounds.w, bounds.y + r.nextFloat() * bounds.h);
    }

    public MutableRectFloat size(float w, float h) {
        this.nw = w;
        this.nh = h;
        return this;
    }
//
//    @Override
//    public String toString() {
//        return getClass().getSimpleName() + '{' +
//                "cx=" + x +
//                ", cy=" + y +
//                ", w=" + w +
//                ", h=" + h +
//                '}';
//    }




    public RectF immutable() {
        return RectF.XYWH(cx(), cy(), Math.abs(w), Math.abs(h)); //HACK
    }

    /** stretch to maximum bounding rectangle of this rect and the provided point */
    public MutableRectFloat<X> mbr(float px, float py) {

        boolean change = false;

        float x1 = left(), x2 = right();
        if (x1 > px) {
            x1 = px;
            change = true;
        }
        if (x2 < px) {
            x2 = px;
            change = true;
        }
        float y1 = bottom(), y2 = top();
        if (y1 > py) {
            y1 = py;
            change = true;
        }
        if (y2 < py) {
            y2 = py;
            change = true;
        }

        return change ? setXYXY(x1, y1, x2, y2) : this;
    }


    public RectF normalizeScale(float cx, float cy, float cw, float ch, float minVisibleDim, float sw, float sh) {

        MutableRectFloat<X> extent = this;
        float ew = extent.w ;
        float px = (cx - extent.left()) / ew;
        float eh = extent.h;
        float py = (cy - extent.bottom()) / eh;

        float pw = Math.max(minVisibleDim, cw / ew);
        float ph = Math.max(minVisibleDim, ch / eh);

        return RectF.XYWH(
                px * sw,
                py * sh,
                pw * sw,
                ph * sh
        );

    }

    public void posLERP(float tx, float ty, float rate) {
        nx = Util.lerpSafe(rate, nx, tx);
        ny = Util.lerpSafe(rate, ny, ty);
    }
}