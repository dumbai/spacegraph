package spacegraph.space2d.widget.port;

import spacegraph.space2d.Surface;
import spacegraph.space2d.container.grid.Gridding;

import java.util.function.Function;

public class PortVector extends Gridding {
    private final Port[] out;

    public PortVector(int size) {
        this(size, (x)->x);
    }

    public PortVector(int size, Function<Port, Surface> portRenderer) {
        super();

        out = new Port[size];
        Surface[] outs = new Surface[size];
        for (int i = 0; i < size; i++) {
            out[i] = new Port();
            outs[i] = portRenderer.apply(out[i]);
        }

        set(outs);
    }

    public void out(int x, Object value) {
        out[x].out(value);
    }
}
