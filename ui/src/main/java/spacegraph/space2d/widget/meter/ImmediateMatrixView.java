package spacegraph.space2d.widget.meter;

import com.jogamp.opengl.GL2;
import jcog.Util;
import jcog.data.atomic.AtomicCycle;
import org.eclipse.collections.api.block.function.primitive.IntToFloatFunction;
import spacegraph.space2d.ReSurface;
import spacegraph.video.Draw;

import java.util.function.Supplier;

import static jcog.Util.toFloat;


/** TODO use TriggeredMatrixView for async which should cause less updates */
public class ImmediateMatrixView extends BitmapMatrixView {
    public ImmediateMatrixView(float[] x) {
        super(x);
    }
    public ImmediateMatrixView(double[] x) {
        super(x, 1, Draw::colorBipolar);
    }
//    public static ImmediateMatrixView fromFloat(Supplier<float[]> x, int len) {
//        super(x, len, Draw::colorBipolar);
//    }

    public ImmediateMatrixView(Supplier<float[]> x, int len, int stride) {
        super(x, len, stride, Draw::colorBipolar);
    }

    public ImmediateMatrixView(int w, int h, ViewFunction2D view) {
        super(w, h, view);
    }

    public ImmediateMatrixView(IntToFloatFunction d, int len, int stride, ViewFunction1D view) {
        super(d, len, stride, view);
    }

    public ImmediateMatrixView(float[][] x) {
        super(x);
    }

    @Override
    protected void paint(GL2 gl, ReSurface reSurface) {
        updateIfShowing();
        super.paint(gl, reSurface);
    }

    public static ImmediateMatrixView scroll(double[] x, boolean normalize, int window, int step) {
        assert(window >= step && (window % step == 0));

        int max = x.length;
        assert(max >= window);

        double[] xx = new double[window];
        AtomicCycle.AtomicCycleN i = new AtomicCycle.AtomicCycleN(max);
        return new ImmediateMatrixView(()->{
            int start = i.addAndGet(step);
            int over;
            int end = start + window;
            if (end >= max) {
                over = (end-max); end = max;
            } else
                over= 0;

            System.arraycopy(x, start, xx, 0, (end-start));
            if (over > 0)
                System.arraycopy(x, 0, xx, (end-start), over);

            if(normalize)
                Util.normalize(xx);
            return toFloat(xx) /* HACK */;
        }, window, step);
    }
}
