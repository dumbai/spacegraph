package spacegraph.space2d.widget.port;

import com.jogamp.opengl.GL2;
import org.eclipse.collections.api.block.procedure.primitive.BooleanProcedure;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.container.unit.Scale;
import spacegraph.space2d.widget.button.CheckBox;
import spacegraph.video.Draw;

/** TODO include and enforce debounce and latch duration parameters */
public class TogglePort extends BoolPort {

    private final CheckBox toggle;

    public TogglePort() {
        this("");
    }

    private TogglePort(String label) {
        this(label, true);
    }

    private TogglePort(String label, boolean initially) {
        super();

        toggle = new CheckBox(label) {
            @Override
            protected void paintIt(GL2 gl, ReSurface r) {
                //transparent
            }
        };
        toggle.on((BooleanProcedure) this::out);
        on(toggle::set);

        set(new Scale(toggle.set(initially), 0.75f));
    }

    public final TogglePort set(boolean s) {
        toggle.value(s);
        return this;
    }

    @Override
    protected void paintIt(GL2 gl, ReSurface r) {
        if (toggle.get()) {
            gl.glColor4f(0.0f,0.75f,0.1f,0.8f);
        } else {
            gl.glColor4f(0.75f,0.25f, 0.0f,0.7f);
        }
        Draw.rect(bounds, gl);
    }
}