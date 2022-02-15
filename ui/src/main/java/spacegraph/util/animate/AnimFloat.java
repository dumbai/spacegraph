package spacegraph.util.animate;

import jcog.Util;
import jcog.signal.MutableFloat;


public class AnimFloat extends MutableFloat implements Animated {

    private float target;
    private final Number speed;
    private boolean running = true;

    public AnimFloat(float current, float speed) {
        super();
        set(current);
        target = current;
        this.speed = new MutableFloat(speed);
    }

    public void stop() {
        running = false;
    }

    public void invalidate() {
        super.set(Float.NaN);
    }

    @Override
    public boolean animate(float dt) {

        float x = floatValue();
        if (x!=x) {
            
            super.set(target);
        } else {
            
            interpLERP(dt);
        }

        return running;
    }

    private void interpLERP(float dt) {
        float rate =
            speed.floatValue() * dt; //HACK?
            //TODO: Math.exp(...) //??
        
        super.set(
                Util.lerp(rate, floatValue(), target)
        );
    }
    @Override
    public float add(float value) {
        float next = target + value;
        set(next);
        return next;
    }

    @Override
    public void set(float value) {
        if (!Float.isFinite(floatValue()))
            super.set(value);
        target = value;
    }

}