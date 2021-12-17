package spacegraph.input.finger.impl;

import spacegraph.input.finger.Finger;
import spacegraph.input.finger.Fingering;
import spacegraph.space2d.Surface;

import java.util.function.Function;

public abstract class MouseFinger extends Finger {

    protected MouseFinger(int buttons) {
        super(buttons);
    }

    /** called for each layer. returns true if continues down to next layer*/
    public Surface finger(Function<Finger, Surface> s) {

        posGlobal.set(posPixel); //HACK

        Fingering ff = this.fingering.get();

        Surface touchNext = s.apply(this);

        //touching.accumulateAndGet(touchNext, ff::touchNext);
        touching.set(ff.touchNext(touching.get(), touchNext));

        commitButtons();
        clearRotation();

        return touchNext;
    }

//    private void _update() {
//
//        if (!focused.get())
//            return;
//
//        if (updating.compareAndSet(false, true)) {
//            try {
//                update();
//            } finally {
//                updating.set(false);
//            }
//        } else {
//            /* busy */
//            logger.warn("{} event update already in-progress", this);
//        }
//    }

    @Override
    public Surface cursorSurface() {
        return new CursorSurface(this);
    }


//    final static Logger logger = LoggerFactory.getLogger(MouseFinger.class);
    static final int MAX_BUTTONS = 5;

}