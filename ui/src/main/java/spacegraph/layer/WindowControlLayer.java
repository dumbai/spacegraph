package spacegraph.layer;

import jcog.TODO;
import spacegraph.input.finger.Fingering;
import spacegraph.input.finger.impl.NewtMouseFinger;
import spacegraph.input.finger.state.FingerMoveWindow;
import spacegraph.input.finger.state.FingerResizeWindow;
import spacegraph.space2d.Surface;
import spacegraph.space2d.container.EmptySurface;
import spacegraph.video.JoglWindow;

public class WindowControlLayer extends AbstractLayer {
    private static final short MOVE_AND_RESIZE_BUTTON = 1;

    /* {
        @Override public Surface finger(Finger finger) {
            //check windowResize first since it is a more exclusive condition than windowMove
            if (finger.test(windowResize) || finger.test(windowMove))
                return null;
            return super.finger(finger);
        }
    };*/

    private final Fingering windowResize = new FingerResizeWindow(this, MOVE_AND_RESIZE_BUTTON) {
        static {
            new TODO();
        }

        @Override
        public Surface touchNext(Surface prev, Surface next) {
            return null;
        }
    };

    private final Fingering windowMove = new FingerMoveWindow(MOVE_AND_RESIZE_BUTTON) {

        @Override
        protected AbstractLayer window() {
            return WindowControlLayer.this;
        }

        @Override
        public void move(float dx, float dy) {
            int nx = Math.round(xStart + dx);
            int ny = Math.round(yStart - dy);
            if (nx != xStart || ny != yStart)
                window.setPosition(nx, ny);
        }

        @Override
        public Surface touchNext(Surface prev, Surface next) {
            return null;
        }
    };

    @Override
    public boolean changed() {
        return false;
    }

    public WindowControlLayer(JoglWindow w) {
        setWindow(w);
        Surface dummy = new EmptySurface();
        w.runLater(()-> addFinger(new NewtMouseFinger(this, f -> {
            if (f.test(windowMove))
                return dummy;
            return null;
        })));
    }

}