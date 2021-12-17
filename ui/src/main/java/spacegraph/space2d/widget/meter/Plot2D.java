package spacegraph.space2d.widget.meter;

import com.jogamp.opengl.GL2;
import jcog.Util;
import jcog.data.list.Lst;
import jcog.data.list.MetalRing;
import jcog.event.Off;
import jcog.math.CumulativeSum;
import jcog.signal.tensor.TensorRing;
import org.eclipse.collections.api.block.procedure.primitive.FloatProcedure;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.container.Stacking;
import spacegraph.space2d.widget.text.BitmapLabel;
import spacegraph.video.Draw;
import spacegraph.video.font.HersheyFont;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.function.DoubleSupplier;

public class Plot2D extends Stacking {
//    public static final PlotVis Line = (series, minValue, maxValue, gl) -> plotLine(series, gl, minValue, maxValue, false, false);
//    public static final PlotVis LineLanes = (series, minValue, maxValue, gl) -> plotLine(series, gl, minValue, maxValue, true, false);
//    public static final PlotVis BarLanes = (series, minValue, maxValue, gl) -> plotLine(series, gl, minValue, maxValue, true, true);

    public final List<Series> series;
    private final PlotVis vis;

    private final int maxHistory;


    @Nullable
    private BitmapLabel titleLabel;
    private String title = "";
    private Off on;
    private volatile boolean invalid;
    private transient float minMinValue;
    private transient float maxMaxValue;

    public Plot2D(int history) {
        this(history, new PlotVis() {

            private FloatBuffer vertexBuffer;
            private final float[] backgroundColor = {0, 0, 0, 0.75f};

            @Override
            void draw(List<Series> series, float minValue, float maxValue, GL2 g) {
                g.glColor4fv(backgroundColor, 0);
                Draw.rect(g, 0, 0, 1, 1);

                plotLine(series, g, minValue, maxValue, false, false);
            }

            private void plotLine(List<Series> series, GL2 gl, float minValue, float maxValue, boolean lanes, boolean filled) {
                int n = series.size();





                if (minValue == maxValue) {
                    float center = minValue;
                    minValue = center - (center / 2);
                    maxValue = center + (center / 2);
                }

                gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

                gl.glLineWidth(2);

                float W = 1.0f;
                Draw.linf(0, 0, W, 0, gl);
                float H = 1.0f;
                Draw.linf(0, H, W, H, gl);

                HersheyFont.hersheyText(gl, Float.toString(minValue), 0.04f, 0, 0, 0, Draw.TextAlignment.Left);
                HersheyFont.hersheyText(gl, maxValue +
                        (n == 1 ? " max, " + (series.get(0).meanValue()) + " avg" : ""), 0.04f, 0, H, 0, Draw.TextAlignment.Left);

                float range = maxValue - minValue;
                if (n > 1 && lanes) {
                    float height = 1.0f / n;
                    for (int i = 0; i < n; i++) {
                        float top = ((float) i) / n;
                        lineplot(gl, minValue, W, range, top, height / 2, i, series.get(i));
                    }
                } else {
                    for (int i = 0; i < n; i++)
                        lineplot(gl, minValue, W, range, 0, 1, i, series.get(i));
                }
            }

            private void lineplot(GL2 gl, float minValue, float w, float range, float top, float height, int i, Series s) {
                //float mid = ypos((s.minValue() + s.maxValue()) / 2f, lanes, sn, seriesSize, maxValue-minValue, minValue);
//            float base = ypos(s.minValue(), lanes, i, seriesSize,
//                    maxValue-minValue, minValue);
                float textScale = height / 2;
                float y = top + height / 2;

                if (range > Float.MIN_NORMAL) {

                    int histSize = s.size(), histCap = s.capacity();


                    float[] color = s.color();
                    float r = color[0], g = color[1], b = color[2];


//                    gl.glBegin(GL.GL_LINE_STRIP);


                    if (vertexBuffer==null) {
                        ByteBuffer bb = ByteBuffer.allocateDirect(histCap * 3 * Float.BYTES);
                        bb.order(ByteOrder.nativeOrder());
                        vertexBuffer = bb.asFloatBuffer();
                    }


                    //float a = -1;
                    float dx = (w / histCap);
                    float x0 = (histCap - histSize) * dx;


                    //TODO use VBO or something
                    for (int j = 0; j < histSize; j++) {

                        float v = s.get(j);
                        if (v == v) {

                            y = jcog.Util.fma(ypos(v, minValue, range), height, top);

//                            float nextA = Util.lerpSafe(((v - minValue) / range), 0.5f, 0.95f);
//                            if (!Util.equals(a, nextA, 1 / 256.0f))
//                                gl.glColor4f(r, g, b, a = nextA);

                            //gl.glVertex2f(x, y);
                            vertexBuffer.put(x0 + j * dx); vertexBuffer.put(y);
                        }
                    }

                    int vbSize = vertexBuffer.position()/2;
                    vertexBuffer.rewind();


                    gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
                    gl.glVertexPointer(2, GL2.GL_FLOAT, 0, vertexBuffer);

                    gl.glLineWidth(3);
                    gl.glColor3f(r, g, b);
                    gl.glDrawArrays(GL2.GL_LINE_STRIP, 0, vbSize);

                    gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

                    //draw mean line
                    float mean = s.meanValue();
                    if (mean == mean) {
                        gl.glColor4f(color[0], color[1], color[2], 0.5f);
                        float yMean = jcog.Util.fma(ypos(mean, minValue, range), height, top);
                        float t = 0.02f;// / s.size();
                        Draw.rect(0, yMean - height * t / 2, w, height * t / 2, gl);
                    }

                }


                gl.glColor3f(1, 1, 1);

                gl.glLineWidth(2);

                HersheyFont.hersheyText(gl, s.name(), 0.04f * textScale, w, y, 0, Draw.TextAlignment.Right);


            }

        });
    }


