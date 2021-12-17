package spacegraph.space2d.widget.button;

import org.jetbrains.annotations.Nullable;
import spacegraph.space2d.Surface;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.video.ImageTexture;

import java.util.function.Consumer;

import static spacegraph.space2d.container.grid.Containers.col;

public class PushButton extends AbstractButton  {


    private @Nullable Consumer<PushButton> onClick;

    public PushButton() {
        super();
    }

    public PushButton(String s) {
        this();
        text(s);
    }

    public PushButton(Surface content) {
        super(content);
    }

    public PushButton(Consumer<PushButton> clicked) {
        this();
        clicked(clicked);
    }
    public PushButton(Surface s, Runnable clicked) {
        this(s);
        clicked(clicked);
    }
    public PushButton(String s, Runnable clicked) {
        this(s);
        clicked(clicked);
    }

    public PushButton(ImageTexture s, Runnable clicked) {
        this(s);
        clicked(clicked);
    }

    public PushButton(ImageTexture s) {
        this(s.view());
    }

    /** button with FontAwesome icon */
    public static PushButton iconAwesome(String icon) {
        return new PushButton(ImageTexture.awesome(icon).view(1));
    }
    /** button with FontAwesome icon + label above */
    public static PushButton iconAwesome(String icon, String label) {
        return new PushButton(col(new VectorLabel(label), 0.1f, ImageTexture.awesome(icon).view(1)));
    }

    public final PushButton clicked(@Nullable Runnable onClick) {
        return clicked(onClick!=null ? pb->onClick.run() : null);
    }

    public final PushButton clicked(@Nullable Consumer<PushButton> onClick) {
        this.onClick = onClick;
        return this;
    }

    @Override
    protected final void onClick() {
        Consumer<PushButton> c = this.onClick;
        if (c !=null)
            c.accept(this);
    }

}