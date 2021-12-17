package spacegraph.input.finger.impl;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import jcog.TODO;
import jcog.math.v2;
import jcog.sort.TopN;
import spacegraph.SpaceGraph;
import spacegraph.input.finger.Finger;
import spacegraph.input.key.KeyPressed;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.ContainerSurface;
import toxi.math.MathUtils;

import java.util.concurrent.atomic.AtomicReference;

import static com.jogamp.newt.event.KeyEvent.*;

/**
 * interface for NEWT keyboard. directs keyboard events to keyboard-focusable components
 */
public class NewtKeyboard extends Finger implements KeyListener {

    final AtomicReference<Surface> keyFocus = new AtomicReference<>(null);

    public NewtKeyboard() {
        super(0);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //if (!e.isAutoRepeat()) {
            setKey(e, true);
        //}
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!e.isAutoRepeat()) {
            setKey(e, false);
        }
    }

    private void setKey(KeyEvent e, boolean pressOrRelease) {
        if (e.isConsumed())
            return;

//        Surface t = finger.touching();
//        if (t != null) {
//            if (!t.key(e, pressOrRelease))
//                e.setConsumed(true);
//        }
        Surface s = keyFocus.getOpaque();
        if (s != null) {
            Surface ss = s;
            if (!ss.showing()) {
                keyFocus.compareAndSet(s, null); //free the focus
                return;
            }

            if (((KeyPressed) s).key(e, pressOrRelease)) {
                e.setConsumed(true);
                return;
            }
        }

        if (!e.isConsumed())
            this.onKey(e, pressOrRelease);
    }

    /**
     * for next-stage / global handlers
     */
    protected void onKey(KeyEvent e, boolean pressOrRelease) {
        if (!pressOrRelease) {
            switch (e.getKeyCode()) {
                case VK_LEFT -> focusTraverse(+180);
                case VK_RIGHT -> focusTraverse(0);
                case VK_UP -> focusTraverse(+90);
                case VK_DOWN -> focusTraverse(-90);
            }
        }
    }

    protected void focusTraverse(float _angle) {


        Surface x = keyFocus.get();
        if (x == null)
            return; //nothing to start with

        v2 xc = x.bounds.center();

        float angleInRadians = MathUtils.radians(_angle);

        //search neighborhood for what can be focused in the provided direction
        //TODO abstract to multi-level bfs/dfs graph search with early termination and heuristics etc
        TopN<Surface> next = new TopN<>(new Surface[1], s -> {
            v2 c = s.bounds.center();
            double dAngle = Math.abs(angleInRadians - Math.atan2(c.y - xc.y, c.x - xc.x));
            if (dAngle < Math.PI / 4 /* +-45deg */){
                return 1.0f / (1 + c.distanceSq(xc));
            } else {
                return Float.NaN;
            }
        });

        Surface p = (Surface) x.parent;
        if (p!=null) {
            ContainerSurface parent = p.parentOrSelf(ContainerSurface.class);
            parent.whileEach(c -> {
                if (c == x)
                    return true;
                if (!(c instanceof KeyPressed))
                    return true;
                next.accept(c);
                return true;
            });
        }

        Surface n = next.first();
        if (n!=null) {
            focus(n);
        }
    }


    public boolean focus(Surface s) {
        if (!(s instanceof KeyPressed ss))
            throw new UnsupportedOperationException(s + " does not implement " + KeyPressed.class);

        Surface r = keyFocus.getAndSet(s);
        if (r != s) {
            if (r != null)
                ((KeyPressed) r).keyEnd();
            ss.keyStart();
        }

        return true;
    }

    @Override
    public v2 posGlobal() {
        throw new TODO("estimate based on position of the focused element (if any)");
    }

    @Override
    protected void start(SpaceGraph x) {

    }

    @Override
    protected void stop(SpaceGraph x) {

    }

//    public Surface keyFocusSurface(Ortho.Camera cam) {
//        return new SurfaceHiliteOverlay(cam) {
//
//            @Override
//            protected Surface target() {
//                return keyFocus.getOpaque();
//            }
//
//        };
//    }
}