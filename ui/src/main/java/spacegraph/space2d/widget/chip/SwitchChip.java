package spacegraph.space2d.widget.chip;

import spacegraph.space2d.Surface;
import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.port.IntPort;
import spacegraph.space2d.widget.port.Port;
import spacegraph.space2d.widget.port.PortVector;

import java.util.function.Function;

/** demultiplexer/decoder.  int to boolean outputs */
public class SwitchChip extends Gridding {

    final PortVector out;

    int prev = -1;

    public SwitchChip(int size) {
        this(size, (p) -> p);
    }

    /**
     * port renderer can be used for example to create enable-button decorated ports
     */
    public SwitchChip(int size, Function<Port, Surface> portRenderer) {
        super();

        Port in = new IntPort(this::trigger);

        set(in, out = new PortVector(size, portRenderer));
    }

    /** TODO different modes: adhoc, exclusive, etc */
    public synchronized void trigger(int next) {
        if (next!=prev) {

            if (prev >= 0) {
                out.out(prev, Boolean.FALSE);
            }

            out.out(next, Boolean.TRUE);
            prev = next;
        }
    }

}
