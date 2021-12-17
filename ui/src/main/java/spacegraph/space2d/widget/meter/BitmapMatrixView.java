package spacegraph.space2d.widget.meter;

import com.jogamp.opengl.GL2;
import jcog.Util;
import jcog.math.FloatSupplier;
import jcog.math.v2;
import jcog.math.v2i;
import jcog.signal.wave2d.Bitmap2D;
import jcog.tree.rtree.rect.RectF;
import org.eclipse.collections.api.block.function.primitive.IntToFloatFunction;
import spacegraph.input.finger.Finger;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.video.Draw;
import spacegraph.video.Tex;
import spacegraph.video.TexSurface;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.sqrt;
import static jcog.Util.clampSafe;
import static jcog.Util.unitizeSafe;


/**
 * 1D/2D bitmap viewer with parametric coloring.
 * updated and displayed as a bitmap texture
 */
public class BitmapMatrixView extends TexSurface {

    public final int w, h;

    protected BitmapPainter view;

    public final v2 touchPos = new v2();
    public final v2i touchPix = new v2i(-1, -1);

    private static final int TOUCH_BUTTONS = /* button 1 */ 1 | /* button 3 */ (1<<3);

    public volatile int touchState; //buttons pressed while touching
    private BufferedImage buf;
    private int[] pix;
    public boolean cellTouch = true;


    /** the implementation must implement BitmapPainter */
    protected BitmapMatrixView(int w, int h) {
        this(w, h, null);
    }


    public BitmapMatrixView(float[] d, ViewFunction1D view) {
        this(d, Util.sqrtInt(d.length), view);
    }

    public BitmapMatrixView(double[] d) {
        this(d, Draw::colorBipolar);
    }

    public BitmapMatrixView(double[] d, ViewFunction1D view) {
        this(d, Util.sqrtInt(d.length), view);
    }

    public BitmapMatrixView(int w, int h, ViewFunction2D view) {
        this(w, h, (BitmapPainter)view);
    }

    public BitmapMatrixView(int w, int h, BitmapPainter view) {
        super(new Tex());
        this.w = w;
        this.h = h;
        this.view = view != null ? view : ((BitmapPainter) this);
    }

    public BitmapMatrixView(Bitmap2D tex) {
        this(tex.width(), tex.height(), (x, y)->{
            float a = tex.value(x, y);
            return Draw.rgbInt(a,a,a);
        });
    }


    public BitmapMatrixView(float[] f) {
        this(f, Util.sqrtInt(f.length));
    }

    public BitmapMatrixView(float[] f, int stride) {
        this(f, stride, Draw::colorBipolar);
    }

    public BitmapMatrixView(float[/*height*/][/*width*/] f) {
        this(f, colorBipolar(f));
    }

    public BitmapMatrixView(float[/*height*/][/*width*/] f, ViewFunction2D view) {
        this(f[0].length, f.length, view);
    }
    public BitmapMatrixView(double[/*height*/][/*width*/] f) {
        this(f[0].length, f.length, colorBipolar(f));
    }

    private BitmapMatrixView(float[] d, int stride, ViewFunction1D view) {
        this((int) Math.ceil(((float) d.length) / stride), stride, (x, y) -> {
            int i = y * stride + x;
            return i < d.length ? view.update(d[i]) : 0;
        });
    }

    public BitmapMatrixView(IntToFloatFunction d, int len, ViewFunction1D view) {
        this(d, len, Math.max(1, (int) Math.ceil(sqrt(len))), view);
    }

    public BitmapMatrixView(IntToFloatFunction d, int len, int stride, ViewFunction1D view) {
        this((int) Math.ceil(((float) len) / stride), stride, (x, y) -> {
            int i = y * stride + x;
            return i < len ? view.update(d.valueOf(i)) : 0;
        });
    }

    public BitmapMatrixView(double[] d, int stride, ViewFunction1D view) {
        this((int) Math.ceil(((float) d.length) / stride), stride, (x, y) -> {
            int i = y * stride + x;
            return i < d.length ? view.update((float) d[i]) : 0;
        });
    }

    public <P extends FloatSupplier> BitmapMatrixView(P[] d, int stride, ViewFunction1D view) {
        this((int) Math.ceil(((float) d.length) / stride), stride, (x, y) -> {
            int i = y * stride + x;
            return i < d.length ? view.update(d[i].asFloat()) : 0;
        });
    }


    public BitmapMatrixView(Supplier<float[]> e, int len, ViewFunction1D view) {
        this(e, len, Math.max(1, (int) Math.ceil(sqrt(len))), view);
    }

    public BitmapMatrixView(Supplier<float[]> e, int len, int stride, ViewFunction1D view) {
        this((int) Math.ceil(((float) len) / stride), stride, viewFunction2D(new IntToFloatFunction() {
            float[] cache;
            @Override
            public float valueOf(int x) {
                if (x == 0) cache = e.get();
                return cache[x];
            }
        }, stride, view));
    }

    public static ViewFunction2D viewFunction2D(IntToFloatFunction e, int stride, ViewFunction1D view) {
        return (x, y) -> view.update(e.valueOf(y * stride + x));
    }

    public static ViewFunction2D colorBipolar(float[] ww) {
        return arrayRendererX(ww);
    }


