package spacegraph.audio.synth.string;

/**
 * Created by ericlgame on 14-Mar-16.
 */

public class RandomWave extends KarplusStrongString {

    private final double pluckDelta;
    private final double releaseDelta;
    private double filterIn;
    private double filterOut;

    public RandomWave(double frequency) {
        super(frequency, 0);
        setMaxVolume(0.2);
        pluckDelta = 0.9998;
        releaseDelta = 0.9;
        filterIn = 0;
        filterOut = 0;
    }

    public void pluck() {
        setDeltaVolume(pluckDelta);
        clear();
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.enqueue((Math.random() - 0.5) * 2 * getMaxVolume());
        }
    }

    public void tic() {
        double first = buffer.dequeue();
        double x = first * deltaVolume;
		filterOut = C * x + filterIn - C * filterOut; // allpass tuning filter
        filterIn = x;
        buffer.enqueue(filterOut * deltaVolume);
    }

    public void release() {
        setDeltaVolume(releaseDelta);
    }
}