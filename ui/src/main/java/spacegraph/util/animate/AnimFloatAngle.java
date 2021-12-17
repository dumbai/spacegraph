//package spacegraph.util.animate;
//
//
//import org.hipparchus.util.MathUtils;
//
///**
// * Created by me on 6/25/16.
// */
//public class AnimFloatAngle extends AnimFloat {
//
//    public AnimFloatAngle(float current, float speed) {
//        super(current, speed);
//    }
//
//    @Override
//    public void set(float value) {
//        float angleToRad = (float)Math.PI/180f;
//        float base = super.floatValue() * angleToRad;
//        value = (float) MathUtils.normalizeAngle(value * angleToRad - base, 0 /* places it nearest to current value */) + base;
//        super.set(value/angleToRad);
//    }
//}
