package spacegraph.space2d.widget.slider;

import jcog.signal.IntRange;

public class IntSlider extends FloatSlider {

    private IntSlider(int v, int min, int max) {
        super(v, min, max);
    }

    public IntSlider(IntRange x) {
        this(x.intValue(), x.min, x.max);
        on(x::set);
        input = x;
    }

    public IntSlider(String label, IntRange x) {
        this(x);
        text(label);
    }



//    protected FloatSliderModel slider(float v, float min, float max) {
//        return new MyDefaultFloatSlider(v, min, max);
//    }
//
//    private static final class MyDefaultFloatSlider extends DefaultFloatSlider {
//
//        public MyDefaultFloatSlider(float v, float min, float max) {
//            super(v, min, max);
//        }
//
//        @Override
//        protected float p(float v) {
//            return super.p(Math.round(v));
//        }
//
//        @Override
//        protected float v(float p) {
//            return Math.round(super.v(p));
//        }
//    }
}