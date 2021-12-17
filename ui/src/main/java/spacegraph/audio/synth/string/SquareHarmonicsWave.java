package spacegraph.audio.synth.string;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ericlgame on 24-Feb-16.
 */
public class SquareHarmonicsWave extends KarplusStrongString {

    Map<Integer, Double> harmonics = new HashMap<>();

    private final double pluckDelta;
    private final double releaseDelta;
    private double filterIn;
    private double filterOut;

    public SquareHarmonicsWave (double frequency) {
        super(frequency, 0);
        harmonics.put(1, 0.1);
        harmonics.put(3, 0.05);
        harmonics.put(5, 0.05);
        pluckDelta = 0.9998;
        releaseDelta = 0.9;
        filterIn = 0;
        filterOut = 0;
    }

    public void pluck() {
        setDeltaVolume(pluckDelta);
        clear();
        int capacity = buffer.capacity();
        for (int i = 0; i < capacity; i++) {
            buffer.enqueue(getSample(i));
        }
    }

    public void tic() {
        double first = buffer.dequeue();
        double x = first * deltaVolume;
		filterOut = C * x + filterIn - C * filterOut; // allpass tuning filter
        filterIn = x;
        buffer.enqueue(filterOut * deltaVolume);
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

    public void release() {
        setDeltaVolume(releaseDelta);
    }
}