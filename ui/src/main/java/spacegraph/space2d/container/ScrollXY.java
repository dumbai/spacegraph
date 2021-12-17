package spacegraph.space2d.container;

import jcog.Util;
import jcog.data.map.CellMap;
import jcog.math.FloatSupplier;
import jcog.math.v2;
import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.input.finger.Finger;
import spacegraph.input.finger.state.SurfaceDragging;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.collection.MutableMapContainer;
import spacegraph.space2d.container.grid.GridModel;
import spacegraph.space2d.container.grid.GridRenderer;
import spacegraph.space2d.container.grid.ListModel;
import spacegraph.space2d.container.unit.Clipped;
import spacegraph.space2d.widget.slider.FloatSlider;
import spacegraph.space2d.widget.slider.SliderModel;
import spacegraph.space2d.widget.slider.XYSlider;
import spacegraph.util.MutableRectFloat;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static jcog.Util.*;
import static spacegraph.space2d.widget.slider.SliderModel.KnobHoriz;
import static spacegraph.space2d.widget.slider.SliderModel.KnobVert;

/**
 * virtual viewport
 */
public class ScrollXY<S> extends Bordering {


    public S content;

    /**
     * proportional in scale to bounds
     */
    private static final float defaultScrollEdge = 0.12f;

    private final FloatSlider scrollX;
    private final FloatSlider scrollY;
    private final XYSlider scale;

    /**
     * current view, in local grid coordinate
     */
    private MutableRectFloat view = new MutableRectFloat(); //TODO MutableFloatRect

    private v2 viewMin = new v2(0.5f,0.5f);
    //private volatile v2 viewDefault = new v2(0,0);
    protected v2 viewMax = new v2(1,1);


    public ScrollXY(@Nullable GridModel<S> model, @Nullable GridRenderer<S> renderer) {
        this();
        if (model == null) model = (GridModel)this;
        if (renderer == null) renderer = (GridRenderer)this;
        set(new DynGrid(model, renderer));
    }

    public ScrollXY(@Nullable ScrolledXY scrollable) {
        this();
        if (scrollable==null) scrollable = (ScrolledXY)this;
        scrollable((S)scrollable);
    }

    /**
     * by default, only the first cell will be visible
     */
    public ScrollXY() {
        super();

        this.scale = new XYSlider(); //.RelativeXYSlider();
        this.scrollX = new FloatProportionalSlider("X", () -> 0, () -> view.w / viewMax.x, () -> viewMax.x - view.w, true);
        this.scrollY = new FloatProportionalSlider("Y", () -> 0, () -> view.h / viewMax.y, () -> viewMax.y - view.h, false);


        scrollX.on((sx, x) -> scroll(x, view.y, view.w, view.h));
        scrollY.on((sy, y) -> scroll(view.x, y, view.w, view.h));
//        scale.set(1,1);
        scale.on((w, h)-> scroll(view.x, view.y, lerp(w, viewMin.x, viewMax.x), lerp(h, viewMin.y, viewMax.y)));

        set(E,scrollY);
        set(S,scrollX);
        set(SE, scale);

    }

    public synchronized void scrollable(S scrollable) {

        //scrollable.update(this);
//        if (viewMin == null)
//            throw new NullPointerException("view min set by " + scrollable);
//        if (viewMax == null)
//            throw new NullPointerException("view max set by " + scrollable);
//        if (view == null)
//            view.size(viewMax.x, viewMax.y); //TODO max reasonable limit

        borderSize(defaultScrollEdge);

        set(new Clipped((Surface)(content = scrollable)));
    }

    public ScrollXY<S> viewMax(v2 viewMax) {
        this.viewMax = viewMax;
        this.viewMin = new v2(Math.min(viewMax.x, viewMin.x),Math.min(viewMax.y, viewMin.y));
        layoutModel(); //TODO update if changed
        return this;
    }

    public ScrollXY<S> viewMin(v2 viewMin) {
        this.viewMin = viewMin;
        this.viewMax = new v2(Math.max(viewMax.x, viewMin.x),Math.max(viewMax.y, viewMin.y));
        layoutModel(); //TODO update if changed
        return this;
    }
    public ScrollXY<S> viewMinMax(v2 min, v2 max) {
        this.viewMin = min;
        this.viewMax = max;
        layoutModel(); //TODO update if changed
        return this;
    }
    public ScrollXY<S> view(v2 view) {
        return view(view.x, view.y, Float.NaN);
    }

