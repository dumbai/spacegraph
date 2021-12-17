package spacegraph.space2d.container;

import spacegraph.space2d.Surface;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;


/**
 * TODO extend UnitContainer
 */
public class Switching extends ContainerSurface {

    private int switched = -1;
    private Surface current;
    private Supplier<Surface>[] states;

    @SafeVarargs
    public Switching(Supplier<Surface>... states) {
        super();

        current = new EmptySurface();

        if (states.length > 0) {
            states(states);
        }
        set(0);
    }

    /**
     * sets the available states
     */
    @SafeVarargs
    private Switching states(Supplier<Surface>... states) {

        switched = -1;
        this.states = states;

        return this;
    }


    /**
     * selects the active state
     */
    public Switching set(int next) {

        if (switched == next)
            return this;

        Surface prevSurface = this.current;

        Surface nextSurface = (current = (states[switched = next].get()));
        if (prevSurface != nextSurface) {

            if (prevSurface != null)
                prevSurface.stop();


            if (parent != null) {
                nextSurface.start(this);
            }

            layout();
        }


        return this;
    }

    @Override protected void starting() {  super.starting();
                if (!current.start(this))
                    throw new RuntimeException();
    }

    @Override
    protected void stopping() {
        current.stop();
        current = null;
        super.stopping();
    }

    @Override
    public void doLayout(float dtS) {
        current.pos(bounds);
    }

    @Override
    public int childrenCount() {
        return 1;
    }

    @Override
    public void forEach(Consumer<Surface> o) {
        o.accept(current);
    }

    @Override
    public boolean whileEach(Predicate<Surface> o) {
        return o.test(current);
    }

    @Override
    public final boolean whileEachReverse(Predicate<Surface> o) {
        return whileEach(o);
    }
}