    public Plot2D(int history, PlotVis vis) {
        super();

        this.vis = vis;
        this.series = new Lst();
        this.maxHistory = history;
    }



    private static float ypos(float v, boolean lanes, int sn, int seriesSize, float range, float minValue) {
        return lanes ? ypos(minValue, range, v, sn, seriesSize) : ypos(v, minValue, range);
    }

    private static float ypos(float v, float minValue, float range) {
        return (v - minValue) / range;
    }

    private static float ypos(float minValue, float range, float v, int lane, int numLanes) {
        return (v == v ? ((v - minValue) / range) : (0.5f)) / numLanes + (((float) lane) / numLanes);
    }

    public static CumulativeSum mean(DoubleSupplier o, int interval) {
        return new CumulativeSum(o, interval);
    }

    protected static AbstractSeries newSeries(String name, float[] data) {
        //return new ArraySeries(name, data);
        return new RingTensorSeries(name, data);
    }

//    public Plot2D on(Function<Runnable, Off> trigger) {
//        synchronized (series) {
//            if (on != null)
//                on.close();
//            this.on = trigger.apply(this::commit);
//        }
//        return this;
//    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Plot2D add(Series s) {
        series.add(s);
        invalid = true;
        return this;
    }

    public Plot2D add(String name, float[] data) {
        return add(newSeries(name, data).autorange());
    }
//
//    public Plot2D add(String name, float[] data, float min, float max) {
//        return add(newSeries(name, data).range(min, max));
//    }


//    public static final PlotVis BarWave = (List<Series> series, GL2 g, float minValue, float maxValue) -> {
//        if (minValue != maxValue) {
//
//            float w = 1.0f;
//            float h = 1.0f;
//
//
//            for (Series s : series) {
//                int histSize = s.size();
//
//                float dx = (w / histSize);
//
//                float x = 0;
//                float prevX = 0;
//
//                float[] ss = s.toArray();
//                int len = Math.min(s.size(), ss.length);
//                float range = maxValue - minValue;
//                for (int i = 0; i < len; i++) {
//                    float v = ss[i];
//
//                    float py = (v - minValue) / range;
//                    if (py < 0) py = 0;
//                    if (py > 1.0) py = 1.0f;
//
//                    float y = py * h;
//
//                    g.glColor4fv(s.color(), 0);
//
//                    Draw.rect(g, prevX, h / 2.0f - y / 2f, dx, y);
//
//                    prevX = x;
//                    x += dx;
//                }
//
//            }
//        }
//    };

    public Plot2D add(String name, DoubleSupplier valueFunc, float min, float max) {
        add(new RingTensorSeries(name, maxHistory) {

            @Override
            public void update() {
                add(Util.clampSafe(valueFunc.getAsDouble(), min, max));
                autorange(); //for mean
                this.minValue = min; this.maxValue = max; //HACK override min/max
            }


        });

        return this;
    }

