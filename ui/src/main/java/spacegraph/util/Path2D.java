package spacegraph.util;

import com.jogamp.opengl.GL2;
import jcog.Util;
import jcog.math.v2;
import jcog.pri.Prioritized;
import jcog.tree.rtree.rect.RectF;
import org.eclipse.collections.api.block.procedure.primitive.FloatFloatProcedure;
import org.eclipse.collections.impl.list.mutable.primitive.FloatArrayList;

import static spacegraph.util.math.Simplify2D.collinearBayazit;
import static spacegraph.util.math.Simplify2D.collinearity;


/**
 * pairs of x,y coordiates stored in linear array
 * <p>
 * useful for iterative trajectory construction and compact storage
 */
public class Path2D extends FloatArrayList {

    public Path2D() {
        super();
    }

    public Path2D(int capacity) {
        super(capacity * 2);
    }

    /**
     * points are preallocated, useful for direct array access
     */
    public Path2D(int capacity, int size) {
        this(capacity);
        this.size = size * 2;
    }

    public void add(float x, float y) {
        addAll(x, y);
    }

    private void add(v2 p) {
        addAll(p.x, p.y);
    }


    /**
     * adds the point if the total size is below the maxPoints limit,
     * otherwise it simplifies the current set of points and sets the
     * end point to the specified value
     */
    public boolean add(v2 p, int maxPoints) {

        assert (maxPoints > 3);

        synchronized (this) {
            int s = this.size;
            if (s > 0) {
                //quick test for equality with last point
                if (Util.equals(items[s-2], p.x, Prioritized.EPSILON) && Util.equals(items[s-1], p.y, Prioritized.EPSILON))
                    return false;
            }

            add(p);

            if (points() > maxPoints)
                collinearSimplifyNext();

            return true;
        }
    }

    private void collinearSimplifyNext() {
        int n = points();
        int worst = -1;
        double minC = Double.POSITIVE_INFINITY;
        for (int i = 1; i < n - 1; i++) {
            int prevId = i - 1;
            if (prevId < 0) prevId = n - 1;
            int nextId = i + 1;
            if (nextId >= n) nextId = 0;

            {

                v2 prev = point(prevId);
                v2 current = point(i);
                v2 next = point(nextId);

                double c = collinearity(prev, current, next);
                if (c < minC) {
                    worst = i;
                    minC = c;
                }
            }
        }
        assert (worst != -1);
        removeAtIndex(worst * 2);
        removeAtIndex(worst * 2);
    }

    void collinearSimplify(float collinearityTolerance, int maxPoints) {

        assert (maxPoints >= 3);

        int n = points();
        for (int i = 0; n > maxPoints && i < n; ) {
            int prevId = i - 1;
            if (prevId < 0) prevId = n - 1;
            int nextId = i + 1;
            if (nextId >= n) nextId = 0;

            v2 prev = point(prevId);
            v2 current = point(i);
            v2 next = point(nextId);

            if (i > 0 && i < n - 1 && collinearBayazit(prev, current, next, collinearityTolerance)) {
                removeAtIndex(i * 2);
                removeAtIndex(i * 2);
                n--;
            } else {
                i++;
            }
        }
    }

    public void setEnd(v2 p) {
        float[] ii = items;
        int s = size;
        ii[s - 2] = p.x;
        ii[s - 1] = p.y;
    }

    public void addAll(v2... pp) {
        ensureCapacity(size + pp.length * 2);
        for (v2 p : pp)
            addAll(p.x, p.y);
    }


    public v2 start() {

        return point(0);
    }

    private v2 point(int i) {
        float[] ii = items;
        return new v2(ii[i * 2], ii[i * 2 + 1]);
    }

    public v2 end() {
        int s = size;
        assert (s > 0);
        float[] ii = items;
        return new v2(ii[s - 2], ii[s - 1]);
    }


    private void forEach(FloatFloatProcedure each) {
        int s = size;
        for (int i = 0; i < s; ) {
            each.value(items[i++], items[i++]);
        }
    }

    public void vertex2f(GL2 gl) {
        forEach(gl::glVertex2f);
    }


    public int points() {
        return size / 2;
    }

    public final float[] array() {
        return items;
    }

    public RectF bounds() {
        float[] a = array();
        int n = points();
        if (n <= 2)
            return RectF.XYWH(a[0], a[1], 1, 1);

        float x1 = Float.POSITIVE_INFINITY, y1 = Float.POSITIVE_INFINITY, x2 = Float.NEGATIVE_INFINITY, y2 = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < n; ) {
            float x = a[i++], y = a[i++];
            if (x < x1) x1 = x;
            if (x > x2) x2 = x;
            if (y < y1) y1 = y;
            if (y > y2) y2 = y;
        }

        return RectF.XYXY(x1, y1, x2, y2);
    }
}
