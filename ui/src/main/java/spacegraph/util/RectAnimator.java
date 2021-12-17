package spacegraph.util;

import jcog.Util;
import jcog.tree.rtree.Spatialization;
import org.eclipse.collections.api.tuple.primitive.ObjectFloatPair;
import spacegraph.util.animate.Animator;

import java.util.concurrent.atomic.AtomicReference;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

public abstract class RectAnimator implements Animator<MutableRectFloat> {

    /** an estimate; for determining the speed */
    protected float duration;

    /** https://en.wikipedia.org/wiki/Exponential_decay */
    public static class ExponentialRectAnimator extends RectAnimator {

        public ExponentialRectAnimator(MutableRectFloat animated) {
            super(animated);
        }

        @Override
        protected void animate(MutableRectFloat from, MutableRectFloat to, float dt) {

            float r = (float) Math.exp(-dt * duration  /* ? */); //TODO verify
            from.x = Util.lerpSafe(r, from.x, to.x);
            from.y = Util.lerpSafe(r, from.y, to.y);
            from.size(
                    Util.lerpSafe(r, from.w, to.w),
                    Util.lerpSafe(r, from.h, to.h)
            );

        }
    }
    /** rect being animated */
    private final MutableRectFloat rect;

    public final MutableRectFloat target = new MutableRectFloat().setX0Y0WH(0, 0, 1, 1);



    public RectAnimator(MutableRectFloat r) {
        target.set(this.rect = r);
    }

    @Override
    public final MutableRectFloat animated() {
        return rect;
    }

    @Override public final boolean animate(float dt/*..*/) {


//        ObjectFloatPair<MutableRectFloat> n = next.getAndSet(null);
//        if (n!=null) {
//            if (target.setIfChanged(n.getOne(), Spatialization.EPSILONf)) {
//                this.duration = n.getTwo();
//            }
//        }

//        float t = this.timeUntilCanSleep;
//        if (t==t) {

//            //continue
//            if (dt > t) {
//                dt = t;
//                this.timeUntilCanSleep = 0;
//            } else {
//                this.timeUntilCanSleep = t - dt;
//            }
        if (this.duration==duration) { //active?
            animate(rect, target, dt);
            if (rect.equals(target, Spatialization.EPSILONf)) {
                rect.set(target);
                this.duration = Float.NaN;
            }
        }

//        }

        return true;
    }

    protected abstract void animate(MutableRectFloat from, MutableRectFloat to, float dt);


    public final AtomicReference<ObjectFloatPair<MutableRectFloat>> next = new AtomicReference();

    /** reset the targetting */
    public void set(MutableRectFloat target, float ttl) {
        this.next.set(pair(target, ttl));
    }


}