    public ScrollXY<S> view(float w, float h) {
        return view(w, h, Float.NaN);
    }

//    public ScrollXY<S> view(float w, float h) {
//        return view(w, h, Float.NaN);
//    }
    public ScrollXY<S> view(float w, float h, float targetAspect) {
        RectF nextView = RectF.WH(w, h);
        this.view.set(nextView);
        layoutModel(targetAspect);
        return this;
    }

//    /** manually trigger layout */
//    public final void update() {
//
//        if (content instanceof ScrolledXY)
//            ((ScrolledXY)content).update(this);
//
//        layoutModel();
//    }

    private synchronized void layoutModel() {
        layoutModel(Float.NaN);
    }

    private synchronized void layoutModel(float targetAspect) {

        if (view==null)
            return;

        float x1 = viewMin.x;
        float x2 = viewMax.x;
        viewMax.x = Math.max(x1, x2);
        float y1 = viewMin.y;
        float y2 = viewMax.y;
        viewMax.y = Math.max(y1, y2);
        float vw = clamp(view.w, x1, x2);
        float vh = clamp(view.h, y1, y2);

        //constrain aspect-ratio
        if (targetAspect==targetAspect) {
            //TODO refine
            float aw = vw / (x2 - x1);
            float ah = vh / (y2 - y1);
            if (aw > ah) {
                vw = vh / targetAspect;
            } else {
                vh = vw / targetAspect;
            }
            x2 = x1 + vw;
            y2 = y1 + vh;
        }

        scale.set(normalize(view.w, x1, x2), normalize(view.h, y1, y2));
        RectF nextView = RectF.X0Y0WH(
                    clamp(view.x, 0, x2),
                    clamp(view.y, 0, y2),
                    vw, vh);
        this.view.set(nextView);


        S m = this.content;
        if (m instanceof ContainerSurface)
            ((ContainerSurface)m).layout();
    }

    /**
     * the current view
     */
    public final RectF view() {
        view.commitLerp(1);
        return view;
    }
//
//    /** set the view window's center of focus, re-using the current width and height */
//    public final ScrollXY<S> view(float x, float y) {
//        return view(x, y, view.w, view.h);
//    }
//
    /** set the view window's center and size of focus, in grid coordinates */
    public final ScrollXY<S> view(RectF v) {
        view.set(v);
        return this;
    }


//    /**
//     * enables requesting entries from the -1'th row and -1'th column of
//     * the model to use as 'pinned' row header cells
//     */
//    public ScrollXY<S> setHeader(boolean rowOrColumn, boolean enabled) {
//        throw new TODO();
//    }

    /**
     * enables or disables certain scrollbar-related features per axis
     */
    public ScrollXY<S> setScrollBar(boolean xOrY, boolean scrollVisible, boolean scaleVisible) {
        if (xOrY) {
            scrollX.visible(scrollVisible);
            borderSize(S, scrollVisible ? defaultScrollEdge : 0);
            //scaleW.visible(scaleVisible);
            borderSize(N, scaleVisible ? defaultScrollEdge : 0);
        } else {
            scrollY.visible(scrollVisible);
            borderSize(E, scrollVisible ? defaultScrollEdge : 0);
            //scaleH.visible(scaleVisible);
            borderSize(W, scaleVisible ? defaultScrollEdge : 0);
        }


        return this;
    }

//    /** limits the scaling range per axis */
//    public ScrollXY<X> setCellScale(boolean xOrY, float minScale, float maxScale) {
//        throw new TODO();
//    }
//
//    /** limits the viewing range per axis */
//    public ScrollXY<X> setCellView(boolean xOrY, float minCoord, float maxCoord) {
//        throw new TODO();
//    }


    public final boolean scroll(float w, float h) {
        return scroll(0, 0, w, h);
    }

    private final transient AtomicBoolean scrolling = new AtomicBoolean(false);

   public boolean scroll(float x, float y, float w, float h) {
        if (!scrolling.compareAndSet(false, true))
            return false;
        try {
    //        boolean autoHideScroll = true;
    //        setScrollBar(true, (!autoHideScroll || w < viewMax.x), true);
    //        setScrollBar(false, (!autoHideScroll || h < viewMax.y), true);

    //        viewMax(new v2(x2-x1, y2-y1)); //HACK

            float x2 = x + w;
            float y2 = y + h;
            RectF nextView = RectF.X0Y0WH(x, y, x2 - x, y2 - y);
            if (!nextView.equals(view)) {
                this.view.set(nextView);
                _updateScrollbars(); //HACK
                layoutModel();
                layout();
                return true;
            }
            return false;
        } finally {
            scale.visible(scrollX.visible() || scrollY.visible());
            scrolling.set(false);
        }

    }

    private void _updateScrollbars() {
        scrollX.set(view.x);
        scrollY.set(view.y);
    }


