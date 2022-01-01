package spacegraph.space2d.widget.text;

import spacegraph.space2d.container.EmptyContainer;
import spacegraph.space2d.container.unit.AspectAlign;
import spacegraph.util.math.Color4f;

import java.util.Objects;

public abstract class AbstractLabel extends EmptyContainer {

    protected volatile String text;

    public final Color4f fgColor = new Color4f(1, 1, 1, 1);
    public final Color4f bgColor = new Color4f(0,0,0,0 /* alpha zero */);

    /** default character visual aspect ratio */
    public static final float characterAspectRatio = 1.8f;

    public AspectAlign.Align align = AspectAlign.Align.Center;

    public final AbstractLabel align(AspectAlign.Align a) {
        this.align = a;
        return this;
    }

    public final AbstractLabel text(String t) {
        if (!Objects.equals(t, text)) {
            this.text = t;
            textChanged();
            layout();
        }
        return this;
    }

    protected void textChanged() {

    }

    public final String text() { return text; }

    @Override
    public String toString() {
        return "Label[" + text + ']';
    }


}