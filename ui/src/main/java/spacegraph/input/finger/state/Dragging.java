package spacegraph.input.finger.state;

import spacegraph.input.finger.Finger;
import spacegraph.input.finger.Fingering;

public abstract class Dragging extends Fingering {

    public final int button;
    private boolean active = false;

    public Dragging(int button) {
        super();
        this.button = button;
    }

    @Override
    public final boolean start(Finger f) {
        return (active = (pressedNow(f) && starting(f) && drag(f)));
    }

    protected boolean starting(Finger f) {
        return true;
    }

    @Override
    public boolean defer(Finger finger) {
        return !finger.pressed(button);
    }

    @Override
    public boolean escapes() {
        return true;
    }

    @Override
    public void stop(Finger finger) {
        active = false;
    }

    public final boolean active() {
        return active;
    }

    private boolean pressed(Finger f) {
        return f.pressed(button);
    }
    private boolean pressedNow(Finger f) {
        return f.pressedNow(button);
    }

    @Override
    public boolean update(Finger finger) {
        return active && pressed(finger) && drag(finger);
    }

    protected abstract boolean drag(Finger f);

}
