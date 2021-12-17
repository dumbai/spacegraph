package spacegraph.audio.synth.string;

/**
 * Created by ericlgame on 23-Feb-16.
 */
public class HarpString extends KarplusStrongString {

    private final double pluckDelta;
    private final double releaseDelta;
    private double filterIn;
    private double filterOut;

    public HarpString (double frequency) {
        super(frequency * 2, 1);
        setMaxVolume(0.4);
        pluckDelta = 1;
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
        double second = buffer.peek();
        double x = (first + second) / 2; // lowpass filter
		filterOut = C * x + filterIn - C * filterOut; // allpass tuning filter
        filterIn = x;
        buffer.enqueue(filterOut * deltaVolume * -1);
    }

    public void release() {
        setDeltaVolume(releaseDelta);
    }
}