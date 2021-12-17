package spacegraph.space2d.widget.chip;

import jcog.data.list.Lst;
import spacegraph.space2d.widget.button.ButtonSet;
import spacegraph.space2d.widget.button.CheckBox;
import spacegraph.space2d.widget.button.ToggleButton;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class FunctionSelectChip<X,Y> extends AbstractFunctionChip<X,Y> {

    final Map<String, Function<X, Y>> ff = new TreeMap();

    private volatile Function<X, Y> f = (x) -> null;

    public FunctionSelectChip(Class<? super X> x, Class<? super Y> y, Map<String,Function<X,Y>> m) {
        super(x, y);

        ff.putAll(m);


        List<ToggleButton> fb = new Lst(m.size());
        for (Map.Entry<String, Function<X, Y>> entry : ff.entrySet()) {
            String n = entry.getKey();
            Function<X, Y> value = entry.getValue();
            fb.add(new CheckBox(n).on(() -> FunctionSelectChip.this.f = value));
        }

        ButtonSet selector = new ButtonSet(ButtonSet.Mode.One, fb);

        set(in, selector, out);
    }
    @Override
    protected Function<X, Y> f() {
        return f;
    }
}
