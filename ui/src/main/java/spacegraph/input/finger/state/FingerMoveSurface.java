package spacegraph.input.finger.state;

import jcog.tree.rtree.rect.RectF;
import org.jetbrains.annotations.Nullable;
import spacegraph.input.finger.Finger;
import spacegraph.space2d.Surface;
import spacegraph.space2d.widget.windo.Windo;

public class FingerMoveSurface extends FingerMove {

    /**
     * what is being moved
     */
    private final Surface moving;

    /**
     * bounds of moved surface, captured at drag start
     */
    private @Nullable RectF before;

    @Deprecated public FingerMoveSurface(Surface moving) {
        this(moving, 0 /* LEFT BUTTON */);
    }
    public FingerMoveSurface(Surface moving, int button) {
        this(moving, button /* LEFT BUTTON */, true, true);
    }

    private FingerMoveSurface(Surface moving, int button, boolean xAxis, boolean yAxis) {
        super(button, xAxis, yAxis);
        this.moving = moving;
    }

    //    @Override
//    public boolean drag(Finger f) {
//        if (super.drag(f)) {
//            if (before == null)
//                this.before = moving.bounds;
//            return true;
//        } else {
//            this.before = null;
//            return false;
//        }
//    }
    @Override
    public boolean drag(Finger f) {
        if (before == null)
            this.before = moving.bounds;

        return super.drag(f);
    }

    @Override
    public void stop(Finger finger) {
        super.stop(finger);
        before = null;
    }



    @Override
    public void move(float tx, float ty) {
//        @Nullable RectFloat before = this.before;
////        if (before!=null) {
//            tx += before.x;
//            ty += before.y;
////        }

        RectF target = RectF.X0Y0WH(tx + before.x, ty + before.y, moving.w(), moving.h());
        if (moving instanceof Windo)
            ((Windo)moving).posFinger(target);
        else
            moving.pos(target);
        //moving.pos(RectFloat.XYWH(tx + before.x, ty + before.y, moving.w() + tx, moving.h() + ty));
        //moving.pos(tx + before.x, ty + before.y);
    }

    @Override
    public Surface touchNext(Surface prev, Surface next) {
        return moving;
    }
}
