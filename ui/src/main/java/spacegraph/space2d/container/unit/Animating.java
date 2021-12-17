package spacegraph.space2d.container.unit;

import jcog.event.Off;
import jcog.exe.Exe;
import jcog.math.FloatSupplier;
import spacegraph.layer.OrthoSurfaceGraph;
import spacegraph.space2d.Surface;
import spacegraph.util.animate.Animated;

import java.util.function.Consumer;

public class Animating<X extends Surface> extends UnitContainer<X> implements Animated {

    private final FloatSupplier minUpdatePeriod;
    private final Consumer<X> each;
    private float waiting;
    private Off animate;

    /** in seconds.  TODO adjustable */
    static final float maxLag = 0.01f;

    public Animating(X the, Runnable each, float minUpdatePeriod) {
        this(the, (z)->each.run(), () -> minUpdatePeriod);
    }

    public Animating(X the, Runnable each, FloatSupplier minUpdatePeriod) {
        this(the, (z)->each.run(), minUpdatePeriod);
    }

    public Animating(X the, Consumer<X> each, float minUpdatePeriod) {
        this(the, each, ()->minUpdatePeriod);
    }

    public Animating(X the, Consumer<X> each, FloatSupplier minUpdatePeriod) {
        super(the);
        this.minUpdatePeriod = minUpdatePeriod;
        this.each = each;
    }

    @Override
    protected void starting() {
        super.starting();
        waiting = minUpdatePeriod.asFloat();
        animate = ((OrthoSurfaceGraph)root()).onUpdate(this);
    }

    @Override
    protected void stopping() {
//        if (animate!=null) {
            animate.close();
            animate = null;
//        }
        waiting = 0;
        super.stopping();
    }

    @Override
    public boolean animate(float dt) {
        if (parent!=null) {
            if (showing()) {
                if (animate == null) {
                    animate = root().animate(this);
                }
                waiting -= dt;
                if (waiting < 0) {
                    boolean async = false;
                    if (async) {
                        Exe.run(this::update);
                    } else {
                        update();
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void update() {
        each.accept(the);
        waiting = Math.max(waiting, -maxLag) + minUpdatePeriod.asFloat();
    }

}