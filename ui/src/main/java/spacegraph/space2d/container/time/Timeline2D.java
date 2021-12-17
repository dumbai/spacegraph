package spacegraph.space2d.container.time;

import com.jogamp.opengl.GL2;
import jcog.Util;
import jcog.data.map.CompactArrayMap;
import jcog.math.LongInterval;
import jcog.math.v2;
import jcog.tree.rtree.Spatialization;
import spacegraph.input.finger.Finger;
import spacegraph.input.finger.state.SurfaceDragging;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.container.Splitting;
import spacegraph.space2d.container.Stacking;
import spacegraph.space2d.container.graph.Graph2D;
import spacegraph.space2d.container.graph.NodeVis;
import spacegraph.space2d.container.unit.Clipped;
import spacegraph.space2d.widget.slider.FloatSlider;
import spacegraph.space2d.widget.slider.SliderModel;
import spacegraph.video.Draw;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static jcog.Util.fma;

/** view for one or more TimeRangeAware implementing surfaces that display aspects of a time-varying signal */
public class Timeline2D extends Stacking {

    /**
     * viewable range
     */
    public long start;
    public long end;
    private long startNext;
    private long endNext;


    public interface TimeRangeAware {
        void setTime(long tStart, long tEnd);
    }

    public Timeline2D() {
        this(0,1);
    }

    public Timeline2D(long start, long end) {
        this.setTime(start, end);
    }


//
//    @Override
//    protected void _add(Surface x) {
//        super._add(x);
//        setLayerTime(x, start, end);
//    }

    /** TODO move to Timeline2D */
    public boolean timeShiftPct(float pct) {
        if (Util.equals(pct, 0))
            return false;

//        synchronized (this) {
            long width = endNext - startNext;
//        int N = buffer.capacity();
//        if (width < N) {
            double mid = ((startNext + endNext) / 2.0);

        float n = pct * width;

        if (Math.abs(n) < 1) {
            if (pct > 0.05f) {
                n = Math.signum(n) > 0 ? 1 : -1; //for scales close to integer units, move at least one when significant mouse movement
            } else
                return false;
        }

        double nextMid = mid + n;

            double first = nextMid - width / 2.0;
            double last = nextMid + width / 2.0;
//            if (first < 0) {
//                first = 0;
//                last = first + width;
//            } else if (last > N) {
//                last = N;
//                first = last -width;
//            }

            setTime(first, last);
//        }
//        }
        return true;
    }

    /** TODO move to Timeline2D */
    public void scale(float pct) {
        if (Util.equals(pct, 1))
            return;

        synchronized (this) {
            double first = this.startNext, last = this.endNext;
            double width = last - first;
            double mid = (last + first) / 2;
            double viewNext = width * pct;

            first = mid - viewNext / 2;
            last = mid + viewNext / 2;
            if (last > 1) {
                last = 1;
                first = last - viewNext;
            }
            if (first < 0) {
                first = 0;
                last = first + viewNext;
            }

            setTime(first, last);
        }
    }
    public Surface withControls() {
        return new Splitting(new Clipped(this), 0.07f, controls());
    }

    public Bordering controls() {
        Bordering b = new Bordering();

        float sticking = 0; //0.05f;
        double tEpsilon = Spatialization.EPSILON;
        double speed = 0.1;

        FloatSlider whenSlider = new FloatSlider(0.5f, 0, 1) {


            @Override
            public boolean canRender(ReSurface r) {
                float v = this.get();
                float d = (v - 0.5f) * 2;
                double delta = d * (end - start) * speed;

                if (Math.abs(d) > tEpsilon) {
                    timeShift(delta);
                    set(Util.lerp(0.5f + sticking/2, v, 0.5f));
                }

                return super.canRender(r);
            }

//            @Override
//            public String text() {
//                return "";
//            }
        }.type(SliderModel.KnobHoriz);

        b.center(whenSlider);

        FloatSlider zoomSlider = new FloatSlider(0.5f, 0.48f, 0.52f) {
            @Override
            public boolean canRender(ReSurface r) {
                float v = this.get();
//                timeScale((v + 0.5f));
//                set(Util.lerp(0.5f + sticking/2, v, 0.5f));

                return super.canRender(r);
            }

//            @Override
//            public String text() {
//                return "";
//            }
        }.type(SliderModel.KnobVert);
        b.borderSize(Bordering.E, 0.2f).east(zoomSlider);

        return b;
    }

