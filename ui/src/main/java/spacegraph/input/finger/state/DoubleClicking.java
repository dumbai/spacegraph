package spacegraph.input.finger.state;

import jcog.math.v2;
import spacegraph.input.finger.Finger;
import spacegraph.space2d.Surface;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DoubleClicking {

    private static final float PIXEL_DISTANCE_THRESHOLD = 0.51f;

    private final int button;

    /** accepts the mouse point where clicked */
    private final Consumer<v2> onDoubleClick;
    private final Surface clicked;

    private v2 doubleClickSpot;

    /** in milliseconds */
    private long doubleClickTime = Long.MIN_VALUE;


    public DoubleClicking(int button, Consumer<v2> doubleClicked, Surface clicked) {
        this.clicked = clicked;
        this.button = button;
        this.onDoubleClick = doubleClicked;
    }


//    public boolean update(Finger finger) {
//        if (finger.clickedNow(button, clicked)) {
//
//        }
//    }



    public void reset() {
        doubleClickSpot = null;
        doubleClickTime = Long.MIN_VALUE;
        count.set(0);
    }

    final AtomicInteger count = new AtomicInteger();

    public boolean update(Finger finger) {

        if (!finger.clickedNow(button, clicked))
            return count.get() > 0; //could be in-between presses

        int c = count.incrementAndGet();

        v2 downHit = finger.posPixelPress[button].clone();
        long now = System.nanoTime();

        boolean unclick = false;
        /** in milliseconds */
        long maxDoubleClickTimeNS = 250 * 1000 * 1000;
        if (c > 1 && doubleClickSpot != null && now - doubleClickTime > maxDoubleClickTimeNS) {
            //taking too long, assume only one click so far
            unclick = true;
        } else if (c == 2 && doubleClickSpot != null && !doubleClickSpot.equals(downHit, PIXEL_DISTANCE_THRESHOLD)) {
            //moved, not on original point
            unclick = true;
        }

        if (unclick) {
            count.set(c = 1);
        }

        switch (c) {
            case 1 -> {
                doubleClickSpot = downHit;
                doubleClickTime = now;
            }
            case 2 -> {
                reset();
                onDoubleClick.accept(finger.posGlobal());
                return true;
            }
        }

        return true; //continues
    }

}