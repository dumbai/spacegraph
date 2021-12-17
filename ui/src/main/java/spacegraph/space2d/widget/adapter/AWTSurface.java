package spacegraph.space2d.widget.adapter;

import com.jogamp.newt.event.KeyEvent;
import jcog.event.Off;
import jcog.math.v2;
import spacegraph.input.finger.Finger;
import spacegraph.input.key.impl.Keyboard;
import spacegraph.space2d.Surface;
import spacegraph.space2d.widget.Widget;
import spacegraph.util.AWTCamera;
import spacegraph.video.Tex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.event.KeyEvent.VK_UNDEFINED;
import static java.awt.event.MouseEvent.NOBUTTON;

public class AWTSurface extends Widget {

    private static Method processKeyEvent;

    static {
        try {
            processKeyEvent = Component.class.getDeclaredMethod("processKeyEvent", java.awt.event.KeyEvent.class);
            processKeyEvent.trySetAccessible();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    private Component component;
    private final Tex tex = new Tex();
    private final Dimension psize;
    private BufferedImage buffer;
    private int lpx = -1;
    private int lpy = -1;
    private Off ons;
    private volatile Component myFocus;

    public AWTSurface(Component component) {
        this(component, component.getWidth(), component.getHeight());
    }

    AWTSurface(Component component, int pw, int ph) {

        this.component = component;
        this.psize = new Dimension(pw, ph);

    }

    @Override
    protected void starting() {

        super.starting();

        if (component instanceof JFrame) {
            component.setVisible(false);
            component = ((RootPaneContainer) component).getRootPane();
        }


        Window frame = new MyFrame();


        component.addNotify();


        component.setPreferredSize(psize);

        component.setSize(psize);
        frame.pack();

        component.setVisible(true);


        component.validate();


        set(tex.view());


        AtomicBoolean busy = new AtomicBoolean(false);
        ons = root().onUpdate(w -> {

            if (!busy.compareAndSet(false, true))
                return;


            SwingUtilities.invokeLater(() -> {
                try {
                    buffer = AWTCamera.get(component, buffer);
                    tex.set(buffer);
                } finally {
                    busy.set(false);
                }
            });

        });
    }

    @Override
    protected void stopping() {
        ons.close();
        ons = null;
        super.stopping();
    }

    @Override
    public boolean key(KeyEvent e, boolean pressedOrReleased) {
        int code = Keyboard.newtKeyCode2AWTKeyCode(e.getKeyCode());

        /*
         * @param source    The {@code Component} that originated the event
         * @param id              An integer indicating the type of event.
         *                  For information on allowable values, see
         *                  the class description for {@link KeyEvent}
         * @param when      A long integer that specifies the time the event
         *                  occurred.
         *                     Passing negative or zero value
         *                     is not recommended
         * @param modifiers The modifier keys down during event (shift, ctrl,
         *                  alt, meta).
         *                     Passing negative value
         *                     is not recommended.
         *                     Zero value means that no modifiers were passed.
         *                  Use either an extended _DOWN_MASK or old _MASK modifiers,
         *                  however do not mix models in the one event.
         *                  The extended modifiers are preferred for using
         * @param keyCode   The integer code for an actual key, or VK_UNDEFINED
         *                  (for a key-typed event)
         *  public KeyEvent(Component source, int id, long when, int modifiers, int keyCode) {
         */


        if (myFocus != null) {
            Component m = myFocus;
            int modifers = 0;
            Component src = component;
            if (pressedOrReleased && e.isPrintableKey()) {

                try {
                    processKeyEvent.invoke(m, new java.awt.event.KeyEvent(src,
                            java.awt.event.KeyEvent.KEY_TYPED,
                            System.currentTimeMillis(),
                            modifers, VK_UNDEFINED, e.getKeyChar()
                    ));
                } catch (IllegalAccessException | InvocationTargetException e1) {
                    e1.printStackTrace();
                }
            }

            try {
                processKeyEvent.invoke(m, new java.awt.event.KeyEvent(src,
                        pressedOrReleased ? java.awt.event.KeyEvent.KEY_PRESSED : java.awt.event.KeyEvent.KEY_RELEASED,
                        System.currentTimeMillis(),
                        modifers, code, e.getKeyChar()
                ));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }


        return false;
    }

    @Override
    public Surface finger(Finger f) {
        Surface s = super.finger(f);
        if (s == this) {
            awtFinger(f);
        }
        return s;
    }


    /**
     * TODO re-test
     */
    public void awtFinger(Finger finger) {
        boolean wasTouching = false; //isTouched();


        long now = System.currentTimeMillis();
        if (finger == null) {
            if (wasTouching) {
                handle(new MouseEvent(component,
                        MouseEvent.MOUSE_EXITED,
                        now, 0,
                        lpx, lpy, lpx, lpy, 0, false, NOBUTTON
                ));
            }
            lpx = lpy = -1;
            return;
        }

        v2 rp = finger.posGlobal();
        int px = Math.round(rp.x * component.getWidth());
        int py = Math.round((1.0f - rp.y) * component.getHeight());
        if (lpx == -1) {
            lpx = px;
            lpy = py;
        }
        if (!wasTouching) {
            handle(new MouseEvent(component,
                    MouseEvent.MOUSE_ENTERED,
                    now, 0,
                    px, py, px, py, 0, false, NOBUTTON)
            );
        }


        Component target = SwingUtilities.getDeepestComponentAt(this.component, px, py);
        if (target == null)
            target = this.component;
        else {
            px -= target.getX();
            py -= target.getY();
        }


        if (finger.pressed(0) && !finger.prevButtonDown.test(0)) {
            handle(new MouseEvent(target,
                    MouseEvent.MOUSE_PRESSED,
                    now, InputEvent.BUTTON1_DOWN_MASK,
                    px, py, px, py, 0, false, MouseEvent.BUTTON1
            ));
        }
        if (!finger.pressed(0) && finger.prevButtonDown.test(0)) {
            handle(new MouseEvent(target,
                    MouseEvent.MOUSE_RELEASED,
                    now, InputEvent.BUTTON1_DOWN_MASK,
                    px, py, px, py, 0, false, MouseEvent.BUTTON1
            ));
            handle(new MouseEvent(target,
                    MouseEvent.MOUSE_CLICKED,
                    now, InputEvent.BUTTON1_DOWN_MASK,
                    px, py, px, py, 1, false, MouseEvent.BUTTON1
            ));
        }

        boolean moved = lpx != px || lpy != py;


        if (finger.pressed(0)) {

            if (moved && finger.prevButtonDown.test(0)) {
                handle(new MouseEvent(target,
                        MouseEvent.MOUSE_DRAGGED,
                        now, 0,
                        px, py, px, py, 0, false, MouseEvent.BUTTON1
                ));
            }
            if (!finger.prevButtonDown.test(0) &&
                    target.isFocusable() && !target.hasFocus()) {


                myFocus = target;


            }
        } else {
            if (moved) {
                handle(new MouseEvent(target,
                        MouseEvent.MOUSE_MOVED,
                        now, 0,
                        px, py, px, py, 0, false, NOBUTTON
                ));
            }
        }


    }

    private static void handle(AWTEvent e) {


        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
    }


    private static class MyFrame extends Window {
        MyFrame() throws HeadlessException {
            super(null);
        }

        @Override
        public boolean isVisible() {
            return true;
        }

        @Override
        public boolean isFocusable() {
            return true;
        }


        @Override
        public boolean isShowing() {
            return true;
        }
    }

}