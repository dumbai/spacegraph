package spacegraph.space2d.meta;

import jcog.event.Off;
import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.unit.UnitContainer;

import java.util.function.Consumer;
import java.util.function.Function;


/**
 * Triggering functions in one of 2 modes:
 *      triggers (and registers the trigger with some external source) children w/ optional supplied update function,
 *      or is triggered by a parent trigger to execute a supplied update function.
 * */
public class Triggering<X extends Surface> extends UnitContainer<X> {

    @Nullable
    private final Function<Runnable, Off> trigger;
    private final Consumer<X> update;
    private transient Off on;

    /** whether to invoke update if invisible */
    private boolean invisibleUpdate = false;

    public Triggering(@Nullable Function<Runnable, Off> trigger, X surface) {
        this(surface, trigger, (Consumer)null);
    }

    public Triggering(X surface, @Nullable Function<Runnable, Off> trigger, Runnable update) {
        this(surface, trigger, x->update.run());
    }

    public Triggering(X surface, Consumer<X> update) {
        this(surface, null, update);
    }

    public Triggering(X surface, @Nullable Function<Runnable, Off> trigger, @Nullable Consumer<X> update) {
        super(surface);
        this.trigger = trigger;
        this.update = update;
    }

    public final Triggering<X> invisibleUpdate(boolean b) {
        this.invisibleUpdate = b;
        return this;
    }
    @Override
    protected void starting() {
        super.starting();
        if (this.trigger!=null)
            this.on = trigger.apply(this::update);
    }

    @Override
    protected void stopping() {
        if (on!=null) {
            on.close();
            on = null;
        }
        super.stopping();
    }

    protected final void update() {

        if (invisibleUpdate && !visible())
            return;

        if (update!=null)
            update.accept(the);

        if (this.trigger!=null) {
            //scan children recursively for dependent TriggeredSurfaces
            //TODO cache the children to a list, and invalidate on re-layout
            forEachRecursively(s -> {
                if (s instanceof Triggering t) {
                    if (t.trigger==null)
                        t.update();
                }
            });
        }
    }

}