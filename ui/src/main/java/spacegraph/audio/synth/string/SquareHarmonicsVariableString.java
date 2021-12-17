package spacegraph.audio.synth.string;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ericlgame on 24-Feb-16.
 */
public class SquareHarmonicsVariableString extends VariableDeltaString {

    Map<Integer, Double> harmonics = new HashMap<>();

    public SquareHarmonicsVariableString (double frequency) {
        super(frequency);
        setDeltaVolume(1);
        setMaxVolume(0.2);
        harmonics.put(2, 0.002);
        harmonics.put(6, 0.001);
    }

    public void pluck() {
        clear();
        int capacity = buffer.capacity();
        for (int i = 0; i < capacity; i++) {
            buffer.enqueue(getSample(i));
        }
        resetTics();
        setDeltaVolume(1.2);
    }

    public void tic() {
        double first = buffer.dequeue();
        double second = buffer.peek();
		double last = (first + second) * (deltaVolume / 2);
        last = checkMax(last);
        buffer.enqueue(last);
        oneTic();
        calcDelta();
    }

    public void release() {

    }

    public void calcDelta() {
        if (getTics() < 10000) {
            setDeltaVolume(1.4);
        } else if (getTics() < 40000) {
            setDeltaVolume(1.0);
        } else {
            setDeltaVolume(0.99);
        }
    }

    private double getSample(int index) {
        double position = index / buffer.capacity();
        double sample = 0;
        for (Map.Entry<Integer, Double> integerDoubleEntry : harmonics.entrySet()) {
            int lowHigh = lowHigh(integerDoubleEntry.getKey(), index);
            double factor = integerDoubleEntry.getValue();
            if (lowHigh == 0) {
                sample -= factor;
            } else {
                sample += factor;
            }
        }
        return sample;
    }

    private int lowHigh(int harmonic, int index) {
        double position = (double) index / buffer.capacity();
        double relPos = position * 2 * harmonic;
        int floored = (int) Math.round(Math.floor(relPos));
        int modded = floored % 2;
        return modded;
    }
}
