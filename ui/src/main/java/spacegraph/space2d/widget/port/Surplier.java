package spacegraph.space2d.widget.port;

import jcog.exe.Exe;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Splitting;
import spacegraph.space2d.widget.button.CheckBox;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class Surplier<T> extends ConstantPort<T> {

    private final AtomicReference<T> built = new AtomicReference(null);

    public Surplier(String label, Class<? super T> type, Supplier<T> builder) {
        super(type);
        CheckBox toggle;
        set(toggle = new CheckBox(label));
        toggle.value(false);
        toggle.on(tb ->{
            if (tb) {
                toggle.enabled(false);
                Exe.run(()->{
                    if (!toggle.get())
                        return; //toggled off while waiting to execute

                    built.updateAndGet(x -> x == null ? builder.get() : x);
//                    if (!toggle.on()) {
//                        built.set(null);
//                        return; //toggled off while building
//                    }

                    T b = built.getOpaque();
                    set(b);
                    if (b instanceof Surface) {
                        toggle.stop();
                        set(new Splitting(toggle, 0.95f, true, (Surface)b));
                    }
                    toggle.value(true);
                    toggle.enabled(true);
                });
            } else {
                if (built.getAndSet(null)!=null) {
                    toggle.enabled(false);

//                    toggle.stop();
                    set((T) null);
//                    set(new Scale(toggle, 1));
                    set(toggle);
                    toggle.enabled(true);
                }
            }
        });
    }

    public static Surplier button(String label, Supplier<Surface> s) {
        return new Surplier<>(label, Surface.class, s);
    }
}