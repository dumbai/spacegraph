package spacegraph.audio.synth.string;

/**
 * Created by ericlgame on 24-Feb-16.
 */
public class ViolinString extends VariableDeltaString {

    public ViolinString(double frequency) {
        super(frequency);
        setDeltaVolume(1);
        setMaxVolume(0.1);
        setInitialVolume(0.002);
    }

    /* Pluck the guitar string by replacing the buffer with white noise. */
    public void pluck() {
        clear();
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.enqueue((Math.random() - 0.5) * 2 * getInitialVolume());
        }
        resetTics();
        setDeltaVolume(1.2);
    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */
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
        } else if (getTics() < 50000) {
            setDeltaVolume(1.0);
        } else {
            setDeltaVolume(0.99);
        }
    }

    /*public void calcDelta() {
        if (getTics() < 5000) {
            setDeltaVolume(1.2);
        } else if (getTics() < 15000) {
            setDeltaVolume(1.05);
        }
        else if (getTics() < 50000) {
            setDeltaVolume(1.0);
        } else {
            setDeltaVolume(0.99);
        }
    }*/
}