    private final SurfaceDragging dragging = new SurfaceDragging(this, 2) {
        private final v2 pos = new v2();
        final MutableRectFloat viewPre = new MutableRectFloat();
        float xRate = 0.005f;
        float yRate = 0.005f;

        float sy;

        @Override
        protected boolean starting(Finger f) {
            if (super.starting(f)) {
                pos.set(f.posScreen);
                if (ScrollXY.this.view!=null) //HACK
                    viewPre.set( ScrollXY.this.view );
                sy = 1;
                return true;
            }
            return false;
        }

        @Override
        protected boolean drag(Finger f) {
            v2 posNext = f.posScreen;
            v2 d = posNext.subClone(pos);

            MutableRectFloat v = this.viewPre;
            float vw = v.w;
            float vh = v.h;
            float vwHalf = vw / 2;
            float vhHalf = vh / 2;
            float nx = clamp(v.cx() + vw *  d.x * xRate, vwHalf, viewMax.x - vwHalf);
            float ny = clamp(v.cy() + vh * -d.y * yRate, vhHalf, viewMax.y - vhHalf);
            if (scroll(nx - vwHalf, ny - vhHalf, vw, vh)) {
                //Util.nop();
            }

            //System.out.println(this + " " + diff);
            //sy += diff.y/h() * timeScaleRate;
            //timeShiftPct(Util.tanhFast(diff.x/w() * timeShiftRate)/3f);
            //timeShift(diff.x/w() * timeShiftRate);
            //scale(sy);
            return true;
        }
    };

    @Override
    public Surface finger(Finger f) {
        Surface s = super.finger(f);
        return s == null && f.test(dragging) ? null : s;
    }

    public final ScrollXY<S> viewMin(float w, float h) {
        return viewMin(new v2(w, h));
    }
    public final ScrollXY<S> viewMax(float w, float h) {
        return viewMax(new v2(w, h));
    }


    @FunctionalInterface
    @Deprecated public interface ScrolledXY {

        /**
         * implementors expected to initially set the view bounds on init, and update the view window if necessary
         */
        void update(ScrollXY s);
    }


    @SafeVarargs
    public static <X> ScrollXY array(GridRenderer<? super X> builder, X...list) {
        return new ScrollXY(ListModel.of(list), builder);
    }

    public static <X> ScrollXY list(GridRenderer<X> builder, List<X> list) {
        return list((Function<X,Surface>)builder, list);
    }

    public static <X> ScrollXY list(Function<X, Surface> builder, List<X> list) {
        return new ScrollXY(ListModel.of(list), GridRenderer.value(builder));
    }

    public static <X> ScrollXY listCached(Function<X, Surface> builder, List<X> list, int cacheCapacity) {
        return new ScrollXY(ListModel.of(list), GridRenderer.valueCached(builder, cacheCapacity));
    }

    private class FloatProportionalSlider extends FloatSlider implements Finger.ScrollWheelConsumer {

        /** proportional knob width */
        private final FloatSupplier knob;
        private final boolean hOrV;

        FloatProportionalSlider(String label, FloatSupplier min, FloatSupplier knob, FloatSupplier max, boolean hOrV) {
            super(new FloatSliderModel() {
                    @Override
                    public float min() {
                        return min.asFloat();
                    }

                    @Override
                    public float max() {
                        return max.asFloat();
                    }
                }, label
            );

            this.knob = knob;

            type((this.hOrV = hOrV) ? new KnobHoriz() : new KnobVert());
        }

        @Override
        public Surface finger(Finger f) {
            Surface s = super.finger(f);
            if (s == this) {
                float ry = f.rotationY(true);
                if (ry != 0) {
                    float nw, nh;
                    if (hOrV) {
                        nw = Math.min(viewMax.x - viewMin.x, (view.w * (1 + 0.5f * ry)));
                        nh = view.h;
                    } else {
                        nw = view.w;
                        nh = Math.min(viewMax.y - viewMin.y, (view.h * (1 + 0.5f * ry)));
                    }
                    scroll(view.x, view.y, nw, nh);
                    ScrollXY.this.layoutModel(); //HACK
                }
            }
            return s;
        }

        @Override
        public boolean canRender(ReSurface r) {

            float k = knob.asFloat();

            //HACK
            k = Float.isFinite(k) ? Util.unitize(k) : 1.0f;

            ((SliderModel.Knob)slider.ui).knob = k;

            return super.canRender(r);
        }
    }

    static void cellVisible(Surface s, float cw, float ch, float cx, float cy) {
        s.pos(RectF.XYWH(cx, cy, cw, ch));
    }

