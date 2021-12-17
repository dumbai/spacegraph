package spacegraph.space2d.widget.chip;

import spacegraph.space2d.widget.meter.Plot2D;
import spacegraph.space2d.widget.port.TypedPort;

public class PlotChip extends TypedPort<Number> {
    private final Plot2D plot;
    double nextValue = Double.NaN;

    public PlotChip() {
        super(Number.class);

        this.plot = new Plot2D(256);
        plot.add("x", ()->nextValue);

        set(plot);

        on(x ->{
            nextValue = x.floatValue();
            if (nextValue==nextValue)
                plot.update();
        });

    }
}