    private final SurfaceDragging drag = new SurfaceDragging(this, 0) {

        private v2 sr;

        @Override
        protected boolean starting(Finger f) {
            sr = f.posRelative(Timeline2D.this);
            return super.starting(f);
        }

        @Override
        protected boolean drag(Finger f) {
            v2 d = f.posRelative(Timeline2D.this);
            var e = d.subClone(sr);

            boolean chg;
            if (Math.abs(e.x) >= Math.abs(e.y))
                chg = timeShiftPct(e.x);
            else
                chg = timeScale(1 + e.y);

            if (chg)
                sr.set(d);

            return true;
        }

    };

    @Override
    public Surface finger(Finger f) {
        Surface s = f.test(drag) ? this : super.finger(f);
//        if (s == null) {
//            float r = f.rotationY(true);
//            if (r != 0) {
//                timeScale(1+r/3f);
//            }
//        }
        return s;
    }


//    public float x(long sample) {
//        return (float) ((sample - start) / ((end - start) * (double)w()));
//    }

    public Timeline2D timeShift(double dt) {
        if (Util.equals(dt, 0))
            return this;


        return setTime(startNext + dt, endNext + dt);
    }

    public boolean timeScale(double dPct) {

        if (Util.equals(dPct, 1))
            return false;

        synchronized (this) {
//            if (endNext - startNext <= 1f /(dPct-1)) {
//                if (dPct > 1) dPct = 2;
//            }

            double range = (endNext - startNext) * dPct;
            //System.out.println(n4(dPct) + " " + n2(range));
            double tCenter = (endNext + startNext) / 2.0;
            setTime(tCenter - range / 2, tCenter + range / 2);
            return true; //HACK TODO false cases
        }

    }

    /**
     * keeps current range
     */
    public Timeline2D setTime(double when) {
        synchronized (this) {
            double range = (endNext - startNext)/2.0;
            assert (range > 0);
            return setTime(when - range, when + range);
        }
    }

    @Override
    protected void renderContent(ReSurface r) {
        _setTime();

        super.renderContent(r);
    }

    public Timeline2D setTime(double start, double end) {
        return setTime(Math.round(start), Math.round(end));
    }

    public Timeline2D setTime(long start, long end) {
        if (end < start) {
            long x = end;
            end = start;
            start = x;
        }
        if (this.startNext!=start || this.endNext!=end) {
            synchronized (this) {
                if (this.startNext != start || this.endNext != end) {
                    this.startNext = start;
                    this.endNext = end;
                    layout();
                }
            }
        }
        return this;
    }

    private void _setTime() {
//        synchronized (this) {
            if (this.start!=startNext || this.end!=endNext) {
                this.start = startNext;
                this.end = endNext;
                forEach(this::setLayerTime);
            }
//        }
    }

    private void setLayerTime(Surface x) {
        if (x instanceof TimeRangeAware)
            ((TimeRangeAware) x).setTime(start, end);
    }

    /**
     * projects time to a position axis of given length
     */
    public static float x(long when, float X, float W, long s, long e) {
        return (float) fma(W, ((when - s) / ((double)(e - s))), X);
    }

    public <X> Timeline2D addEvents(EventBuffer<X> e, Consumer<NodeVis<X>> r, Graph2D.Graph2DUpdater<X> u) {
        add(new Timeline2DEvents<>(e, r, u));
        return this;
    }

    /** model for discrete events to be materialized on the timeline */
    public interface EventBuffer<X> {
        /**
         * any events intersecting with the provided range
         */
        Iterable<X> events(long start, long end);

        long[] range(X event);
//        @Nullable X first();
//        @Nullable X last();

        default boolean intersects(X x, long start, long end) {
            long[] r = range(x);
            return LongInterval.intersects(r[0], r[1], start, end);
        }

        default int compareStart(X x, X y) {
            long rx = range(x)[0];
            long ry = range(y)[0];
            return Long.compare(rx, ry);
        }

        default int compareDur(X x, X y) {
            long[] rx = range(x);
            long[] ry = range(y);
            return compareDur(rx, ry);
        }

        static int compareDur(long[] rx, long[] ry) {
            return Long.compare(rx[1] - rx[0], ry[1] - ry[0]);
        }

        default long intersectLength(X x, X y) {
            long[] rx = range(x);
            long[] ry = range(y);
            return LongInterval.intersectLength(rx[0], rx[1], ry[0], ry[1]);
        }

