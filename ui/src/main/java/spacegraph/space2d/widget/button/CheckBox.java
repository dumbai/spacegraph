package spacegraph.space2d.widget.button;

import org.eclipse.collections.api.block.procedure.primitive.BooleanProcedure;
import org.eclipse.collections.api.block.procedure.primitive.ObjectBooleanProcedure;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.Bordering;
import spacegraph.space2d.widget.text.AbstractLabel;
import spacegraph.space2d.widget.text.BitmapLabel;
import spacegraph.space2d.widget.text.VectorLabel;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by me on 11/12/16.
 */
public class CheckBox extends ToggleButton {

    public CheckBox(String text) {
        this(text, false);
    }
    public CheckBox(String text, boolean value) {
        set(new Bordering(new BitmapLabel(text)).west(new VectorLabel()));
        text(text);
        value(value);
        if (!value) valueChanged(); //force initial update
    }

    public CheckBox(String text, Runnable r) {
        this(text, b -> { if (b) r.run(); } );
    }
    public CheckBox(String text, BooleanProcedure b) {
        this(text);
        on((a, e) -> b.value(e));
    }

    public CheckBox(String text, ObjectBooleanProcedure<ToggleButton> on) {
        this(text);
        on(on);
    }
    public CheckBox(String text, AtomicBoolean b) {
        this(text, b.get());
        on((button, value) -> b.set(value));
    }

    @Override
    protected boolean canRender(ReSurface r) {
        if (get())
            pri(0.5f);

        return super.canRender(r);
    }

    @Override
    protected void valueChanged() {
        Surface w = ((Bordering) the()).west();
        if (w!=null)
            ((AbstractLabel) w).text(get() ? "[+]" : "[ ]");
    }

    @Override
    public CheckBox text(String s) {
        Bordering b = (Bordering) the();
        ((AbstractLabel)((b.center()))).text(s);

        int sl = s.length();
        b.borderSize(Bordering.W, sl > 0 ? 4f/(sl + 3) : 1); //HACK maintain equivalent text size of label and checkbox icon

        return this;
    }
}