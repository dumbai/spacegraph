package spacegraph.space2d.widget.chip;

import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.container.unit.MutableUnitContainer;
import spacegraph.space2d.widget.button.CheckBox;

import java.util.function.Function;
import java.util.function.Supplier;

import static spacegraph.space2d.container.Bordering.S;

public class ExpandingChip extends MutableUnitContainer {

    public final Function<ExpandingChip, Surface> builder;
    private final CheckBox button;

    public ExpandingChip(String label, Supplier<Surface> builder) {
        this(label, (x)->builder.get());
    }

    public ExpandingChip(String label, Function<ExpandingChip,Surface> builder) {
        super();

        this.builder = builder;
        this.button = new CheckBox(label);

        button.on((state)->{
            synchronized (ExpandingChip.this) {
                if (state) {
                    button.delete();
                    set(new Bordering(builder.apply(this)).set(S, button));
                } else {
                    button.delete();
                    set(button);
                }
            }
        });

        set(button);
    }

}