        default int compareDurThenStart(X x, X y) {
            if (x.equals(y)) return 0;

            long[] rx = range(x);
            long[] ry = range(y);
            int wc = -compareDur(rx, ry);
            if (wc != 0)
                return wc;
            int xc = Long.compare(rx[0], ry[0]);
            if (xc != 0)
                return xc;

            return x instanceof Comparable ? ((Comparable) x).compareTo(y) : Integer.compare(System.identityHashCode(x), System.identityHashCode(y));
        }

    }
    public static class SimpleEvent<X> implements Comparable<SimpleEvent<X>> {
        public final X name;
        final long start;
        public final long end;

        public SimpleEvent(X name, long start, long end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return name + "[" + start + ((start == end) ? "]" : (end + "]"));
        }

        @Override
        public int compareTo(SimpleEvent x) {
            if (this == x) return 0;
            int s = Long.compare(start, x.start);
            if (s != 0) return s;
            s = Long.compare(end, x.end);
            if (s != 0) return s;
            return Integer.compare(System.identityHashCode(this), System.identityHashCode(x));
        }

        public long range() {
            return end - start;
        }
    }

    public static class SimpleEventBuffer extends ConcurrentSkipListSet<SimpleEvent> implements EventBuffer<SimpleEvent> {

        @Override
        public Iterable<SimpleEvent> events(long start, long end) {

            return this.stream().filter(x -> intersects(x, start, end))::iterator;
        }

        @Override
        public boolean intersects(SimpleEvent simpleEvent, long start, long end) {
            return LongInterval.intersects(simpleEvent.start, simpleEvent.end, start, end);
        }

        @Override
        public long[] range(SimpleEvent event) {
            return new long[]{event.start, event.end};
        }
    }

    public static class FixedSizeEventBuffer<E extends SimpleEvent> extends ConcurrentSkipListSet<E> implements EventBuffer<E> {

        private final int cap;

        public FixedSizeEventBuffer(int cap) {
            this.cap = cap;
        }

        @Override
        public boolean add(E simpleEvent) {
            if (super.add(simpleEvent)) {
                while (size() > cap) {
                    pollFirst();
                }
                return true;
            }
            return false;
        }

        @Override
        public Iterable<E> events(long start, long end) {

            List<E> list = this.stream().filter(x -> intersects(x, start, end)).collect(Collectors.toList());
            return list;
        }

        @Override
        public boolean intersects(E simpleEvent, long start, long end) {
            return LongInterval.intersects(simpleEvent.start, simpleEvent.end, start, end);
        }

        @Override
        public long[] range(E event) {
            return new long[]{event.start, event.end};
        }
    }


    public static class TimelineGrid extends Surface implements TimeRangeAware {

        int THICKNESS = 2;
        //int DIVISIONS = 10; //TODO

        long start, end;
        private BiConsumer<GL2, ReSurface> paintGrid;

        @Override
        public void setTime(long tStart, long tEnd) {
            this.start = tStart; this.end = tEnd;
            paintGrid = null; //invalidate
        }

        @Override
        protected void render(ReSurface r) {
            if (paintGrid == null) {
                double range = end-start;
                double interval = interval(range);
                double phase = start % interval;
                double iMax = (range / interval) + 0.5f;
                paintGrid = (gl,sr)->{
                    float H = h(), W = w(), LEFT = x(), BOTTOM = y();
                    gl.glColor4f(0.3f,0.3f,0.3f,0.9f);

                    gl.glLineWidth(THICKNESS);
                    long x = Math.round(start - phase);
                    for (int i = 0; i <= iMax; i++) {
                        float xx = Timeline2D.x(x, LEFT, W, start, end);
                        Draw.linf(xx, BOTTOM, xx, BOTTOM + H, gl);
                        x += interval;
                    }
                };
            }
            paintGrid.accept(r.gl, r);
        }

        /** TODO refine */
        static double interval(double range) {
            double x = Math.pow(10.0, Math.floor(Math.log10(range)));
            if (range / (x / 2.0) >= 10)
                return x / 2.0;
            else if (range / (x / 5.0) >= 10)
                return x / 5.0;
            else
                return x / 10.0;
        }
    }

    public static class AnalyzedEvent<X,Y> extends SimpleEvent {

        final CompactArrayMap<X,Y> map = new CompactArrayMap();

        public AnalyzedEvent(Object x, long start, long end) {
            super(x, start, end);
        }

        public Y get(X key) {
            return map.get(key);
        }
        public void put(X key, Y value) {
            map.put(key, value);
        }
        public void forEach(BiConsumer<X,Y> each) {
            map.forEach(each);
        }


    }
}