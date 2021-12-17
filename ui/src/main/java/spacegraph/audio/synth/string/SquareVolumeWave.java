package spacegraph.audio.synth.string;

/**
 * Created by ericlgame on 14-Mar-16.
 */

public class SquareVolumeWave extends KarplusStrongString {

    private final double attackDelta;
    private final double decayDelta;
    private final double releaseDelta;
    private double initialVolume;
    private int state; //0-3 for ADSR

    private double filterIn;
    private double filterOut;

    public SquareVolumeWave(double frequency) {
        super(frequency, 0);
        setMaxVolume(0.3);
        //setInitialVolume(0.2);
        setInitialVolume(0.1);
        filterIn = 0;
        filterOut = 0;
        state = 3;

        attackDelta = Math.pow(1.1, 440.0 / frequency);
        decayDelta = Math.pow(0.9998, 440.0 / frequency);
        double sustainDelta = Math.pow(0.9998, 440.0 / frequency);
        releaseDelta = Math.pow(0.9, 440.0 / frequency);
    }

    public void pluck() {
        // setDeltaVolume(attackDelta);
        state = 0;
        clear();
        int capacity = buffer.capacity();
        int half = capacity / 2;
        for (int i = 0; i < half; i++) {
            buffer.enqueue(getInitialVolume() * -1);
        }
        int otherHalf = capacity - half;
        for (int i = 0; i < otherHalf; i++) {
            buffer.enqueue(getInitialVolume());
        }
        setDeltaVolume(attackDelta);
    }

    public void tic() {
        double first = buffer.dequeue();
        double x = first * deltaVolume;
		filterOut = C * x + filterIn - C * filterOut; // allpass tuning filter
        filterIn = x;
        double next = checkMax(filterOut * deltaVolume);
        if (state == 0 && next != filterOut * deltaVolume) {
            setDeltaVolume(decayDelta);
            state = 1;
        }
        buffer.enqueue(next);
    }

    public void release() {
        setDeltaVolume(releaseDelta);
        state = 3;
    }
}