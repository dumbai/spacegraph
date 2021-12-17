package spacegraph.space2d.widget.slider;

import jcog.Util;
import jcog.signal.MutableInteger;
import spacegraph.space2d.container.Splitting;
import spacegraph.space2d.widget.Widget;
import spacegraph.space2d.widget.button.PushButton;
import spacegraph.space2d.widget.text.AbstractLabel;
import spacegraph.space2d.widget.text.VectorLabel;

import java.util.function.IntFunction;

public class IntSpinner extends Widget {

    private final int min;
    private final int max;
    private final AbstractLabel label;
    private final MutableInteger i;
    private final IntFunction<String> labeller;

    public IntSpinner(MutableInteger i, IntFunction<String> labeller, int min, int max) {
        this.min = min;
        this.max = max;
        this.i = i;
        this.labeller = labeller;
        set(
            new Splitting(
                label = new VectorLabel(),
                    0.8f, false, new Splitting(
                    new PushButton("+", ()-> update(+1)),
                        0.5f, new PushButton("-", ()-> update(-1)))
            )
        );
        update(0);
    }

    private void update(int delta) {
        synchronized (i) {
            set(i.intValue() + delta);
        }
    }

    public void set(int nextValue) {
        synchronized (i) {
            nextValue = Util.clamp(nextValue, min, max);
            label.text(labeller.apply(nextValue));
            i.set(nextValue);
        }
    }

}