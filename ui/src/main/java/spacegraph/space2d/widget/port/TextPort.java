package spacegraph.space2d.widget.port;

import java.util.concurrent.atomic.AtomicReference;

public class TextPort extends EditablePort<String> {

    private final AtomicReference<String> val = new AtomicReference();

    public TextPort() {

        super("", String.class);
        on(s ->{
            boolean[] changed = {false};
            val.accumulateAndGet(s, (p, n) -> {
                if (p != null && n.equals(p)) {
                    changed[0] = true;
                    return p;
                } else
                    return s;
            });
            if (changed[0]) {

            }
        });
    }

    @Override
    protected String parse(String x) {
        return x;
    }
}
