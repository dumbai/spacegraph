package spacegraph.space2d.widget.slider;

import com.jogamp.opengl.GL2;
import jcog.Util;
import jcog.math.v2;
import org.eclipse.collections.api.block.procedure.primitive.ObjectFloatProcedure;
import org.jetbrains.annotations.Nullable;
import spacegraph.input.finger.Finger;
import spacegraph.input.finger.state.Dragging;
import spacegraph.input.finger.state.SurfaceDragging;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.PaintSurface;
import spacegraph.video.Draw;

/**
 * abstract 1D slider/scrollbar
 */
public class SliderModel extends PaintSurface {


    /**
     * dead-zone at the edges to latch min/max values
     */
    private static final float margin =
            0.001f;
    public static final int BUTTON = 0;


    private @Nullable ObjectFloatProcedure<SliderModel> change;



    private volatile float p;

    public SliderModel() {
    }

    public SliderModel(float v) {
        setValue(v);
    }

    public SliderModel on(ObjectFloatProcedure<SliderModel> c) {
        this.change = c;
        return this;
    }

    @Override
    protected final void paint(GL2 gl, ReSurface reSurface) {
        Draw.bounds(bounds, gl, g -> ui.paint(this.p, g));
    }

    public SliderUI ui = SolidLeft;

    public interface SliderUI {
        void paint(float p, GL2 gl);

        /**
         * resolves the 2d hit point to a 1d slider value
         */
        float p(v2 hitPoint);
    }

    public void update() {

    }

    public SliderModel type(SliderUI draw) {
        this.ui = draw;
        return this;
    }

    final Dragging drag = new SurfaceDragging(this, BUTTON) {
        @Override
        protected boolean drag(Finger f) {
            setPoint(f);
            return true;
        }
    };

    @Override
    public Surface finger(Finger f) {
        if (f.test(drag))
            return this;

        return null;
    }

    private void setPoint(Finger f) {
        v2 rel = f.posRelative(this);
//        System.out.println(rel);
        setPoint(ui.p(rel));
    }

    private void setPoint(float pNext) {
        Util.assertFinite(pNext);

        float pPrev = this.p;
        if (Util.equals(pPrev, pNext))
            return;

        this.p = pNext;

        _onChanged();
    }

//    protected void onChanged() {
//        if (change != null) {
//            //TODO if async
//            //Exe.invoke(this::_onChanged);
//            _onChanged();
//        }
//    }

    private void _onChanged() {
        ObjectFloatProcedure<SliderModel> c = this.change;
        if (c != null)
            c.value(this, value());
    }


    final void setValue(float v) {
        if (v == v) {
            setPoint(p(v));
        } else {
            //? NaN to disable?
        }

    }

    public float value() {
        return v(p);
    }

    /**
     * normalize: gets the output value given the proportion (0..1.0)
     */
    float v(float p) {
        return p;
    }

    /**
     * unnormalize: gets proportion from external value
     */
    float p(float v) {
        return v;
    }

    private static float pHorizontal(v2 hitPoint) {
        return pTarget(hitPoint.x);
    }

    private static float pVertical(v2 hitPoint) {
        return pTarget(1 - hitPoint.y);
    }

    private static float pTarget(float x) {
        if (x <= margin)
            return 0;
        else if (x >= (1.0f - margin))
            return 1.0f;
        else
            return x;
    }

    /**
     * resulting value is lower aligned
     */
    private static float pTarget(float x, float knob) {

        return knob > 0 ? Util.clamp(pTarget(x) - knob / 2, 0, 1 - knob) : x;
    }


    private static final SliderUI SolidLeft = new SliderUI() {
        @Override
        public void paint(float p, GL2 gl) {


            gl.glColor4f(0.0f, 0.0f, 0.0f, 0.5f);
            float W = 1;
            float barSize = W * p;
            float H = 1;
            Draw.rect(barSize, 0, W - barSize, H, gl);


            gl.glColor4f(0.75f * 1.0f - p, 0.75f * p, 0.0f, 0.8f);
            Draw.rect(0, 0, barSize, H, gl);
        }

        @Override
        public float p(v2 hitPoint) {
            return pHorizontal(hitPoint);
        }
    };

    /**
     * default impl, with point-center
     */
    public static final SliderUI KnobVert = new KnobVert() {
//        @Override
//        public float p(v2 hitPoint) {
//            return pTarget(1 - hitPoint.y, 0);
//        }
    };


    /**
     * default impl, with point-center
     */
    public static final SliderUI KnobHoriz = new KnobHoriz() {
//        @Override
//        public float p(v2 hitPoint) {
//            return pTarget(hitPoint.x, 0);
//        }
    };

    public static class KnobVert extends Knob {

        @Override
        public void paint(float p, GL2 gl) {
            //float W = bounds.w, H = bounds.h;

            //float y = (float) 1 * (1-p);

            gl.glColor4f(0.0f, 0.0f, 0.0f, 0.5f);
            Draw.rect(0, 0,1, 1, gl);

//            gl.glColor4f(1f - p, p, 0f, 0.75f);
//            Draw.rect(0, y-knob/2, 1, knob, gl);

            p = 1-p;

            float knob = this.knob;
            float kh = knob / 2;
            gl.glColor4f(p, 1-p, 0.0f, 0.5f);
            Draw.rect(margin, Util.lerp(p, kh, 1 - kh) - kh, 1-margin, knob, gl);
        }
        @Override
        public float p(v2 hitPoint) {
            return pTarget(1-hitPoint.y);
        }
    }

    public static class KnobHoriz extends Knob {

        @Override
        public void paint(float p, GL2 gl) {
            gl.glColor4f(0.0f, 0.0f, 0.0f, 0.5f);
            Draw.rect(0, 0, 1, 1, gl);

            float knob = this.knob;
            float kh = knob / 2;
            gl.glColor4f(1.0f - p, p, 0.0f, 0.5f);
            Draw.rect(Util.lerp(p, kh, 1- kh) - kh, margin, knob,  1-margin, gl);
        }

    }

    public abstract static class Knob implements SliderUI {

        /**
         * proportion of the total range of which the knob is visible
         */
        public float knob = 0.05f;
        public float margin = 0.005f;

        @Override
        public float p(v2 hitPoint) {
            return pTarget(hitPoint.x);
            //return pTarget(hitPoint.x, knob);
        }
    }
}