// TODO
//    float _minValue = NaN, _maxValue = NaN;
//    String minValueStr = "", maxValueStr = "";

    public Plot2D add(String name, DoubleSupplier valueFunc) {
        add(new RingTensorSeries(name, maxHistory) {
            @Override
            public void update() {
                add((float) valueFunc.getAsDouble());
                autorange();
            }
        });
        return this;
    }

    /**
     * TODO use a FloatRingBuffer or something non-Box
     */
    @Deprecated
    public Plot2D add(String name, MetalRing<Float> buffer) {
        add(new RingTensorSeries(name, maxHistory) {
            @Override
            public void update() {
                buffer.clear(this::add);
                autorange();
            }
        });
        return this;
    }

    @Override
    protected void renderContent(ReSurface r) {
        String s = this.title;
        if (s.isEmpty()) {
            if (titleLabel != null) {
                remove(titleLabel);
                titleLabel = null;
            }
        } else {
            if (titleLabel == null) {
                titleLabel = new BitmapLabel(s);
                add(titleLabel);
            } else
                titleLabel.text(s);
        }

        Draw.bounds(bounds, r.gl, this::paintUnit);
        super.renderContent(r);
    }

    private void paintUnit(GL2 gl) {

        if (invalid) {
            update();
            invalid = false;
        }

        List<Series> series = this.series;
        if (series.isEmpty())
            return;

        vis.draw(series, minMinValue, maxMaxValue, gl);
    }



    public void update() {
        synchronized (series) {

            float minValue = Float.POSITIVE_INFINITY;
            float maxValue = Float.NEGATIVE_INFINITY;
            for (Series s : series) {
                s.update();

                float min = s.minValue();
                if (Float.isFinite(min)) {
                    float max = s.maxValue();
                    if (Float.isFinite(max)) {
                        minValue = Util.min(minValue, min);
                        maxValue = Util.max(maxValue, max);
                    }
                }
            }

            if (Float.isFinite(minValue) && Float.isFinite(maxValue)) {
                this.minMinValue = minValue;
                this.maxMaxValue = maxValue;
//                vis.update();
            }
//            } else
//                throw new WTF();

        }
    }

    public interface Series {

        String name();

        void update();

        float maxValue();

        float minValue();

        float meanValue();

        float[] color();

        int capacity();

        int size();

        float get(int i);

        void clear();


        //void forEach(IntFloatConsumer value);
    }

    //
