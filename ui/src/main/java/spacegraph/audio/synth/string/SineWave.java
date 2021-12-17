package spacegraph.audio.synth.string;

/**
 * Created by ericlgame on 22-Jul-17.
 */

public class SineWave extends KarplusStrongString {

    private final double pluckDelta;
    private final double releaseDelta;
    private double filterIn;
    private double filterOut;

    public SineWave(double frequency) {
        super(frequency, 0);
        setMaxVolume(0.4);
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
            buffer.enqueue((Math.sin(i * 2 * Math.PI / capacity)) * getMaxVolume());
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