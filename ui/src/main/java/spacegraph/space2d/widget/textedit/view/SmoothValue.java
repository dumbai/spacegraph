package spacegraph.space2d.widget.textedit.view;

import jcog.Util;
import jcog.data.list.MetalConcurrentQueue;
import jcog.data.list.MetalRing;

public class SmoothValue {

    private static final int NUM_OF_DIV = 15;
    private final MetalRing<Double> queue = new MetalConcurrentQueue(32);
    private final int numOfDiv;
    private double value;
    private double currentValue;
    private Interpolator interpolator;

    public SmoothValue() {
        this(0, NUM_OF_DIV, Interpolator.SMOOTH);
    }

    public SmoothValue(double initialValue) {
        this(initialValue, NUM_OF_DIV, Interpolator.SMOOTH);
    }

    public SmoothValue(double initialValue, Interpolator interpolator) {
        this(initialValue, NUM_OF_DIV, interpolator);
    }

    public SmoothValue(double initialValue, int delayTime) {
        this(initialValue, NUM_OF_DIV, Interpolator.SMOOTH);
    }

    private SmoothValue(double initialValue, int numOfDiv, Interpolator interpolator) {
        this.value = initialValue;
        this.currentValue = initialValue;
        this.numOfDiv = numOfDiv;
        this.interpolator = interpolator;
    }

    public void set(double value) {
        set(value, 0, interpolator);
    }

    public void set(double value, int delayTime) {
        set(value, delayTime, interpolator);
    }

    public void set(double value, Interpolator interpolator) {
        set(value, 0, interpolator);
    }

    private void set(double value, int delayTime, Interpolator interpolator) {
        set(value, delayTime, interpolator, numOfDiv);
    }

    public void setWithoutSmooth(double value) {
        this.currentValue += value - this.value;
        this.value = value;
    }

    private void set(double nextValue, int delayTime, Interpolator interpolator, int numOfDiv) {
        this.interpolator = interpolator;

        double delta = nextValue - this.value;
        if (Util.equals(this.value, nextValue))
            return;

        this.value = nextValue;
        double[] gains = interpolator.curve(numOfDiv);

        //queue.clear();
        for (int i = 0; i < delayTime; i++) {
            queue.offer(0.0);
        }
        for (int i = 0; i < numOfDiv; i++) {
            if (queue.isEmpty()) {
                queue.offer(gains[i] * delta);
            } else {
                queue.offer(gains[i] * delta + queue.poll());
            }
        }
    }

    public void add(double value) {
        set(this.value + value);
    }

    public final double value() {
        return value(true);
    }

    private double value(boolean withUpdate) {
        if (withUpdate) {
            update();
        }
        return currentValue;
    }

    private void update() {
        Double next = queue.poll();
        if (next == null) {
            currentValue = value;
        } else {
            currentValue += next;
        }
    }

    public boolean isAnimated() {
        return currentValue!=value;
        //return !Util.equals(currentValue, value, epsilon);
    }

    public double getLastValue() {
        return value;
    }


}