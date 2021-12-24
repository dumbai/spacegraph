package spacegraph.space2d.widget.button;

import com.jogamp.newt.event.KeyEvent;
import org.jetbrains.annotations.Nullable;
import spacegraph.input.finger.Finger;
import spacegraph.input.finger.state.Clicking;
import spacegraph.space2d.Surface;
import spacegraph.space2d.widget.Widget;
import spacegraph.space2d.widget.text.AbstractLabel;
import spacegraph.space2d.widget.text.BitmapLabel;
import spacegraph.space2d.widget.text.VectorLabel;
import spacegraph.video.ImageTexture;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO abstract to FocusableWidget
 */
public abstract class AbstractButton extends Widget {

    static final int CLICK_BUTTON = 0;

    private final Clicking click = new Clicking(CLICK_BUTTON,this, this::fingered,
            () -> dz = 0.5f, () -> dz = 0.0f, () -> dz = 0.0f);

    private final AtomicBoolean enabled = new AtomicBoolean(true);

    private void fingered(Finger f) {
        dz = 0;
        onClick(f);
    }

    protected AbstractButton() {
        super();
    }

    protected AbstractButton(Surface content) {
        super(content);
    }

    @Override
    public Surface finger(Finger finger) {
        Surface result = this;
        boolean finished = false;
        Surface f = super.finger(finger);
        if (f == null || f == this) {
            if (enabled() && finger.test(click)) {
                result = this;
                finished = true;
            } else {
                if (finger.dragging(0) || finger.dragging(1)) {
                    //allow pass-through for drag actions
                    result = null;
                    finished = true;
                }
            }


        }
        if (!finished)
            result = f;

        return result;
    }

    public AbstractButton icon(String s) {
        set(new ImageTexture(s).view());
        return this;
    }

    @Nullable public String text() {
        Surface g = the();
        return g instanceof AbstractLabel l ? l.text() : null;
    }

    public AbstractButton text(String s) {

        if (Objects.equals(text(), s))
            return this;

        set(

                s.length() < 32 ? new BitmapLabel(s) : new VectorLabel(s)
                //new BitmapLabel(s)
                //new VectorLabel(s)
        );

        tooltip(s);

        return this;
    }


    public final boolean enabled() {
        return enabled.getOpaque();
    }

    public final <B extends AbstractButton> B enabled(boolean e) {
        enabled.set(e);
        return (B)this;
    }

    protected abstract void onClick();

    /** when clicked by finger */
    protected void onClick(Finger f) {
        if (enabled())
            onClick();
    }

    /** when clicked by key press */
    private void onClick(KeyEvent key) {
        if (enabled() && clickKeycode(key.getKeyCode()))
            onClick();
    }

    private boolean clickKeycode(int keyCode) {
        return keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_ENTER;
    }

    @Override
    public boolean key(KeyEvent e, boolean pressedOrReleased) {
        if (!super.key(e, pressedOrReleased)) {
            if (pressedOrReleased && clickKeycode(e.getKeyCode())) {
                onClick(e);
                return true;
            }

        }
        return false;
    }

}