//    public static class ArraySeries extends AbstractSeries {
//
//        protected final MyFloatArrayList data;
//
//        @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
//        public ArraySeries(String name, int capacity) {
//            data = new MyFloatArrayList(capacity);
//            setName(name);
//            this.capacity = capacity;
//        }
//
//        public ArraySeries(String name, float[] data) {
//            this.data = new MyFloatArrayList(data);
//            setName(name);
//            capacity = data.length;
//        }
//
//        @Override
//        public float get(int i) {
//            return data.get(i);
//        }
//
//        @Override
//        public void clear() {
//            data.clear();
//        }
//
//        @Override
//        public int size() {
//            return data.size();
//        }
//
//        @Override
//        public void forEach(FloatProcedure f) {
//            data.forEach(f);
//        }
//
//        @Override
//        void limit() {
//            int over = data.size() - (ArraySeries.this.capacity - 1);
//            for (int i = 0; i < over; i++)
//                data.removeAtIndex(0);
//        }
//
//        private static final class MyFloatArrayList extends FloatArrayList {
//            public MyFloatArrayList(int initialCapacity) {
//                super(initialCapacity);
//            }
//
//            public MyFloatArrayList(float... array) {
//                super(array);
//            }
//
//            @Override
//            public float[] toArray() {
//                return items;
//            }
//
//            @Override
//            public float get(int index) {
//                float[] ii = this.items;
//                //HACK
//                return ii.length > index ? ii[index] : Float.NaN;
//            }
//        }
//    }
    public static class RingTensorSeries extends AbstractSeries {

        private final TensorRing data;

        public RingTensorSeries(String name, int capacity) {
            data = new TensorRing(1, capacity);
            setName(name);
            clear();
            this.capacity = capacity;
        }

        public RingTensorSeries(String name, float[] data) {
            this(name, data.length);
            for (float f : data)
                add(f); //TODO bulk data.setSpin(data)
        }

        public void add(double v) {
            if (v == v)
                data.setSpin((float) v);
        }

        @Override
        public float get(int i) {
            return data.getAt(capacity - 1 - i);
        } //HACK

        @Override
        public void clear() {
            data.fillAll(Float.NaN);
        }

        @Override
        public int size() {
            return capacity;
        }

        @Override
        public void forEach(FloatProcedure f) {
            data.forEach(f);
        }

//        @Override
//        void limit() {
//            //N/A
//        }
    }

    public abstract static class AbstractSeries implements Series {
        private final float[] color = {1, 1, 1, 0.75f};
        private final AutoRange autoRange = new AutoRange();
        protected int capacity;
        protected transient float minValue;
        protected transient float maxValue;
        protected transient float meanValue;
        private String name;

        @Override
        public abstract float get(int i);

        @Override
        public abstract void clear();

        protected void setName(String name) {
            AbstractSeries.this.name = name;
            Draw.colorHash(name, color());
        }

        @Override
        public void update() {

        }

        @Override
        public abstract int size();

        public Series autorange() {

            forEach(autoRange.restart());

            int n = autoRange.count;
            if (n == 0) {
                range(0, 0, 0);
            } else {
                float min = autoRange.min, max = autoRange.max;
                float mean = (float) (autoRange.sum / n);
                if (min != min || max != max || mean != mean)
                    range(0, 0, 0);
                else
                    range(min, max, mean);

            }

            return AbstractSeries.this;
        }

        public abstract void forEach(FloatProcedure f);

        private Series range(float min, float max) {
            AbstractSeries.this.minValue = min;
            AbstractSeries.this.maxValue = max;
            return AbstractSeries.this;
        }

        private Series range(float min, float max, float mean) {
            range(min, max);
            this.meanValue = mean;
            return this;
        }


//        abstract void limit();

        @Override
        public String name() {
            return name;
        }

        @Override
        public int capacity() {
            return capacity;
        }

        @Override
        public float maxValue() {
            return maxValue;
        }

        @Override
        public float minValue() {
            return minValue;
        }

        @Override
        public float meanValue() {
            return meanValue;
        }

        @Override
        public float[] color() {
            return color;
        }

        private static class AutoRange implements FloatProcedure {
            /**
             * min, max, mean
             */
            float min, max;
            double sum;
            int count; //counts non-NaN values

            AutoRange restart() {
                min = Float.POSITIVE_INFINITY;
                max = Float.NEGATIVE_INFINITY;
                sum = 0;
                count = 0;
                return this;
            }

            @Override
            public void value(float v) {
                if (v == v) {
                    if (v < min) min = v;
                    if (v > max) max = v;
                    sum += v;
                    count++;
                }
            }


        }
    }