    public static ViewFunction2D arrayRendererX(float[] ww) {
        return (x, y) -> Draw.colorBipolar(ww[x]);
    }
    public static ViewFunction2D arrayRendererY(float[] ww) {
        return (x, y) -> Draw.colorBipolar(ww[y]);
    }

    public static ViewFunction2D colorBipolar(float[/*height*/][/*width*/] ww) {
        return (x, y) -> Draw.colorBipolar(ww[y][x]);
    }
    public static ViewFunction2D colorHue(float[/*height*/][/*width*/] ww) {
        return (x, y) ->
                Draw.colorHue(unitizeSafe(ww[y][x]));
                //Draw.colorHueInfra(unitizeSafe(ww[y][x]), 0.1f);

    }

    public static ViewFunction2D colorBipolar(double[][] ww) {
        return (x, y) -> Draw.colorBipolar((float) ww[y][x]);
    }



    @Override
    public Surface finger(Finger finger) {
        if (cellTouch && updateTouch(finger))
            return this;
        return null;
    }

    protected boolean updateTouch(Finger finger) {

        v2 r = finger.posRelative(bounds);
        if (r.inUnit()) {
            int touchState = finger.buttonDown.intDirect();
            if ((touchState & TOUCH_BUTTONS)!=0) {
                this.touchState = touchState;

                r.scaleInto(w, h, touchPos);

                touchPix.set(
                        clampSafe((int) Math.floor(touchPos.x), 0, w - 1),
                        (h - 1) - clampSafe((int) Math.floor(touchPos.y), 0, h - 1));
                return true;
            }
        }

        touchPix.set(-1, -1);
        touchState = 0;
        return false;
    }

    @Override
    protected void paint(GL2 gl, ReSurface reSurface) {
        if (buf!=null)
            tex.set(buf, gl);

        super.paint(gl, reSurface);
        /* paint cursor hilited cell */
        int tx = touchPix.x;
        int ty = (this.h-1) - touchPix.y;
        if (cellTouch && tx >= 0 && ty >= 0) {
            float w = w() / this.w, h = h() / this.h;
            float x = x(), y = y();
            gl.glColor4f(0.5f, 0.5f, 0.5f, 0.75f);
            gl.glLineWidth(2);
            Draw.rectStroke(x + tx * w, y + ty * h, w, h, gl);
        }
    }


    /**
     * the position of a cell's center
     */
    private v2 cell(float x, float y) {
        float W = w();
        float xx = ((x + 0.5f) / (w)) * W;
        float H = h();
        float yy = (1.0f - ((y + 0.5f) / (h))) * H;
        return new v2(xx, yy);
    }

    /**
     * the prw, prh represent a rectangular size proportional to the displayed cell size
     */
    public RectF cellRect(float x, float y, float prw, float prh) {
        float pw = prw / this.w;
        float ph = prh / this.h;
        v2 c = cell(x, y);
        return RectF.XYWH(c.x, c.y, pw * w(), ph * h());
    }

    /**
     * must call this to re-generate texture so it will display
     */
    public final boolean updateIfShowing() {
        return showing() && update();
    }

    private final AtomicBoolean busy = new AtomicBoolean();

    protected final boolean update() {

        if (!busy.weakCompareAndSetVolatile(false, true))
            return false;

        try {
            if (buf == null) {
                if (w == 0 || h == 0) return false;

                this.buf = new BufferedImage(w, h, alpha() ? TYPE_INT_ARGB : TYPE_INT_RGB);
                this.pix = ((DataBufferInt) buf.getRaster().getDataBuffer()).getData();
            }

            renderView();

            return tex.set(buf);

        } catch (NullPointerException e) {
            //HACK, try again
            return false;
        } finally {
            busy.set(false);
        }

    }

    private void renderView() {
        view.color(buf, pix);
    }

    public boolean alpha() {
        return false;
    }

//    public boolean equalShape(Tensor x) {
//        int[] shape = x.shape();
//        if (shape.length == 1)
//            return h == 1 && w == shape[0];
//        else if (shape.length == 2) {
//            return w == shape[0] && h == shape[1];
//        } else
//            return false;
//    }

    @FunctionalInterface  public interface ViewFunction1D {
        /**
         * updates the GL state for each visited matrix cell (ex: gl.glColor...)
         * before a rectangle is drawn at the returned z-offset
         */
        int update(float x);
    }

    @FunctionalInterface
    public interface BitmapPainter {
        /** provides access to the bitmap in either BufferedImage or the raw int[] raster */
        void color(BufferedImage buf, int[] pix);
    }


    @FunctionalInterface
    public interface ViewFunction2D extends BitmapPainter {
        /**
         * updates the GL state for each visited matrix cell (ex: gl.glColor...)
         * before a rectangle is drawn at the returned z-offset
         */
        int color(int x, int y);

        default void color(BufferedImage buf, int[] pix) {
            int w = buf.getWidth(), h = buf.getHeight();
            int i = 0;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    pix[i++] = color(x, y);
                }
            }
        }
    }

    BitmapMatrixView cellTouch(boolean cellTouch) {
        this.cellTouch = cellTouch;
        return this;
    }
}