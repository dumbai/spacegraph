package spacegraph.space2d.container;

import jcog.signal.IntRange;
import jcog.tree.rtree.rect.RectF;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class RingContainer<X extends Surface> extends EmptyContainer {

    protected X[] x;

    /** T time axis capacity (history) */
    private final IntRange T = new IntRange(1, 1, 512);

    public RingContainer(X[] initArray) {
        x = initArray;
        coords = new float[initArray.length * 4];
        t(initArray.length);
        layout();
    }

    @Override
    public int childrenCount() {
        return 1;
    }

    /** TODO abstract, and other visualization options (z-curve, etc) */
    @Deprecated protected boolean horizOrVert;


    /** current time */
    protected final AtomicInteger y = new AtomicInteger(0);


    float[] coords;

    protected abstract void reallocate(X[] x);

    /** prepares the next row for rewrite TODO make unsynchronized */
    public synchronized void next(Consumer<X> setter) {
        int t = this.T.intValue();
        int y = this.y.getAndIncrement();

        X[] x = this.x;
        if (x == null || x.length!=t) { //TODO if history changes
            x = this.x = Arrays.copyOf(x, t);
            this.coords = new float[t * 4];
            reallocate(x);
        } else if (x[0]==null)
            reallocate(x);

        setter.accept(x[y%t]);
        layout();
    }

    @Override
    protected void doLayout(float dtS) {

        int y = this.y.getOpaque();
        float[] c = coords;
        int t = T.intValue();

        float W = w(), H = h(), left = left(), right = right(), top = bottom(), bottom = top();
        float di = (horizOrVert ? W : H)/t;
        int j = 0;
        for (int i = 0; i < t; i++) {
            int ii = i;
//            Surface xyi = xy[i];
//            ii = (t - 1) - ii; //t-.. : for y-orientation HACK
            if (horizOrVert) {
                float ix = ii * di;
                c[j++] = left + ix; c[j++] = top; c[j++] = left + ix + di; c[j++] = bottom;
                //xyi.posSpectro(left, top + ix,  left + ix + di, bottom);
            } else {
                c[j++] = left;
                float iy = ii * di;
                c[j++] = top + iy; c[j++] = right; c[j++] = top+iy+di;
                //xyi.posSpectro(left,  top + iy, right, top + iy + di);
            }
        }
    }

    public void forEach(BiConsumer<X, RectF> each) {
        int j = 0;
        float[] c = this.coords;
        X[] xes = this.x;
        int t = T.intValue();
        int y = this.y.intValue();
        for (int i = 0, xesLength = xes.length; i < xesLength; i++) {
            X x = xes[(i + y) % t];
            if (x != null)
                each.accept(x, RectF.XYXY(c[j++], c[j++], c[j++], c[j++]));
            else
                j += 4;
        }
    }

    /** set the history length of the spectrogram */
    public void t(int t) {
        this.T.set(t);
    }

    @Override
    protected void renderContent(ReSurface r) {
        forEach((z, b)->{
            z.pos(b);
            z.renderIfVisible(r);
        });
    }


}