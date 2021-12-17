package spacegraph.space2d.widget.slider;

import com.jogamp.opengl.GL2;
import jcog.Util;
import jcog.math.v2;
import jcog.tree.rtree.Spatialization;
import org.eclipse.collections.api.block.procedure.primitive.FloatFloatProcedure;
import spacegraph.input.finger.Finger;
import spacegraph.input.finger.state.Dragging;
import spacegraph.input.finger.state.SurfaceDragging;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.PaintSurface;
import spacegraph.space2d.widget.port.TypedPort;
import spacegraph.video.Draw;

import static jcog.Str.n4;
import static jcog.Util.unitize;
import static jcog.Util.unitizeSafe;

/**
 * x and y are in 0..1.0 domain
 */
public class XYSlider extends PaintSurface  {

    public static final int BUTTON = 0;
    protected final v2 knob = new v2(0.5f, 0.5f);

    private FloatFloatProcedure change;
    private final float[] knobColor = {0.75f, 0.75f, 0.75f};


    private static final float _low = 0.2f;
    private static final float _HIH = 0.8f;
    private static final float[] lefAlphaCorners = {_low, _HIH, _HIH, _low};
    private static final float[] rihAlphaCorners = {_HIH, _low, _low, _HIH};
    private static final float[] topAlphaCorners = {_HIH, _HIH, _low, _low};
    private static final float[] botAlphaCorners = {_low, _low, _HIH, _HIH};
    private boolean pressing;


    public XYSlider() {
        super();
    }

//    public XYSlider(FloatRange x, FloatRange y) {
//        this();
//        set(x.floatValue(), y.floatValue());
//        on((xx, yy) -> {
//            x.setProportionally(xx);
//            y.setProportionally(yy);
//        });
//    }


//    /**
//     * creates a live-updating label
//     */
//    public Surface caption() {
//        return new VectorLabel() {
//            @Override
//            protected void renderContent(ReSurface r) {
//                text(summary());
//                super.renderContent(r);
//            }
//
//        };
//    }

    /**
     * TODO optional labels for x and y axes
     */
    public String summary() {
        return summaryX(knob.x) + ", " + summaryY(knob.y);
    }

    public static String summaryX(float x) {
        return n4(x);
    }

    public static String summaryY(float y) {
        return n4(y);
    }

    public XYSlider on(FloatFloatProcedure change) {
        this.change = change;
        return this;
    }


    final Dragging drag = new SurfaceDragging(this, BUTTON) {

        @Override
        protected boolean starting(Finger f) {
            return pressing = super.starting(f);
        }

        @Override
        public void stop(Finger finger) {
            super.stop(finger);
            pressing = false;
        }

        @Override
        protected boolean drag(Finger f) {
            touch(f);
            return true;
        }
    };

    private void touch(Finger f) {
        pressing = true; set(f.posRelative(this));
//        if (knob.setIfChanged(unitize(hitPoint.x), unitize(hitPoint.y), Spatialization.EPSILONf))
//            updated();
    }

    @Override
    public Surface finger(Finger f) {
        return f.test(drag) ? this : null;
    }


    protected void updated() {
        //Exe.invokeLater(() ->
        value(knob.x, knob.y);
        //);
    }

    protected void value(float kx, float ky) {
        FloatFloatProcedure change = this.change;
        if (change !=null)
            change.value(kx, ky);
    }


    @Override
    protected void paint(GL2 gl, ReSurface reSurface) {

        Draw.rectRGBA(bounds, 0.0f, 0.0f, 0.0f, 0.8f, gl);

        float px = knob.x;
        float py = knob.y;

        float knobThick = pressing ? 0.08f : 0.04f;


        float bw = bounds.w;
        float bh = bounds.h;
        float KT = Math.min(bw, bh) * knobThick;
        float kw = bounds.x + (px * bw);
        float kh = bounds.y + (py * bh);
        float KTH = KT / 2;
        Draw.rectAlphaCorners(bounds.x, kh - KTH, kw - KTH, kh + KTH, knobColor, lefAlphaCorners, gl
        );
        Draw.rectAlphaCorners(kw + KTH, kh - KTH, bounds.x + bw, kh + KTH, knobColor, rihAlphaCorners, gl
        );

        Draw.rectAlphaCorners(kw - KTH, bounds.y, kw + KTH, kh - KTH, knobColor, botAlphaCorners, gl
        );
        Draw.rectAlphaCorners(kw - KTH, kh + KTH, kw + KTH, bounds.y + bh, knobColor, topAlphaCorners, gl
        );


    }

    public final XYSlider set(v2 v) {
        return set(v.x, v.y);
    }

    public XYSlider set(float x, float y) {
        if (knob.setIfChanged(unitize(x), unitize(y), Spatialization.EPSILONf))
            updated();
        return this;
    }

    public Surface chip() {
//        FloatPort px = new FloatPort();
//        FloatPort py = new FloatPort();
//        b.set(S, px, 0.1f);
//        b.set(E, py, 0.1f);
        TypedPort<v2> b = new TypedPort<>(v2.class);
        on((x, y) -> b.outLazy(() -> knob));
        b.set(this);

        return b;
    }

    /** TODO needs work, untested */
    public static class RelativeXYSlider extends XYSlider {
        public static final float restitutionRate = 0.5f;

        float kx = 1, ky = 1;
        float ax, ay;
        float speedX = 0.02f, speedY = speedX;
//        float exponent = 2;

        @Override
        protected void paint(GL2 gl, ReSurface reSurface) {
            ax = unitizeSafe(ax + kx*speedX);
            ay = unitizeSafe(ay + ky*speedY);
            //super.value(ax, ay);
//            System.out.println(kx + " " + ky + " : " + ax + " " + ay);

            super.value(ax, ay);

            kx = Util.lerpSafe(restitutionRate, 0, kx);
            ky = Util.lerpSafe(restitutionRate, 0, ky);
            knob.set(kx/2 + 0.5f, ky/2 + 0.5f);

            super.paint(gl, reSurface);
        }

        @Override
        protected void value(float kx, float ky) {
            this.kx = 2 * (kx - 0.5f);
            this.ky = 2 * (ky - 0.5f);
//            super.value(ax, ay);
        }

    }
}