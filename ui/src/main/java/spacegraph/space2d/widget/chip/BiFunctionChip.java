package spacegraph.space2d.widget.chip;

import java.util.function.BiFunction;

public class BiFunctionChip<X,Y,Z> extends AbstractBiFunctionChip<X,Y,Z> {

    final BiFunction<X,Y,Z> f;

    public BiFunctionChip(Class<? super X> x, Class<? super Y> y, Class<? super Z> z, BiFunction<X, Y, Z> f) {
        super(x, y, z);
        this.f = f;
    }

    @Override
    protected BiFunction f() {
        return f;
    }

    @Override
    public String toString() {
        return BiFunctionChip.class.getSimpleName() + '[' + f + ']';
    }
}
