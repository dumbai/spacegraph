package spacegraph.space2d.widget.button;

import jcog.Log;
import org.eclipse.collections.api.block.procedure.primitive.BooleanProcedure;
import org.eclipse.collections.api.block.procedure.primitive.ObjectBooleanProcedure;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.EmptySurface;
import spacegraph.video.ImageTexture;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by me on 11/12/16.
 */
public class ToggleButton extends AbstractButton {

    final AtomicBoolean on = new AtomicBoolean(false);

    public @Nullable ObjectBooleanProcedure<ToggleButton> action;

    ToggleButton() {
        this(false);
    }

    private ToggleButton(boolean startingValue) {
        super(new EmptySurface());
        on.set(startingValue);
    }

    public ToggleButton(Surface view) {
        super(view);
    }


    protected ToggleButton(ObjectBooleanProcedure<ToggleButton> action) {
        this();
        on(action);
    }

    public static IconToggleButton iconAwesome(String icon) {
        return new IconToggleButton(ImageTexture.awesome(icon));
    }

    private static final Logger logger = Log.log(ToggleButton.class);

    public final ToggleButton set(boolean on) {
        value(on);
        return this;
    }

    /** returns true if changed */
    public final boolean value(boolean value) {
        if (this.on.compareAndSet(!value, value)) {
            if (action != null) {
                //Exe.invoke(()->{

                try {
                    action.value(this, value);
                } catch (RuntimeException t) {
                    this.on.set(!value);
                    logger.error("revert {} {}", this, t);
                }

                //});
            }
            valueChanged();
            return true;
        }
        return false;
    }

    protected void valueChanged() {

    }

    public final boolean get() {
        return on.getOpaque();
    }

    public <T extends ToggleButton> T on(Runnable a) {
        return on((x)->{ if (x) a.run(); });
    }

    public <T extends ToggleButton> T on(BooleanProcedure a) {
        return on((thizz, x)->a.value(x));
    }

    public <T extends ToggleButton> T on(ObjectBooleanProcedure<ToggleButton> a) {
        this.action = a;
        return (T) this;
    }

    @Override
    protected void onClick() {
        set(!get());
    }

}