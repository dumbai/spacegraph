package spacegraph.space2d.widget.chip;

import spacegraph.space2d.container.grid.Gridding;
import spacegraph.space2d.widget.port.TypedPort;
import spacegraph.space2d.widget.text.Labelling;

import java.util.function.Function;

public abstract class AbstractFunctionChip<X,Y> extends Gridding {
    protected final TypedPort<X> in;
    protected final TypedPort<Y> out;

    protected AbstractFunctionChip(Class<? super X> cx, Class<? super Y> cy) {
        super();
        out = new TypedPort<>(cy);
        in = new TypedPort<>(cx, (X x) -> {
            try {
                Y y = f().apply(x);
                if (y != null)
                    out.out(y);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        set(Labelling.awesome(out, "angle-right"), Labelling.awesome(in, "question-circle"));
    }

    protected abstract Function<X, Y> f();

}