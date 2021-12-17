package spacegraph.input.finger.state;

import jcog.math.v2;
import spacegraph.input.finger.Finger;

public abstract class FingerMove extends Dragging {

    private final float xSpeed;
    private final float ySpeed;
    protected final v2 start = new v2();

    protected FingerMove(int button) {
        this(button, true, true);
    }

    /** for locking specific axes */
    FingerMove(int button, boolean xAxis, boolean yAxis) {
        this(button, xAxis ? 1 : 0, yAxis ? 1 : 0);
        assert(xAxis || yAxis);
    }

    private FingerMove(int button, float xSpeed, float ySpeed) {
        super(button);
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
    }

    /** internal coordinate vector */
    public v2 pos(Finger finger) {
        return finger.posGlobal();
    }

    /** accepts an iterational position adjustment */
    protected abstract void move(float tx, float ty);

    /** whether the position adjustment is tracked incrementally (delta from last iteration),
     * or absolutely (delta from initial conditions) */
    protected boolean incremental() {
        return false;
    }

    @Override
    protected boolean drag(Finger f) {

        v2 next = pos(f);
        if (next !=null) {


            float tx = xSpeed != 0 ? (next.x - start.x) * xSpeed : 0;
            float ty = ySpeed != 0 ? (next.y - start.y) * ySpeed : 0;

            if (incremental())
                start.set(next);

            if (tx!=0 || ty!=0)
                move(tx, ty);

            return true;
        }

        return false;
    }

    @Override
    protected boolean starting(Finger f) {
        this.start.set(pos(f));
        return super.starting(f);
    }



}
