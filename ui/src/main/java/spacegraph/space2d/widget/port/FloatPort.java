package spacegraph.space2d.widget.port;

import jcog.math.FloatSupplier;
import jcog.signal.MutableFloat;

/** buffers the last sent value and compares with the current; transmits if inequal
 * TODO also transmit if there are new downstream connections
 * TODO abstract equality test method (ex: threshold, resolution, epsilon)
 * */
public class FloatPort extends TypedPort<Float> implements FloatSupplier {

    private final MutableFloat curValue = new MutableFloat(Float.NaN);

    public FloatPort() {
        super(Float.class);
        on(curValue::set);
    }

    /** retransmit */
    public final void out() {
        super.out(asFloat());
    }

    @Override
    public float asFloat() {
        return curValue.asFloat();
    }
}