package spacegraph.input.finger.state;

import spacegraph.input.finger.Finger;
import spacegraph.input.finger.Fingering;
import spacegraph.space2d.Surface;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Clicking extends Fingering {

    private final Predicate<Finger> pressable;

    public Clicking(int fingerButton, Surface surface, Consumer<Finger> clicked, Runnable armed, Runnable hover, Runnable becameIdle) {
        pressable = clicked(surface, fingerButton, clicked, armed, hover, becameIdle);
    }

    public static Predicate<Finger> clicked(Surface what, int button, Consumer<Finger> clicked, Runnable armed, Runnable hover, Runnable becameIdle) {

        if (becameIdle != null)
            becameIdle.run();

        return new Predicate<>() {

            final AtomicBoolean idle = new AtomicBoolean(false);

            @Override
            public boolean test(Finger f) {

                if (f != null /*&& (what = f.touching()) != null*/) {

                    idle.set(false);

                    if (f.clickedNow(button, what)) {

                        if (clicked != null)
                            clicked.accept(f);

                    } else if (f.pressed(button)) {
                        if (armed != null)
                            armed.run();

                    } else {
                        if (hover != null)
                            hover.run();
                    }

                } else {
                    if (idle.compareAndSet(false, true)) {
                        if (becameIdle != null)
                            becameIdle.run();
                    }
                }
                return false;
            }
        };
    }

    @Override
    protected final boolean start(Finger f) {
        return pressable.test(f);
    }

    @Override
    public final boolean defer(Finger finger) {
        return true; //transient
    }
}