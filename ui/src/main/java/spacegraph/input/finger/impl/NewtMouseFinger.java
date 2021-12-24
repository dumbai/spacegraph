package spacegraph.input.finger.impl;

import com.jogamp.newt.event.*;
import com.jogamp.newt.opengl.GLWindow;
import jcog.math.v2;
import spacegraph.SpaceGraph;
import spacegraph.input.finger.Finger;
import spacegraph.layer.AbstractLayer;
import spacegraph.space2d.Surface;
import spacegraph.video.JoglWindow;

import java.util.function.Function;

/**
 * ordinary desktop/laptop computer mouse, as perceived through jogamp NEWT's native interface
 */
public class NewtMouseFinger extends MouseFinger implements MouseListener, WindowListener {


    private final AbstractLayer space;
    /**
     * raw pixel coordinates from MouseEvent
     */
    private final v2 posEvent = new v2();
    private final Function<Finger, Surface> root;

    private final boolean synchronous = false;


    public NewtMouseFinger(AbstractLayer s, Function<Finger, Surface> root) {
        super(MAX_BUTTONS);
        this.space = s;
        this.root = root;

        JoglWindow w = s.window;
        GLWindow ww = w.window;
        ww.addMouseListener(0, this);
        ww.addWindowListener(this);
        if (ww.hasFocus())
            focused.set(true);

        if (synchronous)
            w.onUpdate(() -> finger());
    }

    private Surface finger() {
        return finger(root);
    }

    private void updatePosition(float px, float py) {
        posEvent.set(px, py);
        JoglWindow win = space.window;

        posPixel.set(px, win.H() - py);

        posScreen.set(win.getX() + px, win.getScreenH() - (win.getY() + py));
    }

    private void updateMoved(MouseEvent e) {
        updatePosition(e.getX(), e.getY());
        fingerAsync();
    }

    private void fingerAsync() {
        if (!synchronous) finger();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseEntered(/*@Nullable */MouseEvent e) {
        if (focused.compareAndSet(false, true))
            enter();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (focused.compareAndSet(true, false))
            exit();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isConsumed())
            return;

        short[] bd = e.getButtonsDown();
        for (int i = 0, bdLength = bd.length; i < bdLength; i++)
            bd[i] = (short) +bd[i];

        consumeIfTouching(e);

        updateButtons(e.getButtonsDown());
        fingerAsync();
    }

    private void consumeIfTouching(MouseEvent e) {
        if (touching() != null)
            e.setConsumed(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isConsumed()) return;

        short[] bd = e.getButtonsDown();
        for (int i = 0, bdLength = bd.length; i < bdLength; i++)
            bd[i] = (short) -bd[i];

        consumeIfTouching(e);

        updateButtons(bd);

        fingerAsync();
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.isConsumed()) return;

        consumeIfTouching(e);

        updateMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //if (e.isConsumed()) return;
        updateMoved(e);
    }

    @Override
    public void windowResized(WindowEvent e) {

    }

    @Override
    public void windowMoved(WindowEvent e) {

    }

    @Override
    public void windowDestroyNotify(WindowEvent e) {
        exit();
    }

    @Override
    public void windowDestroyed(WindowEvent e) {

    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        if (focused.compareAndSet(false, true))
            enter();
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        if (focused.compareAndSet(true, false))
            exit();
    }

    @Override
    public void windowRepaint(WindowUpdateEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        if (!e.isConsumed()) {
            rotationAdd(e.getRotation());
            e.setConsumed(true);
            fingerAsync();
        }
    }

    @Override
    protected void start(SpaceGraph x) {

    }

    @Override
    protected void stop(SpaceGraph x) {

    }
}