//    /**
//     * TODO merge with BitmapWave
//     */
//    @Deprecated
//    public static class BitmapPlot implements PlotVis, BitmapMatrixView.BitmapPainter {
//        BitmapMatrixView bmp = null;
//        private final int w;
//        private final int h;
//
//
//        transient private List<Series> series;
//        transient private float minValue, maxValue;
//        private Graphics gfx;
//
//        volatile boolean update = false;
//
//        /**
//         * visualization bounds
//         */
//        float first = 0f, last = 1f;
//
//        public BitmapPlot(int w, int h) {
//            this.w = w;
//            this.h = h;
//        }
//
//        public float first() {
//            return first;
//        }
//
//        public float last() {
//            return last;
//        }
//
//        public int firstSample() {
//            return (int) Math.floor(first * (series.get(0).size() - 1) /* TODO */);
//        }
//
//        public int lastSample() {
//            return (int) Math.ceil(last * (series.get(0).size() - 1) /* TODO */);
//        }
//
//        @Override
//        public void stop() {
//            if (gfx != null) {
//                gfx.dispose();
//                gfx = null;
//            }
//            if (bmp != null) {
//                bmp.stop();
//                bmp = null;
//            }
//        }
//
//        @Override
//        public void draw(List<Series> series, float minValue, float maxValue, GL2 g) {
//            if (bmp == null) {
//                bmp = new BitmapMatrixView(w, h, this) {
//                    @Override
//                    public boolean alpha() {
//                        return true;
//                    }
//                };
//            }
//            this.series = series;
//            this.minValue = minValue;
//            this.maxValue = maxValue;
//
//            if (update) {
//                update = !bmp.updateIfShowing(); //keep updating till updated
//            }
//
//            bmp.paint(g, null);
//        }
//
//        @Override
//        public void update() {
//            update = true;
//        }
//
//
//        @Override
//        public synchronized void color(BufferedImage buf, int[] pix) {
//
//
//            if (gfx == null) {
//                gfx = buf.getGraphics();
//            }
//
//            gfx.clearRect(0, 0, w, h);
//
//            int ns = series.size();
//            if (ns == 0)
//                return;
//
//            int w = this.w;
//            int h = this.h;
//            float minValue = this.minValue;
//            float maxValue = this.maxValue;
//
//
//            float yRange = ((maxValue) - (minValue));
//            float absRange = Math.max(Math.abs(maxValue), Math.abs(minValue));
//            if (absRange < Float.MIN_NORMAL) absRange = 1;
//
//
//            float alpha = 1f / ns;
//            int first = firstSample(), last = lastSample();
//            assert (series.size() == 1) : "only size=1 support for now";
//            int sn = series.get(0).size();
//            for (Series s : series) {
//
//                for (int x = 0; x < w; x++) {
//
//                    float sStart = first + (last - first) * (x / ((float) w));
//                    float sEnd = first + (last - first) * ((x + 1) / ((float) w));
//
//                    int iStart = Util.clampSafe((int) Math.ceil(sStart), 0, sn - 1);
//                    int iEnd = Util.clampSafe((int) Math.floor(sEnd), 0, sn - 1);
//                    float amp = 0;
//
//                    amp += (iStart - sStart) * s.get(iStart);
//                    for (int i = iStart + 1; i < iEnd - 1; i++)
//                        amp += s.get(i);
//                    amp += (sEnd - iEnd) * s.get(iEnd);
//
//                    amp /= (sEnd - sStart);
//
//                    float ampNormalized = (amp - minValue) / yRange;
//
//                    float intensity = Math.abs(amp) / absRange;
//                    //gfx.setColor(Color.getHSBColor(intensity, 0.7f, 0.7f));
//                    float[] sc = s.color();
//                    float iBase = Util.unitize(intensity / 2 + 0.5f);
//                    gfx.setColor(new Color(sc[0] * iBase, sc[1] * iBase, sc[2] * iBase, alpha));
//
//                    int ah = Math.round(ampNormalized * h);
//                    gfx.drawLine(x, h / 2 - ah / 2, x, h / 2 + ah / 2);
//                }
//            }
//        }
//
//        private float sampleX(int x, int w, int first, int last) {
//            return ((float) x) / w * (last - first) + first;
//        }
//
//        public synchronized void pan(float pct) {
//            float width = last - first;
//            if (width < 1) {
//                float mid = ((first + last) / 2);
//                float nextMid = mid + (pct * width);
//
//                float first = nextMid - width / 2;
//                float last = nextMid + width / 2;
//                if (first < 0) {
//                    first = 0;
//                    last = first + width;
//                } else if (last > 1) {
//                    last = 1;
//                    first = last - width;
//                }
//
//                this.first = first;
//                this.last = last;
//            }
//
//            update();
//        }
//
//        public synchronized void scale(float pct) {
//
//            float first = this.first, last = this.last;
//            float view = last - first;
//            float mid = (last + first) / 2;
//            float viewNext = Util.clamp(view * pct, ScalarValue.EPSILON, 1);
//
//            first = mid - viewNext / 2;
//            last = mid + viewNext / 2;
//            if (last > 1) {
//                last = 1;
//                first = last - viewNext;
//            }
//            if (first < 0) {
//                first = 0;
//                last = first + viewNext;
//            }
//
//            this.first = first;
//            this.last = last;
//            update();
//        }
//
//    }

    public abstract static class PlotVis extends Stacking {

        abstract void draw(List<Series> series, float minValue, float maxValue, GL2 g);

    }

}