    /**
     * "infinite" scrollable grid, possibly only partially visible at a given time
     * internally stores cells as hashed 2D coordinates entries in 16-bit pairs of x,y coordinates
     */
    class DynGrid extends MutableMapContainer<Integer, S> implements ScrolledXY {

        volatile int x1, y1, x2 = 1, y2 = 1;
        private transient float dx, dy;
        private transient float cw, ch;

        private final GridModel<S> model;
        private final GridRenderer<S> render;

        private DynGrid(GridModel<S> model, GridRenderer<S> render) {
            super();
            this.model = model;
            this.render = render;
        }

        private static final int MAX_INITIAL_SIZE = 16;

        @Override
        protected void starting() {
            super.starting();
            model.start(ScrollXY.this);
            viewMax.x = model.cellsX();
            viewMax.y = model.cellsY();
            scroll(Math.min(MAX_INITIAL_SIZE, viewMax.x), Math.min(MAX_INITIAL_SIZE, viewMax.y));
        }

        @Override
        protected void stopping() {
            model.stop(ScrollXY.this);
            super.stopping();
        }

        @Override
        protected void hide(S x, Surface s) {
            render.hide(x, s);
        }

        protected Surface surface(short x, short y, S nextValue) {
            return render.apply(x, y, nextValue);
        }

        protected S value(short sx, short sy) {
            return model.get(sx, sy);
        }



        /**
         * test if a cell is currently visible
         */
        boolean cellVisible(short x, short y) {
            return (x >= x1 && x < x2)
                    &&
                    (y >= y1 && y < y2);
        }




        @Override
        protected void doLayout(float dtS) {

            if (parent == null)
                return;

            ScrollXY xy = parentOrSelf(ScrollXY.class);
            if (xy == null)
                return;

            RectF v = xy.view();
            float vx = v.x, vy = v.y, vw = v.w, vh = v.h;
            this.x1 = Math.max(0, (int) Math.floor(vx));
            this.y1 = Math.max(0, (int) Math.floor(vy));
            this.x2 = Math.min(cellsX(),(int) Math.ceil(vx + vw));
            this.y2 = Math.min(cellsY(), (int) Math.ceil(vy + vh));

            dx = x();
            dy = y();
            float ww = w();
            float hh = h();
            cw = ww / vw;
            ch = hh / vh;


            cells.map.removeIf(e -> {
                Surface s = ((SurfaceCacheCell) e).surface;

                if (s == null) {
                    //return true;
                } else {
                    int cellID = e.key;
                    short sx = (short) (cellID >> 16);
                    short sy = (short) (cellID & 0xffff);
                    return !cellVisible(sx, sy);
                }

                return false;
            });





            if (model instanceof ScrolledXY)
                ((ScrolledXY)model).update(ScrollXY.this);

            for (short sx = (short) x1; sx < x2; sx++) {
                for (short sy = (short) y1; sy < y2; sy++) {
                    SurfaceCacheCell e = (SurfaceCacheCell) set(sx, sy, value(sx, sy), true);
                    if (e != null) {
                        Surface s = e.surface;
                        if (s != null) {
                            doLayout(s, vx, vy, sx, sy);
                            if (s.parent == null)
                                s.start(this);
                        }
                    }
                }
            }


        }


        void doLayout(Surface s, float vx, float vy, short sx, short sy) {
            float cx = dx + (sx - vx + 0.5f) * cw;
            float cy = dy + h() - ((sy - vy + 0.5f) * ch);
            ScrollXY.cellVisible(s, cw, ch, cx, cy);
        }

        public final void set(short x, short y, @Nullable S v) {
            set(x, y, v, false);
        }

        /**
         * allows a model to asynchronously report changes, which may be visible or not.
         * set 'v' to null to remove an entry (followed by a subsequent non-null 'v'
         * is a way to force rebuilding of a cell.)
         * returns if there was a change
         */
        CellMap.CacheCell set(short x, short y, @Nullable S nextValue, boolean force) {
            if (!force && !cellVisible(x, y))
                return null;

            return put(shortToInt(x, y), nextValue, this::renderer);
        }

        private Surface renderer(int cellID, S value) {
            short sx = (short) (cellID >> 16);
            short sy = (short) (cellID & 0xffff);
            return surface(sx, sy, value);
        }


        public int cellsX() {
            return model.cellsX();
        }


        public int cellsY() {
            return model.cellsY();
        }

        @Override
        public void update(ScrollXY s) {
            float minX = 0.5f; float minY = 0.5f;
            v2 min = new v2(minX, minY);
            v2 max = new v2(Math.max(minX, cellsX()), Math.max(minY, cellsY()));
            s.viewMinMax(min, max);
            s.view(max); //TODO reasonable # of items cut-off
        }
    }
}