package spacegraph.input.finger.state;

import jcog.math.v2;
import spacegraph.input.finger.Finger;
import spacegraph.layer.AbstractLayer;

/** finger move trigger, using screen pixel scale */
public abstract class FingerMoveWindow extends FingerMove {

    public FingerMoveWindow(int button) {
        super(button);
    }

//    public FingerMovePixels(int button, boolean xAxis, boolean yAxis) {
//
//        super(button, xAxis, yAxis);
//    }

    protected volatile int xStart;
    protected volatile int yStart;


    protected abstract AbstractLayer window();


    @Override
    protected boolean starting(Finger f) {
        AbstractLayer w = window();
        xStart = w.window.getX();
        yStart = w.window.getY();
        return super.starting(f);
    }


    @Override public final v2 pos(Finger finger) {
        return finger.posScreen;
    }
}
