package spacegraph.space2d.widget.slider;

import jcog.Str;
import jcog.Util;
import jcog.math.FloatSupplier;
import jcog.pri.Prioritized;
import jcog.pri.UnitPri;
import jcog.signal.DoubleRange;
import jcog.signal.FloatRange;
import jcog.signal.MutableFloat;
import jcog.signal.NumberX;
import org.eclipse.collections.api.block.procedure.primitive.FloatProcedure;
import org.eclipse.collections.api.block.procedure.primitive.ObjectFloatProcedure;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.container.Splitting;
import spacegraph.space2d.container.Stacking;
import spacegraph.space2d.container.unit.AspectAlign;
import spacegraph.space2d.container.unit.Scale;
import spacegraph.space2d.widget.Widget;
import spacegraph.space2d.widget.text.BitmapLabel;
import spacegraph.space2d.widget.text.VectorLabel;

/**
 * Created by me on 11/18/16.
 * TODO extend Surface
 *
 * context popup menu with option to
 *   record/display history chart,
 *   record to disk etc
 *   frequency analyze
 *   predict
 *   etc
 */
public class FloatSlider extends Widget implements FloatSupplier {

    protected final SliderLabel label = new SliderLabel();
    FloatSupplier input;

    public final SliderModel slider;

    public FloatSlider(float v, float min, float max) {
        this(new FloatRange(v, min, max));
    }

    public FloatSlider(String label, float v, float min, float max) {
        this(v, min, max);
        this.label.text(label);
    }

    public FloatSlider(FloatRange f, String label) {
        this(f);
        this.label.text(label);
    }
    public FloatSlider(DoubleRange f, String label) {
        this(f);
        this.label.text(label);
    }
    public FloatSlider(FloatSliderModel f, String label) {
        this(f);
        this.label.text(label);
    }

    public FloatSlider(FloatRange f) {
        this(new DefaultFloatSlider(f.asFloat(), f.min, f.max));
        on(f::set);
        input = f;
    }
    public FloatSlider(DoubleRange f) {
        this(new DefaultFloatSlider((float)f.get(), (float)f.min, (float)f.max));
        on(f::set);
        input = ()->(float)f.get();
    }

    public FloatSlider(UnitPri f) {
        this(new DefaultFloatSlider(f.pri(), 0, 1));
        on((x, p)-> f.pri(p));
        input = f;
    }

    public FloatSlider(MutableFloat f, float min, float max) {
        this(new DefaultFloatSlider(f.asFloat(), min, max));
        on(f::set);
        input = f;
    }


    private FloatSlider(FloatSliderModel m) {
        super();

        set(new Scale(new Stacking(
            slider = m,
            label
        ), 0.85f));
    }

    public final FloatSlider text(String label) {
        this.label.text(label);
        return this;
    }

    public final FloatSlider type(SliderModel.SliderUI t) {
        slider.type(t);
        return this;
    }

    @Deprecated volatile float lastValue = Float.NaN;

    @Override
    public boolean canRender(ReSurface r) {

        slider.update();

        float nextValue = get();
        if (lastValue != nextValue) {
            label.updateValue();
            lastValue = nextValue;
        }


        return super.canRender(r);
    }


    public float get() {
        return slider.value();
    }

    public void set(float value) {
        slider.setValue(value);
        label.updateValue();
    }

    public final FloatSlider on(ObjectFloatProcedure<SliderModel> c) {
        if (input instanceof NumberX) {
            ObjectFloatProcedure<SliderModel> c0 = c;
            c = (each,x) -> {
                //chain
                ((NumberX)input).set(x);
                c0.value(each,x);
                label.updateValue();
            };
        }
        slider.on(c);
        return this;
    }


    public final FloatSlider on(FloatProcedure c) {
        return on((ObjectFloatProcedure<SliderModel>) (x, v) ->c.value(v));
    }

    @Override
    public final float asFloat() {
        return get();
    }

    public abstract static class FloatSliderModel extends SliderModel {

        @Override
        protected void starting() {
            super.starting();
            update();
        }

        public void update() {
            FloatSlider p = parentOrSelf(FloatSlider.class);
            if (p!=null) {
                FloatSupplier input = p.input; 
                if (input != null) {
                    setValue(input.asFloat());
                }
            }
        }

        public abstract float min();
        public abstract float max();

//
//        @Override
//        protected void _onChanged() {
//
//            FloatSlider parent = parent(FloatSlider.class);
//            if (parent!=null) {
//
//                FloatSupplier input = parent.input;
//                if (input instanceof NumberX) {
//                    ((NumberX) input).set(value());
//                }
//            }
//            super._onChanged();
//        }


        @Override
        protected float p(float v) {
            float min = min(), max = max();
//            min = Math.min(min, max); //HAcK
//            max = Math.max(min, max); //HAcK
            return Util.equals(min, max, Prioritized.EPSILON) ? 0.5f : (Util.clamp(v, min, max) - min) / (max - min);
        }

        @Override
        protected float v(float p) {
            return Util.lerpSafe(p, min(), max());
        }

    }

    /** with constant min/max limits */
    public static final class DefaultFloatSlider extends FloatSliderModel {

        private final float min;
        private final float max;

        DefaultFloatSlider(float v, float min, float max) {
            super();
            this.min = min;
            this.max = max;
            setValue(v);
        }

        @Override
        public float min() {
            return min;
        }

        @Override
        public float max() {
            return max;
        }
    }

    public class SliderLabel extends Splitting {
        BitmapLabel label = new BitmapLabel();
        final VectorLabel digits = new VectorLabel();
        protected String labelText = "";

        public SliderLabel() {
            set(label, 0.1f, digits)
                    .vertical()
                    .margin(0.1f);

            digits.align(AspectAlign.Align.Center);
        }

        public void text(String label) {
            if (!labelText.equals(label)) {
                synchronized (this) {
                    var l = new BitmapLabel(label);
                    setAt(0, l);
                    this.label = l;
                    //this.label.text(this.labelText = label);
                    tooltip(label);
                }
            }
        }

        public void updateValue() {
            this.digits.text(Str.n2(asFloat()));
        }
    }
}